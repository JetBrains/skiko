package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
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

    private val device: Long
    val adapterName: String
    val adapterMemorySize: Long

    init {
        val adapter = chooseAdapter(properties.adapterPriority.ordinal)
        if (adapter == 0L) {
            throw RenderException("Failed to choose DirectX12 adapter.")
        }
        adapterName = getAdapterName(0)
        adapterMemorySize = getAdapterMemorySize(adapter)
        onDeviceChosen(adapterName)
        device = createDirectXDevice(adapter, layer.contentHandle, layer.transparency)
        if (device == 0L) {
            throw RenderException("Failed to create DirectX12 device.")
        }
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
            withContext(Dispatchers.IO) {
                drawAndSwap(withVsync = properties.isVsyncEnabled)
            }
        }
    }

    private fun drawAndSwap(withVsync: Boolean) = synchronized(drawLock) {
        if (!isDisposed) {
            contextHandler.draw()
            swap(device, withVsync)
        }
    }

    fun makeContext() = DirectContext(
        makeDirectXContext(device)
    )

    fun makeSurface(context: Long, width: Int, height: Int, index: Int) = Surface(
        makeDirectXSurface(device, context, width, height, index)
    )

    fun resizeBuffers(width: Int, height: Int) = resizeBuffers(device, width, height)

    fun getBufferIndex() = getBufferIndex(device)
    fun initSwapChain() = initSwapChain(device)
    fun initFence() = initFence(device)

    // Called from native code
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, name)

    private external fun chooseAdapter(adapterPriority: Int): Long
    private external fun createDirectXDevice(adapter: Long, contentHandle: Long, transparency: Boolean): Long
    private external fun makeDirectXContext(device: Long): Long
    private external fun makeDirectXSurface(device: Long, context: Long, width: Int, height: Int, index: Int): Long
    private external fun resizeBuffers(device: Long, width: Int, height: Int)
    private external fun swap(device: Long, isVsyncEnabled: Boolean)
    private external fun disposeDevice(device: Long)
    private external fun getBufferIndex(device: Long): Int
    private external fun initSwapChain(device: Long)
    private external fun initFence(device: Long)
    private external fun getAdapterName(adapter: Long): String
    private external fun getAdapterMemorySize(adapter: Long): Long
}
