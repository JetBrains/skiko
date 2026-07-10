package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.withContext
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skiko.*
import java.lang.ref.Reference

/**
 * This is the single per-window Direct3D render context: it owns the native DirectX12 device/adapter and
 * swap chain lifecycle, the Skia [DirectContext] and double-buffered on-screen GPU surfaces for the current
 * frame, and the frame-loop/presentation plumbing. Device, swap chain, Skia surfaces, and frame loop all
 * live in this one type, mirroring [MetalRedrawer].
 *
 * Content to draw is provided by [SkiaLayer.draw].
 *
 * @see "src/awtMain/cpp/windows/direct3DContext.cc" -- native GPU surface implementation
 * @see "src/awtMain/cpp/windows/directXRedrawer.cc" -- native device/swap chain implementation
 */
internal class Direct3DRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.DIRECT3D) {

    /**
     * Guards every native touch point. Frames render off the EDT (see [draw]) while [dispose] can run on the
     * EDT concurrently; both take this lock and re-check [isDisposed] inside it, so [dispose] can't free the
     * native device out from under an in-flight JNI call.
     */
    private val drawLock = Any()
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
    }

    override val renderInfo: String
        get() = renderInfoHeader(layer.renderApi) +
                "Video card: $adapterName\n" +
                "Total VRAM: ${adapterMemorySize / 1024 / 1024} MB\n"

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing) {
            update()
            draw()
        }
    }

    init {
        onContextInit()
    }

    // GPU surfaces for the current frame; only touched under `drawLock`.
    private var context: DirectContext? = null
    private val bufferCount = 2
    private val surfaces: Array<Surface?> = arrayOfNulls(bufferCount)
    /** The back buffer the current frame draws into and flushes; picked once per frame by [initSurface]. */
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0
    private fun isSurfacesNull() = surfaces.all { it == null }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        disposeSurfaces()
        context?.close()
        context = null
        disposeDevice(device)
        device = 0L
        super.dispose()
    }

    override fun needRender(throttledToVsync: Boolean) {
        checkDisposed()
        frameDispatcher.scheduleFrame()
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
        if (isDisposed) {
            return
        }
        drawFrame()
        swap(withVsync)
    }

    private fun LayerDrawScope.drawFrame() {
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic Direct3D context")
        }
        initSurface()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            layer.draw(this)
        }
        flushFrame()
    }

    private fun ensureContext(): Boolean {
        if (context == null) {
            try {
                val newContext = DirectContext(makeDirectXContext(device))
                context = newContext
                onContextInitialized(newContext, layer.properties.gpuResourceCacheLimit) { renderInfo }
            } catch (e: Exception) {
                Logger.warn(e) { "Failed to create Skia Direct3D context!" }
                return false
            }
        }
        return true
    }

    private fun LayerDrawScope.initSurface() {
        val context = context ?: return

        // Direct3D can't work with zero size.
        // Don't rewrite code to skipping, as we need the whole pipeline in zero case too
        // (drawing -> flushing -> swapping -> waiting for vsync)
        val width = scaledLayerWidth.coerceAtLeast(1)
        val height = scaledLayerHeight.coerceAtLeast(1)

        if (isSizeChanged(width, height) || isSurfacesNull()) {
            disposeSurfaces()
            context.flush()

            val justInitialized = changeSize(width, height)
            try {
                val surfaceProps = SurfaceProps(pixelGeometry = pixelGeometry)
                for (bufferIndex in 0 until bufferCount) {
                    surfaces[bufferIndex] = makeSurface(
                        context = getPtr(context),
                        width = width,
                        height = height,
                        surfaceProps = surfaceProps,
                        index = bufferIndex
                    )
                }
            } finally {
                Reference.reachabilityFence(context)
            }

            if (justInitialized) {
                initFence(device)
            }
        }
        // Capture the frame's back buffer once. `getBufferIndex` is not a getter: each call advances the
        // swap chain's buffer index, blocks until that buffer's GPU fence completes, and then bumps the
        // fence value. Exactly one call per frame is what `swap`'s matching Signal balances, so a second
        // call would wait on a fence value nothing has signalled yet and block forever.
        surface = surfaces[getBufferIndex(device)]
        canvas = surface!!.canvas
    }

    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    private fun flushFrame() {
        val context = context ?: return
        val surface = surface ?: return
        try {
            flush(getPtr(context), getPtr(surface))
        } finally {
            Reference.reachabilityFence(context)
            Reference.reachabilityFence(surface)
        }
    }

    private fun disposeSurfaces() {
        for (bufferIndex in 0 until bufferCount) {
            surfaces[bufferIndex]?.close()
            surfaces[bufferIndex] = null
        }
        surface = null
        canvas = null
    }

    private fun makeSurface(context: Long, width: Int, height: Int, surfaceProps: SurfaceProps, index: Int): Surface {
        return interopScope {
            Surface(makeDirectXSurface(device, context, width, height, toInterop(surfaceProps.packToIntArray()), index))
        }
    }

    private fun changeSize(width: Int, height: Int): Boolean {
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

    // Native GPU surface flush; implemented in direct3DContext.cc.
    private external fun flush(context: Long, surface: Long)
}
