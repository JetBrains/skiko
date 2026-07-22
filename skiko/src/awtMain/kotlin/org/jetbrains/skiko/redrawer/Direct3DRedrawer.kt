package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.withContext
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.Direct3DContextHandler
import java.awt.Dimension

internal class Direct3DRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.DIRECT3D) {

    private val contextHandler = Direct3DContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var drawLock = Any()
    private var isSwapChainInitialized = false

    // Which interactive-resize mechanism this window uses, fixed at install time. isInLiveResize is the shared
    // "in a resize" flag for both hooked modes; the mode selects the render target (initCanvas/drawAndSwap) and
    // the on-screen swapchain scaling (NONE only for the fallback pre-render).
    private enum class LiveResizeMode { NONE, OVERLAY, FALLBACK }
    private var liveResizeMode = LiveResizeMode.NONE

    // Overlay path routes rendering into the frame overlay surface (read by Direct3DContextHandler).
    internal val liveResizeUsesOverlay: Boolean get() = liveResizeMode == LiveResizeMode.OVERLAY

    private var device: Long = 0L
        get() {
            if (field == 0L) {
                throw RenderException("DirectX12 device is not initialized or already disposed")
            }
            return field
        }

    val adapterName: String
    val adapterMemorySize: Long

    init {
        val adapter = chooseAdapter(properties.adapterPriority.ordinal)
        if (adapter == 0L) {
            throw RenderException("Failed to choose DirectX12 adapter.")
        }
        adapterName = getAdapterName(adapter)
        adapterMemorySize = getAdapterMemorySize(adapter)
        onDeviceChosen(adapterName)
        device = createDirectXDevice(adapter, layer.contentHandle, layer.transparency)
            .takeIf { it != 0L } ?: throw RenderException("Failed to create DirectX12 device.")

        // Pick the interactive live-resize mechanism from whether the frame has WS_EX_NOREDIRECTIONBITMAP (no DWM
        // redirection surface): the synchronous OVERLAY composites cleanly only on such a frame (a JBR that honors
        // the "jbr.window.noRedirectionBitmap" property Compose sets creates one); on a redirected frame the
        // overlay's shrink case bleeds white, so use the on-screen pre-render FALLBACK instead. Detect the ex-style
        // directly on the realized frame HWND rather than guessing from the JVM.
        if (layer.fillsWindow && SkikoProperties.direct3DSynchronousLiveResize) {
            if (frameHasNoRedirectionBitmap(layer.windowHandle)) {
                liveResizeMode = LiveResizeMode.OVERLAY
                installLiveResizeHook(device, layer.windowHandle, layer.contentHandle)
            } else {
                // On a redirected frame the overlay still bleeds white, so use the "delay the resize" approach
                // instead — synchronously pre-render the ON-SCREEN swapchain at the new size inside WM_NCCALCSIZE,
                // before the geometry commits, so DWM never exposes an unrendered region.
                liveResizeMode = LiveResizeMode.FALLBACK
                installFallbackResizeHook(device, layer.windowHandle, layer.contentHandle)
            }
        }
    }

    // called from native on the toolkit thread on WM_EXITSIZEMOVE.
    @Suppress("unused")
    private fun onLiveResizeEnded() {
        frameDispatcher.scheduleFrame() // resume the normal animation loop
    }

    // called from native (toolkit thread, pump-waited) at drag-end while the overlay still covers the
    // canvas. Runs on the EDT: the contentPane/layer/canvas lag the final window size by one resize step (Swing
    // knows the final window size but never re-lays-out at rest), so force a layout pass to catch the canvas up
    // to the exact native client size, then render it — so revealing it doesn't snap a frame later.
    @Suppress("unused")
    private fun onLiveResizeFinalize() {
        EdtInvoker.invokeAndWaitWhilePumping {
            javax.swing.SwingUtilities.getWindowAncestor(layer)?.let {
                it.invalidate()
                it.validate()
            }
            isInLiveResize = false
            renderImmediately() // render the canvas at the final size before it's revealed
        }
    }

