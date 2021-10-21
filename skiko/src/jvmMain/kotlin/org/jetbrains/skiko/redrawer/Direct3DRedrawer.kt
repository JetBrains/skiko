package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.Direct3DContextHandler

internal class Direct3DRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer(Direct3DContextHandler(layer)) {

    private var isDisposed = false
    private var drawLock = Any()

    private val device = createDirectXDevice(getAdapterPriority(), layer.contentHandle, layer.transparency).also {
        if (it == 0L || !isVideoCardSupported(layer.renderApi)) {
            throw RenderException("Failed to create DirectX12 device.")
        }
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    override fun dispose() = synchronized(drawLock) {
        frameDispatcher.cancel()
        super.dispose()
        disposeDevice(device)
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed) { "Direct3DRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "Direct3DRedrawer is disposed" }
        layer.update(System.nanoTime())
        layer.inDrawScope {
            drawAndSwap(withVsync = false)
        }
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private suspend fun draw() {
        layer.inDrawScope {
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

    private fun getAdapterPriority(): Int {
        val adapterPriority = GpuPriority.parse(System.getProperty("skiko.directx.gpu.priority"))
        return when (adapterPriority) {
            GpuPriority.Auto -> 0
            GpuPriority.Integrated -> 1
            GpuPriority.Discrete -> 2
            else -> 0
        }
    }

    fun resizeBuffers(width: Int, height: Int) = resizeBuffers(device, width, height)

    fun getBufferIndex() = getBufferIndex(device)
    fun initSwapChain() = initSwapChain(device)
    fun initFence() = initFence(device)
    val adapterName get() = getAdapterName(device)
    val adapterMemorySize get() = getAdapterMemorySize(device)

    private external fun createDirectXDevice(adapterPriority: Int, contentHandle: Long, transparency: Boolean): Long
    private external fun makeDirectXContext(device: Long): Long
    private external fun makeDirectXSurface(device: Long, context: Long, width: Int, height: Int, index: Int): Long
    private external fun resizeBuffers(device: Long, width: Int, height: Int)
    private external fun swap(device: Long, isVsyncEnabled: Boolean)
    private external fun disposeDevice(device: Long)
    private external fun getBufferIndex(device: Long): Int
    private external fun initSwapChain(device: Long)
    private external fun initFence(device: Long)
    private external fun getAdapterName(device: Long): String
    private external fun getAdapterMemorySize(device: Long): Long
}
