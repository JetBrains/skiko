package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.DirectContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.useDrawingSurfacePlatformInfo
import javax.swing.SwingUtilities.convertPoint
import javax.swing.SwingUtilities.getRootPane

internal class MetalRedrawer(
    private val layer: HardwareLayer,
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
        frameDispatcher.scheduleFrame()
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        layer.draw()
    }

    override fun syncSize() {
        println("syncsize")
        val globalPosition = convertPoint(layer, layer.x, layer.y, getRootPane(layer))
        setContentScale(device, layer.contentScale)
        resizeLayers(device,
            globalPosition.x,
            globalPosition.y,
            layer.width.coerceAtLeast(0),
            layer.height.coerceAtLeast(0)
        )
    }

    fun makeContext(device: Long) = DirectContext(
        makeMetalContext(device)
    )

    fun makeRenderTarget(device: Long, width: Int, height: Int) = BackendRenderTarget(
        makeMetalRenderTarget(device, width, height)
    )

    fun createDevice(): Long {
        device = layer.useDrawingSurfacePlatformInfo(::createMetalDevice)
        return device
    }

    private external fun createMetalDevice(platformInfo: Long): Long
    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    private external fun disposeDevice(device: Long)
    external fun finishFrame(device: Long)
    external fun resizeLayers(device: Long, x: Int, y: Int, width: Int, height: Int)
    external fun setContentScale(device: Long, contentScale: Float)
}
