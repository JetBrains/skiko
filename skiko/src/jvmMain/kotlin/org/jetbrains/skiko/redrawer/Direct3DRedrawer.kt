package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.DirectContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer

internal class Direct3DRedrawer(
    private val layer: HardwareLayer
) : Redrawer {

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        layer.update(System.nanoTime())
        layer.draw()
    }

    override fun dispose() {
        frameDispatcher.cancel()
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        layer.update(System.nanoTime())
        layer.draw()
    }

    fun makeContext(device: Long) = DirectContext(
        makeDirectXContext(device)
    )

    fun makeRenderTarget(device: Long, width: Int, height: Int) = BackendRenderTarget(
        makeDirectXRenderTarget(device, width, height)
    )

    external fun createDevice(): Long
    external fun makeDirectXContext(device: Long): Long
    external fun makeDirectXRenderTarget(device: Long, width: Int, height: Int): Long
    external fun createSwapChain(windowHandle: Long, device: Long)
    external fun resizeBuffers(device: Long, width: Int, height: Int)
    external fun finishFrame(device: Long, context: Long, surface: Long)
}
