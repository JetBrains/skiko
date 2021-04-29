package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.DirectContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties

internal class AngleRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {

    private var isDisposed = false
    private var device: Long = 0

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        update(System.nanoTime())
        draw()
    }

    override fun dispose() {
        frameDispatcher.cancel()
        disposeDevice(device)
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed)
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed)
        update(System.nanoTime())
        draw()
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        layer.draw()
    }

    fun createDevice(): Long {
        device = createAngleDevice(layer.windowHandle)
        return device
    }

    fun makeContext() = DirectContext(
        makeAngleContext(device)
    )

    fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeAngleRenderTarget(device, width, height)
    )

    fun finishFrame() = finishFrame(device, properties.isVsyncEnabled)

    external fun createAngleDevice(windowHandle: Long): Long
    external fun makeAngleContext(device: Long): Long
    external fun makeAngleRenderTarget(device: Long, width: Int, height: Int): Long
    external fun resizeBuffers(device: Long, width: Int, height: Int)
    external fun finishFrame(device: Long, isVsyncEnabled: Boolean)
    external fun disposeDevice(device: Long)
}
