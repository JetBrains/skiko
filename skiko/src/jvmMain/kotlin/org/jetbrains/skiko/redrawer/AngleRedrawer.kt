package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.context.AngleContextHandler
import org.jetbrains.skiko.context.DirectSoftwareContextHandler

internal class AngleRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    private val contextHandler = AngleContextHandler(layer)
    override val renderInfo: String get() = contextHandler.rendererInfo()

    private var isDisposed = false
    private var device: Long = 0

    private val frameDispatcher = FrameDispatcher(Dispatchers.Swing) {
        if (layer.isShowing) {
            update(System.nanoTime())
            draw()
        }
    }

    override fun dispose() {
        frameDispatcher.cancel()
        contextHandler.dispose()
        disposeDevice(device)
        isDisposed = true
    }

    override fun needRedraw() {
        check(!isDisposed) { "AngleRedrawer is disposed" }
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        check(!isDisposed) { "AngleRedrawer is disposed" }
        update(System.nanoTime())
        draw()
    }

    private fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    private fun draw() {
        contextHandler.draw()
    }

    fun createDevice(): Long {
        device = createAngleDevice(layer.contentHandle)
        return device
    }

    fun makeContext() = DirectContext(
        makeAngleContext(device)
    )

    fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeAngleRenderTarget(device, width, height)
    )

    fun finishFrame() = finishFrame(device, properties.isVsyncEnabled)

    external fun createAngleDevice(contentHandle: Long): Long
    external fun makeAngleContext(device: Long): Long
    external fun makeAngleRenderTarget(device: Long, width: Int, height: Int): Long
    external fun resizeBuffers(device: Long, width: Int, height: Int)
    external fun finishFrame(device: Long, isVsyncEnabled: Boolean)
    external fun disposeDevice(device: Long)
}
