package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.withContext
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.ISize
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.Direct3DContextHandler

internal class Direct3DRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.DIRECT3D) {

    private val contextHandler = Direct3DContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var drawLock = Any()
    private var isSwapChainInitialized = false

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

        // Install the WndProc subclass that drives synchronous overlay presents during an interactive
        // (drag) resize (see the native live-resize block).
        installLiveResizeHook(device, layer.windowHandle, layer.contentHandle)
    }

    // the live client size while we (the native modal resize loop) drive the resize; null otherwise. This
    // is the single source of truth for "a live resize is in progress" (isInLiveResize below is derived from it):
    //  - Non-null makes SkiaLayer report isHandlingSizing — the designed hook that stops AWT's reshape from
    //    issuing its own renderImmediately/needRender during the drag (the overlay owns all rendering, so those
    //    would be pure waste: an extra onRender per step). Mirrors Metal's layerSizeInLiveResize.
    //  - isInLiveResize gates the normal Skia present path so the native WM_NCCALCSIZE/WM_PAINT present is the sole
    //    writer to the swapchain during the drag.
    // Set on the first resize step (drawFrameWhileLiveResizing, at WM_NCCALCSIZE — NOT at WM_ENTERSIZEMOVE, so plain moves
    // keep animating the visible canvas), cleared at drag end.
    @Volatile
    private var layerSizeInLiveResize: ISize? = null
    override val layerSizeWhileHandlingSizing: ISize? get() = layerSizeInLiveResize

    private val isInLiveResize: Boolean get() = layerSizeInLiveResize != null

    // called from native on the toolkit thread on WM_EXITSIZEMOVE.
    @Suppress("unused")
    private fun onLiveResizeEnded() {
        layerSizeInLiveResize = null
        frameDispatcher.scheduleFrame() // resume the normal animation loop
    }

    // called from native (toolkit thread, pump-waited) at drag-end while the overlay still covers the
    // canvas. Runs on the EDT: the contentPane/layer/canvas lag the final window size by one resize step (Swing
    // knows the final window size but never re-lays-out at rest), so force a layout pass to catch the canvas up
    // to the exact native client size, then render it — so revealing it doesn't snap a frame later.
    @Suppress("unused")
    private fun onLiveResizeFinalize() {
        javax.swing.SwingUtilities.invokeLater {
            try {
                javax.swing.SwingUtilities.getWindowAncestor(layer)?.let { it.invalidate(); it.validate() }
                layerSizeInLiveResize = null // clear BEFORE renderImmediately so it uses the real backedLayer size
                renderImmediately() // render the canvas at the final size before it's revealed
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                signalRenderDone()
            }
        }
    }

    // called from native (toolkit thread) inside WM_NCCALCSIZE during a drag. Renders the REAL
    // renderDelegate content at the new client size straight into the NOREDIR frame's comp swapchain and
    // presents it synchronously. Shares the live render context (via contextHandler) and serializes on
    // drawLock so it never races an in-flight EDT draw.
    // Called from native (toolkit thread) in WM_NCCALCSIZE. onRender (user/Compose code) requires the EDT, so
    // we hop to it via invokeAndWait — the toolkit thread blocks until the EDT finishes rendering+presenting,
    // so the present still completes before WM_NCCALCSIZE returns (atomicity preserved). This is the Windows
    // analog of the Metal fix's LWCToolkit.invokeAndWait; de-risks the toolkit→EDT hop during the modal loop.
    @Suppress("unused")
    private fun drawFrameWhileLiveResizing(width: Int, height: Int) {
        // Report the live size so SkiaLayer.isHandlingSizing is true — AWT's reshape then skips its own
        // renderImmediately/needRender for the duration of the drag (the overlay is the sole renderer).
        layerSizeInLiveResize = ISize(width, height)
        // Native (toolkit thread) called us and is now pump-waiting on signalRenderDone(). POST the render
        // to the EDT (onRender requires it) and return immediately; the toolkit pump keeps servicing the EDT's
        // cross-thread window ops so this doesn't deadlock. Signal native when the EDT render completes.
        javax.swing.SwingUtilities.invokeLater {
            try {
                renderFrameLocked(width, height)
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                signalRenderDone()
            }
        }
    }

    // cached overlay surfaces (one per swapchain buffer), recreated only on size change — mirrors the
    // on-screen Direct3DContextHandler. Re-wrapping a fresh SkSurface every frame churns Skia's wrapped render
    // targets and wedges ResizeBuffers.
    private val frameSurfaces = arrayOfNulls<Surface>(2)
    private var frameWidth = 0
    private var frameHeight = 0

    private fun renderFrameLocked(width: Int, height: Int): Unit = synchronized(drawLock) {
        if (isDisposed || width <= 0 || height <= 0) return
        val delegate = layer.renderDelegate ?: return
        val ctxPtr = contextHandler.contextPtr()
        if (ctxPtr == 0L) return
        if (width != frameWidth || height != frameHeight || frameSurfaces[0] == null) {
            for (i in frameSurfaces.indices) { frameSurfaces[i]?.close(); frameSurfaces[i] = null }
            resizeFrameBuffers(device, ctxPtr, width, height)
            for (i in 0 until 2) {
                val p = makeFrameSurface(device, ctxPtr, width, height, i)
                if (p == 0L) return
                frameSurfaces[i] = Surface(p)
            }
            frameWidth = width; frameHeight = height
        }
        val surface = frameSurfaces[frameBufferIndex()] ?: return
        delegate.onRender(surface.canvas, width, height, skikoNanoTime())
        flushFrame(ctxPtr, org.jetbrains.skia.impl.getPtr(surface))
        presentFrame(properties.isVsyncEnabled)
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
        if (isInLiveResize) postLiveResizeRender() else frameDispatcher.scheduleFrame()
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
        if (isDisposed || isInLiveResize) { // `isInLiveResize` gate
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
            initSwapChain(device, width, height, layer.transparency)
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
    private external fun initSwapChain(device: Long, width: Int, height: Int, transparency: Boolean)
    private external fun initFence(device: Long)
    private external fun getAdapterName(adapter: Long): String
    private external fun getAdapterMemorySize(adapter: Long): Long

    // Installs a WndProc subclass on the top-level window (GA_ROOT of `window`) that, during an interactive
    // resize, renders the real content into the frame's overlay swapchain and presents it synchronously.
    private external fun installLiveResizeHook(device: Long, window: Long, content: Long)

    // render the real renderDelegate content into the NOREDIR frame's own comp swapchain.
    private external fun resizeFrameBuffers(device: Long, context: Long, width: Int, height: Int)
    private external fun makeFrameSurface(device: Long, context: Long, width: Int, height: Int, index: Int): Long
    private external fun frameBufferIndex(): Int
    private external fun flushFrame(context: Long, surface: Long)
    private external fun presentFrame(vsync: Boolean)
    private external fun signalRenderDone()
    private external fun postLiveResizeRender()
}
