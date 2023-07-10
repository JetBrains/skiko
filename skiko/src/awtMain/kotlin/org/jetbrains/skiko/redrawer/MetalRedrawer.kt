package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
    private val displayLinkThrottler = DisplayLinkThrottler()

    init {
        onDeviceChosen(adapter.name)
        val initDevice = layer.backedLayer.useDrawingSurfacePlatformInfo {
            MetalDevice(createMetalDevice(layer.windowHandle, layer.transparency, adapter.ptr, it))
        }
        _device = initDevice
        contextHandler = MetalContextHandler(layer, initDevice, adapter)
        setVSyncEnabled(initDevice.ptr, properties.isVsyncEnabled)
    }

    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val windowHandle = layer.windowHandle

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
            setVSyncEnabled(device.ptr, enabled = false)
            update(System.nanoTime())
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                performDraw()
                setVSyncEnabled(device.ptr, properties.isVsyncEnabled)
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
            withContext(Dispatchers.IO) {
                performDraw()
            }
        }
        if (isDisposed) throw CancellationException()

        // When window is not visible - it doesn't make sense to redraw fast to avoid battery drain.
        // In theory, we could be more precise, and just suspend rendering in
        // `NSWindowDidChangeOcclusionStateNotification`, but current approach seems to work as well in practise.
        if (isOccluded(windowHandle))
            delay(300)
    }

    private fun performDraw() = synchronized(drawLock) {
        if (!isDisposed) {
            // Wait for vsync because:
            // - macOS drops the second/next drawables if they are sent in the same vsync
            // - it makes frames consistent and limits FPS
            displayLinkThrottler.waitVSync(windowPtr = layer.windowHandle)

            val handle = startRendering()
            try {
                contextHandler.draw()
            } finally {
                endRendering(handle)
            }
        }
    }

    override fun syncSize() = synchronized(drawLock) {
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

    private external fun createMetalDevice(window: Long, transparency: Boolean, adapter: Long, platformInfo: Long): Long
    private external fun disposeDevice(device: Long)
    private external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    private external fun setLayerVisible(device: Long, isVisible: Boolean)
    private external fun setContentScale(device: Long, contentScale: Float)
    private external fun setVSyncEnabled(device: Long, enabled: Boolean)
    private external fun isOccluded(window: Long): Boolean
    private external fun startRendering(): Long
    private external fun endRendering(handle: Long)
}
