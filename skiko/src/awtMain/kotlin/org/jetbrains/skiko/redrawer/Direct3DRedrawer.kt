package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.withContext
import org.jetbrains.skia.DirectContext
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
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    init {
        onContextInit()
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device)
        device = 0L
        super.dispose()
    }

    override fun needRedraw() {
        check(!isDisposed) { "Direct3DRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "Direct3DRedrawer is disposed" }
        inDrawScope {
            update(System.nanoTime())
            drawAndSwap(withVsync = false)
        }
    }

    private suspend fun draw() {
        inDrawScope {
            withContext(dispatcherToBlockOn) {
                drawAndSwap(withVsync = properties.isVsyncEnabled)
            }
        }
    }

    private fun drawAndSwap(withVsync: Boolean) = synchronized(drawLock) {
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
}
