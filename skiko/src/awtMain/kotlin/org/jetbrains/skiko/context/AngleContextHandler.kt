package org.jetbrains.skiko.context

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.AngleRedrawer

internal class AngleContextHandler(layer: SkiaLayer) : JvmContextHandler(layer) {
    val angleRedrawer: AngleRedrawer
        get() = layer.redrawer!! as AngleRedrawer

    var device: Long = 0
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                device = angleRedrawer.createDevice()
                if (device == 0L) {
                    throw RenderException("Failed to create Angle device.")
                }
                context = angleRedrawer.makeContext()
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Angle context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        dispose()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        renderTarget = angleRedrawer.makeRenderTarget(w, h)

        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        ) ?: throw RenderException("Cannot create surface")

        canvas = surface!!.canvas
    }

    override fun flush() {
        super.flush()
        angleRedrawer.finishFrame()
    }
}