    // called from native (toolkit thread) inside WM_NCCALCSIZE during a drag. Renders the REAL
    // renderDelegate content at the new client size straight into the frame overlay's comp swapchain and
    // presents it synchronously. Shares the live render context (via contextHandler) and serializes on
    // drawLock so it never races an in-flight EDT draw.
    // Called from native (toolkit thread) in WM_NCCALCSIZE. onRender (user/Compose code) requires the EDT, so
    // we hop to it via invokeAndWait — the toolkit thread blocks until the EDT finishes rendering+presenting,
    // so the present still completes before WM_NCCALCSIZE returns (atomicity preserved). This is the Windows
    // analog of the Metal fix's LWCToolkit.invokeAndWait; de-risks the toolkit→EDT hop during the modal loop.
    @Suppress("unused")
    private fun drawFrameWhileLiveResizing(width: Int, height: Int) {
        isInLiveResize = true

        // onRender requires the EDT, but we're on the toolkit thread. Run the render on the EDT and block here
        // until it finishes — so the present still completes before WM_NCCALCSIZE returns (atomicity preserved).
        EdtInvoker.invokeAndWaitWhilePumping {
            if (isDisposed) return@invokeAndWaitWhilePumping
            synchronized(drawLock) {
                // The differences from the usual path are the target surface and the present:
                //  - contextHandler.draw() renders into the frame overlay surface, which
                //    Direct3DContextHandler.initCanvas selects (instead of the usual swapchain surface) while
                //    isInLiveResize;
                //  - presentLiveResizeFrame() presents the overlay comp-swapchain synchronously in WM_NCCALCSIZE
                //    (vs swap()).
                val size = Dimension(width, height)
                update(forcedSize = size)
                inDrawScope(forcedSize = size) {
                    contextHandler.draw()
                    presentLiveResizeFrame(properties.isVsyncEnabled)
                }
            }
        }
    }

    // called from native (toolkit thread) in WM_NCCALCSIZE on the non-NOREDIR fallback. Synchronously renders the
    // real content into the ON-SCREEN swapchain at the new client size and presents it, BEFORE the resize geometry
    // is committed — the "delay the resize until content is drawn" path. Because liveResizeUsesOverlay is false,
    // initCanvas takes the on-screen branch (changeSize -> resizeBuffers to the forced size) and drawAndSwap runs.
    @Suppress("unused")
    private fun renderOnScreenWhileResizing(width: Int, height: Int, vsync: Boolean) {
        isInLiveResize = true // quiesce the async EDT renders for the rest of the drag (cleared at drag end)
        EdtInvoker.invokeAndWaitWhilePumping {
            if (isDisposed) return@invokeAndWaitWhilePumping
            val size = Dimension(width, height)
            update(forcedSize = size)
            inDrawScope(forcedSize = size) {
                if (!isDisposed) {
                    // Always Present(0): DWM composites the windowed swapchain at its own vblank (no tearing), and
                    // Present(1) here would beat against DWM's cadence to refresh x 2/3. Pace the stationary hold
                    // instead by waiting on the real vblank below; the active drag stays unpaced (mouse-driven).
                    drawAndSwap(withVsync = false)
                }
            }
            // Pace the stationary hold at the vblank. Beyond capping the idle FPS, this is what keeps ORIGIN-MOVE
            // (top/left) resize clean: real drags are full of micro-pauses that hit this WM_PAINT hold path, and
            // an unpaced hold floods DWM's present queue, adding latency that desyncs content from the moving
            // window origin. One present per vblank keeps the queue shallow, so top/left stays welded to the edge.
            // (Right/bottom don't move the origin, so they tolerate the latency — which is why only top/left broke.)
            if (vsync && !isDisposed) waitForVBlank()
        }
    }

    // cached overlay surfaces (one per swapchain buffer), recreated only on size change — mirrors the
    // on-screen Direct3DContextHandler. Re-wrapping a fresh SkSurface every frame churns Skia's wrapped render
    // targets and wedges ResizeBuffers.
    private val frameSurfaces = arrayOfNulls<Surface>(2)
    private var frameWidth = 0
    private var frameHeight = 0

    // Current frame-overlay surface for the live-resize draw; (re)creates the cached per-buffer surfaces on a
    // size change (mirrors Direct3DContextHandler's on-screen surface caching — re-wrapping a fresh SkSurface
    // every frame churns Skia's wrapped render targets and wedges ResizeBuffers). Called from that handler's
    // initCanvas while a live resize is in progress.
    fun liveResizeSurface(context: Long, width: Int, height: Int): Surface? {
        if (width <= 0 || height <= 0) return null
        if (width != frameWidth || height != frameHeight || frameSurfaces[0] == null) {
            for (i in frameSurfaces.indices) { frameSurfaces[i]?.close(); frameSurfaces[i] = null }
            resizeLiveResizeBuffers(device, context, width, height)
            for (i in 0 until 2) {
                val p = makeLiveResizeSurface(device, context, width, height, i)
                if (p == 0L) return null
                frameSurfaces[i] = Surface(p)
            }
            frameWidth = width
            frameHeight = height
        }
        return frameSurfaces[liveResizeBufferIndex()]
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing && !isInLiveResize) {
            update()
            draw()
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        // Restore the frame's WndProc and drop the native globals BEFORE freeing the device they reference, so a
        // resize message arriving after this can't call into freed state.
        if (liveResizeMode != LiveResizeMode.NONE) {
            uninstallLiveResizeHook()
        }
        frameDispatcher.cancel()
        for (i in frameSurfaces.indices) { frameSurfaces[i]?.close(); frameSurfaces[i] = null }
        contextHandler.dispose()
        disposeDevice(device)
        device = 0L
        super.dispose()
    }

