package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Component
import java.awt.Dimension
import javax.swing.SwingUtilities.*
import kotlin.time.Duration.Companion.milliseconds

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
 * @see FrameDispatcher
 */
internal class MetalRedrawer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.METAL) {

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

    /**
     * Whether this redrawer is currently driving an interactive live-resize itself (only ever true when
     * [SkikoProperties.metalSynchronousLiveResize] is enabled and this layer fills the window). Set for the
     * duration of a drag; it pauses the loop's frames ([runFrame]) and the bounds sync ([syncBounds]) so
     * the synchronous AppKit-main-thread render is the only thing painting during a drag.
     */
    @Volatile
    final override var isHandlingLiveResizeNow: Boolean = false
        private set

    private var frameHost: FrameHost? = null

    private var context: DirectContext? = null

    init {
        onDeviceChosen(adapter.name)
        val numberOfBuffers = properties.frameBuffering.numberOfBuffers() ?: 0 // zero means default for system
        val initDevice = layer.backedLayer.useDrawingSurfacePlatformInfo {
            MetalDevice(
                createMetalDevice(
                    window = layer.windowHandle,
                    transparency = layer.transparency,
                    frameBuffering = numberOfBuffers,
                    adapter = adapter.ptr,
                    platformInfo = it,
                    // Live resize is driven from the window, so only enable it when this layer covers the whole
                    // window. When embedded as a Swing component (fillsWindow = false), fall back to the legacy path.
                    liveResizeEnabled = layer.fillsWindow && SkikoProperties.metalSynchronousLiveResize
                )
            )
        }
        _device = initDevice
        setDisplaySyncEnabled(initDevice.ptr, properties.isVsyncEnabled)
    }

    override val renderInfo: String
        get() = renderInfoHeader(layer.renderApi) +
                "Video card: ${adapter.name}\n" +
                "Total VRAM: ${adapter.memorySize / 1024 / 1024} MB\n"

