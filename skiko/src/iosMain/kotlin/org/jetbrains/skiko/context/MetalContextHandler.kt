package org.jetbrains.skiko.context

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MetalRedrawer

internal class MetalContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    val metalRedrawer: MetalRedrawer
        get() = layer.redrawer!!

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = metalRedrawer.makeContext()
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Metal context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        renderTarget = metalRedrawer.makeRenderTarget(w, h) ?:
            throw IllegalArgumentException("Cannot create Metal render target")

        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB
        )

        canvas = surface!!.canvas
    }

    override fun flush() {
        // TODO: maybe make flush async as in JVM version.
        super.flush()
        surface!!.flushAndSubmit()
        metalRedrawer.finishFrame()
    }

   override fun rendererInfo(): String {
        return "Native Metal: device ${metalRedrawer.device.name}"
    }
}

