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

    /**
     * Whether this redrawer is currently driving an interactive live-resize itself (only ever true when
     * [SkikoProperties.direct3DSynchronousLiveResize] is enabled and this layer fills the window). Set for the
     * duration of the resize gesture; it quiesces the async EDT renders so the synchronous native render is the only
     * thing painting during a live-resize.
     */
    @Volatile
    internal var isHandlingLiveResizeNow: Boolean = false

    // Opaque handle to this window's native LiveResizeState (0 if the hook isn't installed), returned by
    // installLiveResizeHook and threaded back through postLiveResizeRender/uninstallLiveResizeHook. Per-window, so
    // multiple D3D windows can be hooked at once. When installed, the on-screen swapchain uses DXGI_SCALING_NONE and
    // interactive resizes render synchronously in WM_NCCALCSIZE; isHandlingLiveResizeNow is set for the drag.
    private var liveResizeHandle: Long = 0L
    private val liveResizeInstalled: Boolean get() = liveResizeHandle != 0L

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

        // Install the synchronous live-resize hook: during an interactive drag it renders the real content into
        // the on-screen swapchain at the new size and presents it inside WM_NCCALCSIZE, before the geometry
        // commits, so DWM never exposes an unrendered (white) region at the edges.
        if (layer.fillsWindow && SkikoProperties.direct3DSynchronousLiveResize) {
            liveResizeHandle = installLiveResizeHook(layer.windowHandle, layer.contentHandle)
        }
    }

    // ---- Live-resize lifecycle. All three are called from native on the toolkit thread; a drag runs as
    // onLiveResizeStarted → drawFrameWhileLiveResizing (once per resize step) → onLiveResizeEnded.
    // isHandlingLiveResizeNow is set by the first and cleared by the last. ----

    // Start: the first size-changing step of a drag. Flips the flag (before the first draw) so the async EDT renders
    // (frameDispatcher loop, reshape workaround, onPlatformComponentResized) quiesce for the rest of the drag,
    // leaving the synchronous WM_NCCALCSIZE render as the only thing painting.
    @Suppress("unused")
    private fun onLiveResizeStarted() {
        isHandlingLiveResizeNow = true
    }

    // Draw: called in WM_NCCALCSIZE (active drag) and WM_PAINT (stationary hold). Synchronously renders the real
    // content into the on-screen swapchain at the new client size and presents it, BEFORE the resize geometry
    // commits — "delay the resize until content is drawn". onRender (user/Compose code) requires the EDT, so we hop
    // to it via invokeAndWaitWhilePumping — the toolkit thread blocks until the EDT finishes rendering+presenting,
    // so the present completes before WM_NCCALCSIZE returns. Windows analog of Metal's setBounds + LWCToolkit.invokeAndWait.
    @Suppress("unused")
    private fun drawFrameWhileLiveResizing(width: Int, height: Int) {
        EdtInvoker.invokeAndWaitWhilePumping {
            if (isDisposed) return@invokeAndWaitWhilePumping
            val size = Dimension(width, height)
            update(forcedSize = size)
            inDrawScope(forcedSize = size) {
                if (!isDisposed) {
                    // Always Present(0): DWM composites the windowed swapchain at its own vblank (no tearing);
                    // Present(1) here would beat against DWM's cadence to refresh x 2/3. The stationary hold is
                    // paced natively (WaitForVBlank in the WM_PAINT handler); the active drag stays unpaced.
                    drawAndSwap(withVsync = false)
                }
            }
        }
    }

    // End (drag-end, toolkit thread, pump-waited): runs blocking on the EDT. The contentPane/layer/canvas lag the
    // final window size by one resize step (Swing knows the final window size but never re-lays-out at rest), so
    // force a layout pass to catch the canvas up to the exact native client size, clear the flag, and render at that
    // size before it's revealed. No explicit frameDispatcher.scheduleFrame() to resume the loop: with the flag now
    // cleared, renderImmediately's update() lets onRender re-request a frame (needRender -> scheduleFrame) exactly
    // when content is still animating — the same self-sustaining path the normal loop uses; static content
    // correctly stays idle.
    @Suppress("unused")
    private fun onLiveResizeEnded() {
        EdtInvoker.invokeAndWaitWhilePumping {
            javax.swing.SwingUtilities.getWindowAncestor(layer)?.let {
                it.invalidate()
                it.validate()
            }
            isHandlingLiveResizeNow = false
            renderImmediately() // render the canvas at the final size before it's revealed
        }
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing && !isHandlingLiveResizeNow) {
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
        if (liveResizeInstalled) {
            uninstallLiveResizeHook(liveResizeHandle)
            liveResizeHandle = 0L
        }
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device)
        device = 0L
        super.dispose()
    }

    override fun onPlatformComponentResized() {
        // During live resize, the layer tells us its size directly; the AWT size is not in sync
        if (!isHandlingLiveResizeNow) {
            super.onPlatformComponentResized()
        }
    }

    override fun needRender(throttledToVsync: Boolean) {
        checkDisposed()
        if (isHandlingLiveResizeNow) {
            // during a resize, present only from the toolkit thread (synchronized with the resize loop) — an async EDT
            // present would race the synchronous render.
            postLiveResizeRender(liveResizeHandle)
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
        // No isHandlingLiveResizeNow guard: the live-resize pre-render deliberately drives drawAndSwap during a
        // drag, and the async callers (frameDispatcher loop, reshape workaround) are already quiesced by the flag.
        if (isDisposed) {
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
            initSwapChain(device, width, height, layer.transparency, liveResizeInstalled)
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
    @Suppress("unused")
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

    // Installs a WndProc subclass on the top-level window (GA_ROOT of `window`) that, during an interactive resize,
    // synchronously renders the real content into the on-screen swapchain and presents it in WM_NCCALCSIZE. Returns
    // an opaque handle to the per-window native state (0 on failure), to pass back to the two calls below.
    private external fun installLiveResizeHook(window: Long, content: Long): Long

    // Restores the frame's original WndProc and frees the per-window native state; called from dispose().
    private external fun uninstallLiveResizeHook(handle: Long)

    // Invalidates the frame window so a WM_PAINT drives the stationary-hold render (see needRender).
    private external fun postLiveResizeRender(handle: Long)
}
