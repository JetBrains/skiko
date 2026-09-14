package org.jetbrains.skiko.renderer

import kotlinx.coroutines.withContext
import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.graphicapi.DxgiFormat

internal class Direct3DRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AwtRenderer(layer, analytics, GraphicsApi.DIRECT3D) {

    private var drawLock = Any()
    private var isSwapChainInitialized = false

    // Native LiveResizeState, 0 if the hook isn't installed.
    private var liveResizeHandle: Long = 0L
    private val liveResizeInstalled: Boolean
        get() = liveResizeHandle != 0L

    private var device: Long = 0L
        get() {
            if (field == 0L) {
                throw RenderException("DirectX12 device is not initialized or already disposed")
            }
            return field
        }

    private var adapter = 0L
    val adapterName: String
    val adapterMemorySize: Long

    init {
        adapter = chooseAdapter(properties.adapterPriority.ordinal)
        if (adapter == 0L) {
            throw RenderException("Failed to choose DirectX12 adapter.")
        }
        adapterName = getAdapterName(adapter)
        adapterMemorySize = getAdapterMemorySize(adapter)
        onDeviceChosen(adapterName)
        device = createDirectXDevice(adapter, layer.contentHandle, layer.transparency)
            .takeIf { it != 0L } ?: throw RenderException("Failed to create DirectX12 device.")

        if (layer.fillsWindow && SkikoProperties.direct3DSynchronousLiveResize) {
            liveResizeHandle = installLiveResizeHook(layer.windowHandle, layer.contentHandle)
        }
    }

    override val renderInfo: String
        get() = renderInfoHeader(layer.renderApi) +
                "Video card: $adapterName\n" +
                "Total VRAM: ${adapterMemorySize / 1024 / 1024} MB\n"

    override val presentsOnResize: Boolean get() = true

    private var context: DirectContext? = null
    private val bufferCount = 2
    private val renderTargets: Array<BackendRenderTarget?> = arrayOfNulls(bufferCount)
    private val surfaces: Array<Surface?> = arrayOfNulls(bufferCount)
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0
    private fun isSurfacesNull() = surfaces.all { it == null }

    init {
        onContextInit()
    }

    override fun releaseResources() = synchronized(drawLock) {
        if (liveResizeInstalled) {
            uninstallLiveResizeHook(liveResizeHandle)
            liveResizeHandle = 0L
        }
        disposeSurfaces()
        context?.close()
        context = null
        disposeDevice(device)
        device = 0L
    }

    // An async EDT present would race the synchronous render on the toolkit thread.
    override fun requestPlatformDrivenFrame() = postLiveResizeRender(liveResizeHandle)

    override suspend fun LayerDrawScope.renderFrame(immediate: Boolean) {
        if (immediate) {
            drawAndSwap(withVsync = SkikoProperties.windowsWaitForVsyncOnRedrawImmediately)
        } else {
            withContext(dispatcherToBlockOn) {
                drawAndSwap(withVsync = properties.isVsyncEnabled)
            }
        }
    }