    override fun needRender(throttledToVsync: Boolean) {
        checkDisposed()
        // during a resize the overlay must be presented only from the toolkit thread (synchronized with the
        // resize loop) — an async EDT present corrupts the flip swapchain. Route needRender to a coalesced toolkit
        // present (postLiveResizeRender), which self-gates to fire only when a resize step isn't already presenting
        // (i.e. a stationary hold). Active-drag animation is driven by the WM_NCCALCSIZE renders themselves.
        if (isInLiveResize) {
            postLiveResizeRender()
        } else {
            frameDispatcher.scheduleFrame()
        }
    }

    override fun renderImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
                drawAndSwap(withVsync = SkikoProperties.windowsWaitForVsyncOnRedrawImmediately)
            }
        }
    }

    private suspend fun draw() {
        inDrawScope {
            withContext(dispatcherToBlockOn) {
                drawAndSwap(withVsync = properties.isVsyncEnabled)
            }
        }
    }

    private fun LayerDrawScope.drawAndSwap(withVsync: Boolean) = synchronized(drawLock) {
        // Block on-screen presents only while the OVERLAY owns the display; the fallback path's pre-render
        // deliberately drives drawAndSwap during a resize (its async callers are already quiesced by isInLiveResize).
        if (isDisposed || (isInLiveResize && liveResizeUsesOverlay)) {
            return
        }
        contextHandler.draw()
        swap(withVsync)
    }

    fun makeContext() = DirectContext(
        makeDirectXContext(device)
    )

    fun makeSurface(context: Long, width: Int, height: Int, surfaceProps: SurfaceProps, index: Int): Surface {
        return interopScope {
            Surface(makeDirectXSurface(device, context, width, height, toInterop(surfaceProps.packToIntArray()), index))
        }
    }

    fun changeSize(width: Int, height: Int): Boolean {
        return if (!isSwapChainInitialized) {
            initSwapChain(device, width, height, layer.transparency, liveResizeMode == LiveResizeMode.FALLBACK)
            isSwapChainInitialized = true
            true
        } else {
            resizeBuffers(device, width, height)
            false
        }
    }

    private fun swap(withVsync: Boolean) {
        if (!isSwapChainInitialized) {
            return
        }
        swap(device, withVsync)
    }

    fun getBufferIndex() = getBufferIndex(device)
    fun initFence() = initFence(device)

    // Called from native code
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createDirectXDevice(adapter: Long, contentHandle: Long, transparency: Boolean): Long
    private external fun makeDirectXContext(device: Long): Long
    private external fun makeDirectXSurface(device: Long, context: Long, width: Int, height: Int, surfacePropsIntArray: InteropPointer, index: Int): Long
    private external fun resizeBuffers(device: Long, width: Int, height: Int)
    private external fun swap(device: Long, isVsyncEnabled: Boolean)
    private external fun disposeDevice(device: Long)
    private external fun getBufferIndex(device: Long): Int
    private external fun initSwapChain(device: Long, width: Int, height: Int, transparency: Boolean, preferNoneScaling: Boolean)
    private external fun initFence(device: Long)
    private external fun getAdapterName(adapter: Long): String
    private external fun getAdapterMemorySize(adapter: Long): Long

    // True iff the top-level window (GA_ROOT of `window`) was created with WS_EX_NOREDIRECTIONBITMAP. Gates the
    // live-resize hook: the overlay only composites cleanly on a non-redirected frame.
    private external fun frameHasNoRedirectionBitmap(window: Long): Boolean

    // Installs a WndProc subclass on the top-level window (GA_ROOT of `window`) that, during an interactive
    // resize, renders the real content into the frame's overlay swapchain and presents it synchronously.
    private external fun installLiveResizeHook(device: Long, window: Long, content: Long)

    // Installs the non-overlay fallback hook (on-screen synchronous pre-render in WM_NCCALCSIZE) for redirected
    // frames without WS_EX_NOREDIRECTIONBITMAP.
    private external fun installFallbackResizeHook(device: Long, window: Long, content: Long)

    // Restores the frame's original WndProc and drops the native globals; called from dispose().
    private external fun uninstallLiveResizeHook()

    // Blocks until the primary monitor's next vblank; paces the fallback's stationary-hold render loop.
    private external fun waitForVBlank()

    // render the real renderDelegate content into the frame overlay's own comp swapchain.
    private external fun resizeLiveResizeBuffers(device: Long, context: Long, width: Int, height: Int)
    private external fun makeLiveResizeSurface(device: Long, context: Long, width: Int, height: Int, index: Int): Long
    private external fun liveResizeBufferIndex(): Int
    private external fun presentLiveResizeFrame(vsync: Boolean)
    private external fun postLiveResizeRender()
}
