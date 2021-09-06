package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.GpuPriority
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties

internal class Direct3DRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {

    private var isDisposed = false
    private var disposeLock = Any()

    private val device = createDirectXDevice(getAdapterPriority(), layer.contentHandle).also {
        if (it == 0L) {
            throw Exception("Failed to create DirectX12 device.")
        }
    }

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        update(System.nanoTime())
        draw()
    }

    override fun dispose() = synchronized(disposeLock) {
        disposeDevice(device)
        frameDispatcher.cancel()
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed) { "Direct3DRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override suspend fun awaitRedraw(): Boolean {
        return frameDispatcher.awaitFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "Direct3DRedrawer is disposed" }
        // TODO now we wait until previous layer.draw is finished. it ends only on the next vsync.
        //  because of that we lose one frame on resize and can theoretically see very small white bars on the sides of the window
        //  to avoid this we should be able to draw in two modes: with vsync and without.
        frameDispatcher.scheduleFrame()
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private suspend fun draw() {
        if (layer.prepareDrawContext()) {
            withContext(Dispatchers.IO) {
                synchronized(disposeLock) {
                    if (!isDisposed) {
                        layer.draw()
                        swap(device, properties.isVsyncEnabled)
                    }
                }
            }
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
    fun getAdapterName() = getAdapterName(device)
    fun getAdapterMemorySize() = getAdapterMemorySize(device)

    private external fun createDirectXDevice(adapterPriority: Int, contentHandle: Long): Long
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
