package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.Surface
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

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        update(System.nanoTime())
        draw()
    }

    override fun dispose() = synchronized(disposeLock) {
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
                    }
                }
            }
        }
    }

    fun makeContext(device: Long) = DirectContext(
        makeDirectXContext(device)
    )

    fun makeSurface(device: Long, context: Long, width: Int, height: Int, index: Int) = Surface(
        makeDirectXSurface(device, context, width, height, index)
    )

    fun createDevice(): Long = createDirectXDevice(getAdapterPriority(), layer.contentHandle)

    fun finishFrame(device: Long, context: Long, surface: Long) {
        finishFrame(device, context, surface, properties.isVsyncEnabled)
    }

    fun getAdapterPriority(): Int {
        val adapterPriority = GpuPriority.parse(System.getProperty("skiko.directx.gpu.priority"))
        return when (adapterPriority) {
            GpuPriority.Auto -> 0
            GpuPriority.Integrated -> 1
            GpuPriority.Discrete -> 2
            else -> 0
        }
    }

    external fun createDirectXDevice(adapterPriority: Int, contentHandle: Long): Long
    external fun makeDirectXContext(device: Long): Long
    external fun makeDirectXSurface(device: Long, context: Long, width: Int, height: Int, index: Int): Long
    external fun resizeBuffers(device: Long, width: Int, height: Int)
    private external fun finishFrame(device: Long, context: Long, surface: Long, isVsyncEnabled: Boolean)
    external fun disposeDevice(device: Long)
    external fun getBufferIndex(device: Long): Int
    external fun initSwapChain(device: Long)
    external fun initFence(device: Long)
    external fun getAdapterName(device: Long): String
    external fun getAdapterMemorySize(device: Long): Long
}
