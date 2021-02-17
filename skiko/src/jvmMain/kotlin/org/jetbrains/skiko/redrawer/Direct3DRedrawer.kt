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

    private var device: Long = 0

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        layer.update(System.nanoTime())
        layer.draw()
    }

    override fun dispose() {
        frameDispatcher.cancel()
        disposeDevice(device)
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

    fun createDevice(): Long {
        device = createDirectXDevice(layer.windowHandle)
        return device
    }

    external fun createDirectXDevice(windowHandle: Long): Long
    external fun makeDirectXContext(device: Long): Long
    external fun makeDirectXRenderTarget(device: Long, width: Int, height: Int): Long
    external fun resizeBuffers(device: Long, width: Int, height: Int)
    external fun finishFrame(device: Long, context: Long, surface: Long)
    external fun disposeDevice(device: Long)
}
