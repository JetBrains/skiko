package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Component
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicBoolean
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
 * This is the single per-window Metal render context: it owns the native device/adapter lifecycle, the
 * Skia [DirectContext] and on-screen GPU surface for the current frame, and the frame-loop/presentation
 * plumbing.
 *
 * This [MetalRedrawer] draws content on-screen for maximum efficiency,
 * but it may prevent for using it in embedded components (such as interop with Swing).
 *
 * Content to draw is provided by [SkiaLayer.draw].
 *
 * @see "src/awtMain/objectiveC/macos/MetalRedrawerSurface.mm" -- native GPU surface implementation
 * @see FrameDispatcher
 */
internal class MetalRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.METAL) {

    companion object {
        init {
            Library.load()
        }
    }

    /**
     * Guards every native touch point. Frames render off the EDT (see [draw]) while [dispose] can run on the
     * EDT concurrently; both take this lock and re-check [isDisposed] inside it, so [dispose] can't free the
     * native device out from under an in-flight JNI call.
     */
    private val drawLock = Any()

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
     * Whether the window is in live-resize mode.
     */
    @Volatile
    private var isInLiveResize: Boolean = false

    // GPU surface for the current frame; only touched under `drawLock`.
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null

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

    private val frameDispatcher = FrameScheduler()

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        disposeSurface()
        context?.close()
        context = null
        disposeDevice(device.ptr)
        adapter.dispose()
        vSyncer?.dispose()
        _device = null
        super.dispose()
    }

    override fun needRender(throttledToVsync: Boolean) {
        checkDisposed()
        if (isInLiveResize) {
            // The background frame loop is gated off during a resize (two presenters deadlock / starve
            // the drawable pool), so drive animation frames from the AppKit main thread instead — the
            // same single serialized presenter that setBounds uses.
            scheduleFrameOnAppKitThread()
        } else {
            frameDispatcher.scheduleFrame(needUpdate = true, throttledToVsync = throttledToVsync)
        }
    }

    override fun renderImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                performDraw()
                // Trying to draw immediately in Metal will result in lost (undrawn)
                // frames if there are more than two between consecutive vsync events.
                if (SkikoProperties.macOSWaitForPreviousFrameVsyncOnRedrawImmediately) {
                    runBlocking {
                        vSyncer?.waitForVSync()
                    }
                }
            }
        }
    }

    private suspend fun draw() {
        inDrawScope {
            // Move drawing to another thread to free the main thread
            // It can be expensive to run it in the main thread, and FPS can become unstable.
            // This is visible by running [SkiaLayerPerformanceTest], standard deviation is increased significantly.
            withContext(dispatcherToBlockOn) {
                performDraw()
            }
        }
        if (isDisposed) throw CancellationException()

        // When window is not visible - it doesn't make sense to redraw fast to avoid battery drain.
        if (isWindowOccluded) {
            withTimeoutOrNull(300.milliseconds) {
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

    private fun LayerDrawScope.performDraw(finishFrame: Boolean = true) {
        synchronized(drawLock) {
            if (!isDisposed) {
                autoreleasepool {
                    drawFrame()
                    if (finishFrame) {
                        finishFrame(device.ptr)
                    }
                }
            }
        }
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session starts.
     */
    @Suppress("unused")
    fun onLiveResizeStarted() {
        isInLiveResize = true
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session ends.
     */
    @Suppress("unused")
    fun onLiveResizeEnded() {
        isInLiveResize = false
        invokeLater {
            if (!isDisposed) {
                needRender(throttledToVsync = false)
            }
        }
    }

    override fun onPlatformComponentResized() {
        // During live resize, the layer tells us its size directly; the AWT size is not in sync
        if (!isInLiveResize) {
            super.onPlatformComponentResized()
        }
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
                val layerSize = Dimension(width, height)
                update(forcedSize = layerSize)
                inDrawScope(forcedSize = layerSize) {
                    if (!isDisposed) {  // Redrawer may be disposed in user code, during `update`
                        // The present must run on the AppKit main thread to join the resize transaction, so
                        // only record here; `finishFrameInLiveResize` presents below on the AppKit main thread
                        performDraw(finishFrame = false)
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
                finishFrameInLiveResize(device.ptr)
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
        initSurface()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            layer.draw(this)
        }
        surface?.flushAndSubmit()
        // Recording only. The caller presents via `finishFrame` or, during a live resize,
        // `finishFrameInLiveResize` on the AppKit main thread.
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

    private fun LayerDrawScope.initSurface() {
        disposeSurface()

        val width = scaledLayerWidth
        val height = scaledLayerHeight

        if (width > 0 && height > 0) {
            renderTarget = BackendRenderTarget(makeMetalRenderTarget(device.ptr, width, height))

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface!!.canvas
        } else {
            renderTarget = null
            surface = null
            canvas = null
        }
    }

    private fun disposeSurface() {
        surface?.close()
        renderTarget?.close()
        surface = null
        renderTarget = null
        canvas = null
    }

    override fun syncBoundsFromPlatformComponent() = synchronized(drawLock) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInLiveResize) return

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

    override fun setVisible(isVisible: Boolean) = synchronized(drawLock) {
        Logger.debug { "MetalRedrawer#setVisible($isVisible)" }
        if (!isDisposed) {
            setLayerVisible(device.ptr, isVisible)
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

    // Native GPU-surface entry points; implemented in MetalRedrawerSurface.mm.
    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    /** Presents the frame asynchronously (off the main thread). Used for every frame outside a live resize. */
    private external fun finishFrame(device: Long)

    /**
     * Presents the frame synchronously, joining the ambient window-resize transaction.
     * Must be called on the AppKit main thread during a live resize.
     */
    private external fun finishFrameInLiveResize(device: Long)

    private inner class FrameScheduler {
        private var updateRequested = AtomicBoolean(false)

        private fun updateIfRequested() {
            if (updateRequested.getAndSet(false)) {
                update()
            }
        }

        private val updateDispatcher = FrameDispatcher(MainUIDispatcher) {
            // Gated off during a live resize: presentation is driven from the AppKit main thread
            if (layer.isShowing && !isInLiveResize) {
                updateIfRequested()
            }
        }

        private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
            if (layer.isShowing && !isInLiveResize) {
                updateIfRequested()
                draw()
            }
            vSyncer?.waitForVSync()
        }

        fun scheduleFrame(needUpdate: Boolean, throttledToVsync: Boolean) {
            if (needUpdate) {
                updateRequested.set(true)

                if (!throttledToVsync) {
                    updateDispatcher.scheduleFrame()
                }
            }
            frameDispatcher.scheduleFrame()
        }

        fun cancel() {
            updateDispatcher.cancel()
            frameDispatcher.cancel()
        }
    }
}
