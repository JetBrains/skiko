package org.jetbrains.skiko.context

import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.makeMetalContext
import org.jetbrains.skiko.makeMetalRenderTarget
import org.jetbrains.skiko.redrawer.Redrawer

internal class MetalContextHandler(layer: HardwareLayer) : ContextHandler(layer) {
    override fun initContext(redrawer: Redrawer): Boolean {
        try {
            if (context == null) {
                context = makeMetalContext()
            }
        } catch (e: Exception) {
            println("Failed to create Skia Metal context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        dispose()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        renderTarget = makeMetalRenderTarget(w, h, 0)

        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.getSRGB()
        )

        canvas = surface!!.canvas
    }
}
