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
 *
 * Content to draw is provided by [SkiaLayer.draw].
 *
 * @see MetalContextHandler
 * @see FrameDispatcher
 */
internal class MetalRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
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
    private val vSyncer = if (properties.isVsyncEnabled) MetalVSyncer(layer.windowHandle) else null

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

    private var updateRequested = false

    private fun updateIfRequested() {
        if (updateRequested) {
            // Clear the flag before calling update so that update itself
            // can request updating again.
            updateRequested = false
            update()
        }
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing) {
            // Schedule drawing before calling update so that if update
            // schedules this frameDispatcher again, and update is a long
            // operation, the frame is drawn before update is called again.
            drawOnlyFrameDispatcher.scheduleFrame()
            updateIfRequested()
        }
    }

    private val throttledFrameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing) {
            updateIfRequested()
            draw()
        }
        waitForVSyncIfNeeded()
    }

    private val drawOnlyFrameDispatcher = FrameDispatcher(MainUIDispatcher) {
        draw()
        waitForVSyncIfNeeded()
    }

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        throttledFrameDispatcher.cancel()
        drawOnlyFrameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device.ptr)
        adapter.dispose()
        vSyncer?.dispose()
        _device = null
        super.dispose()
    }

    private suspend fun waitForVSyncIfNeeded() {
        vSyncer?.waitForVSync()
    }

    override fun needRedraw(throttledToVsync: Boolean) {
        checkDisposed()
        updateRequested = true
        if (throttledToVsync) {
            throttledFrameDispatcher.scheduleFrame()
        } else {
            frameDispatcher.scheduleFrame()
        }
    }

    override fun redrawImmediately(updateNeeded: Boolean) {
        checkDisposed()
        if (updateNeeded) {
            update()
        }
        // Trying to draw immediately in Metal will result in lost (undrawn)
        // frames if there's more than two between consecutive vsync events.
        if (layer.isShowing) {
            drawOnlyFrameDispatcher.scheduleFrame()
        } else {
            // But if the layer isn't showing yet, we want to draw immediately,
            // so that if it shows before the next vsync, there is no background flash
            performDraw()
        }
    }

    private suspend fun draw() {
        performDraw()
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

    private fun performDraw() = inDrawScope {
        synchronized(drawLock) {
            if (!isDisposed) {
                autoreleasepool {
                    contextHandler.draw()
                }
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
        Logger.debug { "MetalRedrawer#setVisible($isVisible)" }
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
