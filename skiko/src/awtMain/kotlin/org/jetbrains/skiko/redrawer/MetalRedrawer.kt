package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.MetalContextHandler
import javax.swing.SwingUtilities.*

/**
 * Holder for pointer on MetalDevice described in "MetalDevice.h"
 *
 * Naturally [MetalDevice] is just a holder for native objects required for drawing such as:
 *   * [CAMetalLayer](https://developer.apple.com/documentation/quartzcore/cametallayer)
 *   * [MTLDevice](https://developer.apple.com/documentation/metal/mtldevice)
 *   * etc.
 *
 * @see "src/awtMain/objectiveC/macos/MetalDevice.h"
 */
@JvmInline
internal value class MetalDevice(val ptr: Long)

/**
 * Provides a way to request draws on Skia canvas created in [layer] bounds using Metal GPU acceleration.
 *
 * This [MetalRedrawer] draws content on-screen for maximum efficiency,
 * but it may prevent for using it in embedded components (such as interop with Swing).
 * For off-screen implementation see [MetalOffScreenRedrawer]
 *
 * Content to draw is provided by [SkiaLayer.draw].
 *
 * @see MetalContextHandler
 * @see FrameDispatcher
 */
internal class MetalRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.METAL) {
    private val contextHandler: MetalContextHandler

    companion object {
        init {
            Library.load()
        }
    }

    private var drawLock = Any()

    /**
     * [MetalDevice] initialized for given [layer] or null if [MetalRedrawer] is disposed,
     * so future calls of [device] will throw exception
     */
    private var _device: MetalDevice?

    private val device: MetalDevice
        get() {
            val currentDevice = _device
            require(currentDevice != null) { "Device is disposed" }
            return currentDevice
        }

    private val adapter = chooseMetalAdapter(properties.adapterPriority)
    private val displayLinkThrottler = DisplayLinkThrottler(layer.windowHandle)

    private val windowOcclusionStateChannel = Channel<Boolean>(Channel.CONFLATED)
    @Volatile private var isWindowOccluded = false

    init {
        onDeviceChosen(adapter.name)
        val numberOfBuffers = properties.frameBuffering.numberOfBuffers() ?: 0 // zero means default for system
        val initDevice = layer.backedLayer.useDrawingSurfacePlatformInfo {
            MetalDevice(createMetalDevice(layer.windowHandle, layer.transparency, numberOfBuffers, adapter.ptr, it))
        }
        _device = initDevice
        contextHandler = MetalContextHandler(layer, initDevice, adapter)
        setDisplaySyncEnabled(initDevice.ptr, properties.isVsyncEnabled)
    }

    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    init {
        onContextInit()
    }

    fun drawSync() {
        layer.update(System.nanoTime())
        performDraw()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device.ptr)
        adapter.dispose()
        displayLinkThrottler.dispose()
        _device = null
        super.dispose()
    }

    override fun needRedraw() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "MetalRedrawer is disposed" }
        inDrawScope {
            update(System.nanoTime())
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                performDraw(waitVsync = SkikoProperties.macOSWaitForPreviousFrameVsyncOnRedrawImmediately)
            }
        }
    }

    private suspend fun draw() {
        // 2,3 GHz 8-Core Intel Core i9
        //
        // Test1. 8 windows, multiple clocks, 800x600
        //
        // Executors.newSingleThreadExecutor().asCoroutineDispatcher(): 20 FPS, 130% CPU
        // Dispatchers.IO: 58 FPS, 460% CPU
        //
        // Test2. 60 windows, single clock, 800x600
        //
        // Executors.newSingleThreadExecutor().asCoroutineDispatcher(): 50 FPS, 150% CPU
        // Dispatchers.IO: 50 FPS, 200% CPU
        inDrawScope {
            withContext(dispatcherToBlockOn) {
                performDraw()
            }
        }
        if (isDisposed) throw CancellationException()

        // When window is not visible - it doesn't make sense to redraw fast to avoid battery drain.
        if (isWindowOccluded) {
            withTimeoutOrNull(300) {
                // If the window becomes non-occluded, stop waiting immediately
                @Suppress("ControlFlowWithEmptyBody")
                while (windowOcclusionStateChannel.receive()) { }
            }
        }
    }

    // Called from MetalRedrawer.mm
    @Suppress("unused")
    fun onOcclusionStateChanged(isOccluded: Boolean) {
        isWindowOccluded = isOccluded
        windowOcclusionStateChannel.trySend(isOccluded)
    }

    private fun performDraw(waitVsync: Boolean = true) = synchronized(drawLock) {
        if (!isDisposed) {
            if (waitVsync) {
                // Wait for vsync because:
                // - macOS drops the second/next drawables if they are sent in the same vsync
                // - it makes frames consistent and limits FPS
                displayLinkThrottler.waitVSync()
            }
            autoreleasepool {
                contextHandler.draw()
            }
        }
    }

    override fun syncBounds() = synchronized(drawLock) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        val rootPane = getRootPane(layer)
        val globalPosition = convertPoint(layer.backedLayer, 0, 0, rootPane)
        setContentScale(device.ptr, layer.contentScale)
        val x = globalPosition.x
        val y = rootPane.height - globalPosition.y - layer.height
        val width = layer.backedLayer.width.coerceAtLeast(0)
        val height = layer.backedLayer.height.coerceAtLeast(0)
        Logger.debug { "MetalRedrawer#resizeLayers $this {x: $x y: $y width: $width height: $height} rootPane: ${rootPane.size}" }
        resizeLayers(device.ptr, x, y, width, height)
    }

    override fun setVisible(isVisible: Boolean) {
        Logger.debug { "MetalRedrawer#setVisible $this $isVisible" }
        setLayerVisible(device.ptr, isVisible)
    }

    private external fun createMetalDevice(window: Long, transparency: Boolean, frameBuffering: Int, adapter: Long, platformInfo: Long): Long
    private external fun disposeDevice(device: Long)
    private external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    private external fun setLayerVisible(device: Long, isVisible: Boolean)
    private external fun setContentScale(device: Long, contentScale: Float)

    /**
     * Set this value to true to synchronize the presentation of the layer’s contents with the display’s refresh,
     * also known as vsync or vertical sync. If false, the layer presents new content more quickly,
     * but possibly with brief visual artifacts (screen tearing).
     *
     * @note see https://developer.apple.com/documentation/quartzcore/cametallayer/2887087-displaysyncenabled
     */
    private external fun setDisplaySyncEnabled(device: Long, enabled: Boolean)
}