    private val earlyRecordDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing && !isHandlingLiveResizeNow) frameHost?.updateIfRequested()
    }

    init {
        onContextInit()
    }

    override fun onFrameRequested(throttledToVsync: Boolean) {
        when {
            // The background frame loop is gated off during a resize (two presenters deadlock / starve
            // the drawable pool), so drive animation frames from the AppKit main thread instead — the
            // same single serialized presenter that setBounds uses.
            isHandlingLiveResizeNow -> scheduleFrameOnAppKitThread()
            !throttledToVsync -> earlyRecordDispatcher.scheduleFrame()
        }
    }

    override fun releaseResources() {
        earlyRecordDispatcher.cancel()
        releaseGpuResources()
    }

    private fun releaseGpuResources() = synchronized(drawLock) {
        context?.close()
        context = null
        disposeDevice(device.ptr)
        adapter.dispose()
        vSyncer?.dispose()
        _device = null
    }

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        if (immediate) {
            performFrame(scope)
            // Trying to draw immediately in Metal will result in lost (undrawn)
            // frames if there are more than two between consecutive vsync events.
            if (SkikoProperties.macOSWaitForPreviousFrameVsyncOnRedrawImmediately) {
                vSyncer?.waitForVSync()
            }
        } else {
            // Move drawing to another thread to free the main thread
            // It can be expensive to run it in the main thread, and FPS can become unstable.
            // This is visible by running [SkiaLayerPerformanceTest], standard deviation is increased significantly.
            withContext(dispatcherToBlockOn) {
                performFrame(scope)
            }
            // When window is not visible - it doesn't make sense to redraw fast to avoid battery drain.
            if (!isDisposed && isWindowOccluded) {
                withTimeoutOrNull(300.milliseconds) {
                    // If the window becomes non-occluded, stop waiting immediately
                    @Suppress("ControlFlowWithEmptyBody")
                    while (windowOcclusionStateChannel.receive()) { }
                }
            }
        }
    }

    override fun renderBeforeShown(scope: LayerDrawScope): Boolean {
        performFrame(scope, finishFrame = false)
        performNativeDrawAction {
            finishFrameSync(device.ptr)
        }
        return true
    }

    override suspend fun runFrame(frame: suspend () -> Unit) {
        if (isHandlingLiveResizeNow) return
        frame()
        vSyncer?.waitForVSync()
    }

    // Called from MetalRedrawer.mm
    @Suppress("unused")
    fun onOcclusionStateChanged(isOccluded: Boolean) {
        isWindowOccluded = isOccluded
        windowOcclusionStateChannel.trySend(isOccluded)
    }

    private fun performFrame(scope: LayerDrawScope, finishFrame: Boolean = true) = performNativeDrawAction {
        with(scope) { drawFrame() }
        if (finishFrame) {
            finishFrameAsync(device.ptr)
        }
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session starts.
     */
    @Suppress("unused")
    fun onLiveResizeStarted() {
        isHandlingLiveResizeNow = true
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session ends.
     */
    @Suppress("unused")
    fun onLiveResizeEnded() {
        isHandlingLiveResizeNow = false
        invokeLater {
            if (!isDisposed) {
                frameHost?.requestFrame(throttledToVsync = false)
            }
        }
    }

    override fun attachFrameHost(host: FrameHost) {
        frameHost = host
    }

    /**
     * Called from native code, on the AppKit main thread, to draw a frame during live resize.
     */
    @Suppress("unused")
    fun drawFrameWhileLiveResizing(width: Int, height: Int) {
        if (isDisposed || width <= 0 || height <= 0) return

        // Record content at exactly the present size, on the EDT.
        try {
            invokeOnEventThreadAndWait {
                if (isDisposed) return@invokeOnEventThreadAndWait
                frameHost?.inForcedSizeFrame(Dimension(width, height)) { scope ->
                    if (!isDisposed) {  // may be disposed in user code, during `update`
                        // The present must run on the AppKit main thread to join the resize transaction, so
                        // only record here; `finishFrameSync` presents below on the AppKit main thread
                        performFrame(scope, finishFrame = false)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to record live-resize frame" }
            return
        }

        // The present must run on the AppKit main thread to join the resize transaction
        synchronized(drawLock) {
            if (!isDisposed) {
                finishFrameSync(device.ptr)
            }
        }
    }

    /**
     * Requests one frame on the AppKit main thread (during a live resize).
     *
     * Coalescing (at most one pending frame) lives natively in `scheduleFrameOnAppKitThread`, which hops to the
     * main queue and calls back into [drawFrameWhileLiveResizing], where the frame is rendered and presented.
     */
    private fun scheduleFrameOnAppKitThread() {
        if (isDisposed) return
        scheduleFrameOnAppKitThread(device.ptr)
    }

    private fun LayerDrawScope.drawFrame() {
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic Metal context")
        }
        drawViaSurface(scaledLayerWidth, scaledLayerHeight) { surface ->
            surface.canvas.runRestoringState {
                clear(Color.TRANSPARENT)
                layer.draw(this)
            }
        }
        // Records only; the caller presents.
        Logger.debug { "MetalRedrawer finished drawing frame" }
    }

    private fun ensureContext(): Boolean {
        if (context == null) {
            try {
                val newContext = DirectContext(makeMetalContext(device.ptr))
                context = newContext
                onContextInitialized(newContext, layer.properties.gpuResourceCacheLimit) { renderInfo }
            } catch (e: Exception) {
                Logger.warn(e) { "Failed to create Skia Metal context!" }
                return false
            }
        }
        return true
    }

    private inline fun LayerDrawScope.drawViaSurface(
        width: Int,
        height: Int,
        block: (surface: Surface) -> Unit
    ) {
        if (width <= 0 || height <= 0) return

        var renderTarget: BackendRenderTarget? = null
        var surface: Surface? = null

        try {
            renderTarget = BackendRenderTarget(makeMetalRenderTarget(device.ptr, width, height))
            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            block(surface)

            context?.flush()
            surface.flushAndSubmit()
        } finally {
            surface?.close()
            renderTarget?.close()
        }
    }

    override fun syncBounds() = synchronized(drawLock) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isHandlingLiveResizeNow) return

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
        if (!isDisposed) {
            setLayerVisible(device.ptr, isVisible)
        }
    }

    /**
     * Wraps [block] in the necessary machinery needed to call native, drawing-related, code.
     */
    private inline fun performNativeDrawAction(block: () -> Unit) {
        synchronized(drawLock) {
            if (!isDisposed) {
                autoreleasepool {  // This is needed only if the call is not on the AppKit thread
                    block()
                }
            }
        }
    }

    private external fun createMetalDevice(window: Long, transparency: Boolean, frameBuffering: Int, adapter: Long, platformInfo: Long, liveResizeEnabled: Boolean): Long
    private external fun disposeDevice(device: Long)
    private external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    private external fun setLayerVisible(device: Long, isVisible: Boolean)
    private external fun setContentScale(device: Long, contentScale: Float)

    /**
     * Hops to the AppKit main thread (main dispatch queue) and calls back into [drawFrameWhileLiveResizing] with
     * the layer's current pixel size. Used to drive frames through the single main-thread presenter during
     * a live resize.
     */
    private external fun scheduleFrameOnAppKitThread(device: Long)

    /**
     * Like [javax.swing.SwingUtilities.invokeAndWait], but keeps the AppKit run loop spinning while waiting, so
     * synchronous Java->AppKit calls made from [runnable] are serviced rather than deadlocking.
     * [component] provides the AWT context for the call.
     *
     * This is done via `LWCToolkit.invokeAndWait`. `LWCToolkit` lives in a non-exported JDK package, but JNI is not
     * subject to module access checks, so this needs no `--add-opens`.
     */
    private external fun invokeOnEventThreadAndWait(runnable: Runnable, component: Component)

    private fun invokeOnEventThreadAndWait(runnable: Runnable) {
        invokeOnEventThreadAndWait(runnable, layer)
    }

    /**
     * Set this value to true to synchronize the presentation of the layer’s contents with the display’s refresh,
     * also known as vsync or vertical sync. If false, the layer presents new content more quickly,
     * but possibly with brief visual artifacts (screen tearing).
     *
     * @note see https://developer.apple.com/documentation/quartzcore/cametallayer/2887087-displaysyncenabled
     */
    private external fun setDisplaySyncEnabled(device: Long, enabled: Boolean)

    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    /**
     * Presents the frame asynchronously (off the AppKit main thread).
     */
    private external fun finishFrameAsync(device: Long)

    /**
     * Presents the frame synchronously, in the calling thread.
     *
     * Used in two scenarios:
     * - During live-resize it is called on the AppKit main thread, to join the ambient window-resize transaction.
     * - Before showing the window, it is called while the layer is already displayable (but not yet showing), so the
     *   window's first on-screen frame draws content instead of flashing its background.
     */
    private external fun finishFrameSync(device: Long)
}
