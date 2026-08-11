package org.jetbrains.skiko.renderer

import kotlinx.coroutines.withContext
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skiko.*
import java.awt.Dimension
import java.lang.ref.Reference

internal class Direct3DRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : ContextBasedRenderer(layer, analytics, GraphicsApi.DIRECT3D, "Direct3D") {

    private var drawLock = Any()
    private var isSwapChainInitialized = false

    /**
     * Set for the duration of a resize gesture, to quiesce the async EDT renders so the synchronous native render is
     * the only thing painting.
     */
    @Volatile
    internal var isHandlingLiveResizeNow: Boolean = false

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

    private val adapterName: String
    private val adapterMemorySize: Long

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

        if (layer.fillsWindow && SkikoProperties.direct3DSynchronousLiveResize) {
            liveResizeHandle = installLiveResizeHook(layer.windowHandle, layer.contentHandle)
        }
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing && !isHandlingLiveResizeNow) {
            update()
            drawFrame()
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        if (liveResizeInstalled) {
            uninstallLiveResizeHook(liveResizeHandle)
            liveResizeHandle = 0L
        }
        frameDispatcher.cancel()
        super.dispose()
        disposeDevice(device)
        device = 0L
    }

    override fun onLayerComponentResized() {
        // During live resize, the layer tells us its size directly; the AWT size is not in sync
        if (!isHandlingLiveResizeNow) {
            super.onLayerComponentResized()
        }
    }

    override fun needRender(throttledToVsync: Boolean) {
        checkDisposed()
        if (isHandlingLiveResizeNow) {
            // An async EDT present would race the synchronous render on the toolkit thread.
            postLiveResizeRender(liveResizeHandle)
        } else {
            frameDispatcher.scheduleFrame()
        }
    }

    override fun renderImmediately() {
        checkDisposed()
        update()
        inDrawScope {
            if (!isDisposed) { // Renderer may be disposed in user code, during `update`
                drawAndSwap(withVsync = SkikoProperties.windowsWaitForVsyncOnRedrawImmediately)
            }
        }
    }

    private suspend fun drawFrame() {
        inDrawScope {
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
            draw()
            if (waitForComposition) {
                waitForComposition()
            }
            swap(withVsync)
        }
    }

    override fun makeContext() = DirectContext(
        makeDirectXContext(device)
    )

    private fun makeSurface(context: Long, width: Int, height: Int, surfaceProps: SurfaceProps, index: Int): Surface {
        return interopScope {
            Surface(makeDirectXSurface(device, context, width, height, toInterop(surfaceProps.packToIntArray()), index))
        }
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

    private fun getBufferIndex() = getBufferIndex(device)
    private fun initFence() = initFence(device)

    // Called from native code
    @Suppress("unused")
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    /**
     * Called from native code when a live-resize session starts.
     */
    @Suppress("unused")
    private fun onLiveResizeStarted() {
        isHandlingLiveResizeNow = true
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
            isHandlingLiveResizeNow = false
            renderImmediately()
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
            val size = Dimension(width, height)
            update(forcedSize = size)
            inDrawScope(forcedSize = size) {
                if (!isDisposed) {
                    drawAndSwap(
                        withVsync = !isResizeFrame,
                        waitForComposition = isResizeFrame
                    )
                }
            }
        }
    }

    private val bufferCount = 2
    private var surfaces: Array<Surface?> = arrayOfNulls(bufferCount)
    private fun isSurfacesNull() = surfaces.all { it == null }

    private var currentWidth = 0
    private var currentHeight = 0

    override fun LayerDrawScope.initCanvas() {
        val context = context ?: return

        // Direct3D can't work with zero size.
        // Don't rewrite code to skipping, as we need the whole pipeline in zero case too
        // (drawing -> flushing -> swapping -> waiting for vsync)
        val width = scaledLayerWidth.coerceAtLeast(1)
        val height = scaledLayerHeight.coerceAtLeast(1)

        val sizeChanged = (width != currentWidth || height != currentHeight)
        if (sizeChanged) {
            currentWidth = width
            currentHeight = height
        }

        if (sizeChanged || isSurfacesNull()) {
            disposeCanvas()
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
                initFence()
            }
        }
        surface = surfaces[getBufferIndex()]
        canvas = surface!!.canvas
    }

    override fun flush() {
        val context = context ?: return
        val surface = surface ?: return
        try {
            flush(getPtr(context), getPtr(surface))
        } finally {
            Reference.reachabilityFence(context)
            Reference.reachabilityFence(surface)
        }
    }

    override fun disposeCanvas() {
        for (bufferIndex in 0 until bufferCount) {
            surfaces[bufferIndex]?.close()
        }
    }

    override val renderInfo: String
        get() = super.renderInfo +
                "Video card: $adapterName\n" +
                "Total VRAM: ${adapterMemorySize / 1024 / 1024} MB\n"

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

    private external fun installLiveResizeHook(window: Long, content: Long): Long
    private external fun uninstallLiveResizeHook(handle: Long)
    private external fun postLiveResizeRender(handle: Long)
    private external fun waitForComposition()

    /**
     * Flushes the given Skia surface into the Direct3D swap-chain buffer.
     *
     * @see "src/awtMain/cpp/windows/direct3DContext.cc" -- native implementation
     */
    private external fun flush(context: Long, surface: Long)
}
