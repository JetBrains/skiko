package org.jetbrains.skiko.context

import kotlinx.cinterop.useContents
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MacOsMetalRedrawer

internal class MacOsMetalContextHandler(layer: SkiaLayer) : ContextHandler(layer, layer::draw) {
    val metalRedrawer: MacOsMetalRedrawer
        get() = layer.redrawer!! as MacOsMetalRedrawer

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
        val w = (layer.nsView.frame.useContents { size.width } * scale).toInt().coerceAtLeast(0)
        val h = (layer.nsView.frame.useContents { size.height } * scale).toInt().coerceAtLeast(0)

        renderTarget = metalRedrawer.makeRenderTarget(w, h)

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