    private fun LayerDrawScope.drawAndSwap(withVsync: Boolean, waitForComposition: Boolean = false) {
        synchronized(drawLock) {
            if (isDisposed) {
                return
            }
            drawFrame()
            if (waitForComposition) {
                waitForComposition()
            }
            swap(withVsync)
        }
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
                val newContext = DirectContext.makeDirect3D(
                    adapter,
                    getDirectXDevice(device),
                    getDirectXCommandQueue(device)
                )
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
            val surfaceProps = SurfaceProps(pixelGeometry = pixelGeometry)
            for (backBufferIndex in 0 until bufferCount) {
                val renderTarget = BackendRenderTarget.makeDirect3D(
                    width = width,
                    height = height,
                    texturePtr = getDirectXBackBuffer(device, backBufferIndex),
                    format = DxgiFormat.R8G8B8A8_UNORM.value,
                    sampleCnt = 1,
                    levelCnt = 1
                )
                renderTargets[backBufferIndex] = renderTarget
                surfaces[backBufferIndex] = Surface.makeFromBackendRenderTarget(
                    context,
                    renderTarget,
                    SurfaceOrigin.TOP_LEFT,
                    SurfaceColorFormat.RGBA_8888,
                    ColorSpace.sRGB,
                    surfaceProps
                ) ?: throw RenderException("Cannot create surface")
            }

            if (justInitialized) {
                initFence(device)
            }
        }
        val backBufferIndex = getBufferIndex(device)
        surface = surfaces[backBufferIndex]
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
        val surface = surface ?: return
        surface.flushAndSubmit(syncCpu = true)
    }

    private fun disposeSurfaces() {
        for (bufferIndex in 0 until bufferCount) {
            surfaces[bufferIndex]?.close()
            surfaces[bufferIndex] = null
            renderTargets[bufferIndex]?.close()
            renderTargets[bufferIndex] = null
        }
        surface = null
        canvas = null
    }

    private fun changeSize(width: Int, height: Int): Boolean {
        return if (!isSwapChainInitialized) {
            initSwapChain(
                device = device,
                width = width,
                height = height,
                transparency = layer.transparency,
                preferNoneScaling = liveResizeInstalled
            )
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
    @Suppress("unused")
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    /**
     * Called from native code when a live-resize session starts.
     */
    @Suppress("unused")
    private fun onLiveResizeStarted() {
        liveResizeListener?.onLiveResizeStarted()
    }

    /**
     * Called from native code when the live-resize session ends.
     */
    @Suppress("unused")
    private fun onLiveResizeEnded() {
        WinApiEdtInvoker.invokeAndWaitWhilePumping {
            if (isDisposed) return@invokeAndWaitWhilePumping
            javax.swing.SwingUtilities.getWindowAncestor(layer)?.let {
                it.invalidate()
                it.validate()
            }
            liveResizeListener?.onLiveResizeEnded()
        }
    }

    /**
     * Called from native code to draw a frame during live resize.
     *
     * [isResizeFrame] specifies whether this frame actually resizes the window (there could be non-resizing
     * frames during a live resize).
     */
    @Suppress("unused")
    private fun drawFrameWhileLiveResizing(width: Int, height: Int, isResizeFrame: Boolean) {
        WinApiEdtInvoker.invokeAndWaitWhilePumping {
            if (isDisposed) return@invokeAndWaitWhilePumping
            liveResizeListener?.onLiveResizeFrame(width, height, isResizeFrame)
        }
    }

    override fun LayerDrawScope.renderPlatformDrivenFrame(isResizeFrame: Boolean) {
        if (isDisposed) return // may be disposed in user code, during `update`
        drawAndSwap(
            withVsync = !isResizeFrame,
            waitForComposition = isResizeFrame
        )
    }

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createDirectXDevice(adapter: Long, contentHandle: Long, transparency: Boolean): Long
    private external fun getDirectXDevice(device: Long): Long
    private external fun getDirectXCommandQueue(device: Long): Long
    private external fun getDirectXBackBuffer(device: Long, backBufferIndex: Int): Long
    private external fun resizeBuffers(device: Long, width: Int, height: Int)
    private external fun swap(device: Long, isVsyncEnabled: Boolean)
    private external fun disposeDevice(device: Long)
    private external fun getBufferIndex(device: Long): Int
    private external fun initSwapChain(device: Long, width: Int, height: Int, transparency: Boolean, preferNoneScaling: Boolean)
    private external fun initFence(device: Long)
    private external fun getAdapterName(adapter: Long): String
    private external fun getAdapterMemorySize(adapter: Long): Long

    private external fun installLiveResizeHook(window: Long, content: Long): Long
    private external fun uninstallLiveResizeHook(handle: Long)
    private external fun postLiveResizeRender(handle: Long)
    private external fun waitForComposition()

}
