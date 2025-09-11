package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.AngleApi
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.AngleRedrawer

internal class AngleContextHandler(layer: SkiaLayer) : ContextBasedContextHandler(layer, "ANGLE") {
    private val angleRedrawer: AngleRedrawer
        get() = layer.redrawer!! as AngleRedrawer

    override fun makeContext() = angleRedrawer.makeContext()

    private var currentWidth = 0
    private var currentHeight = 0
    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    override fun initCanvas() {
        val context = context ?: return
        val scale = layer.contentScale

        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        if (isSizeChanged(w, h) || surface == null) {
            disposeCanvas()
            context.flush()

            renderTarget = angleRedrawer.makeRenderTarget(w, h)
            surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = layer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")
        }

        canvas = surface!!.canvas
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Vendor: ${AngleApi.glGetString(AngleApi.GL_VENDOR)}\n" +
                "Model: ${AngleApi.glGetString(AngleApi.GL_RENDERER)}\n" +
                "Version: ${AngleApi.glGetString(AngleApi.GL_VERSION)}\n"
                // "Total VRAM: ${AngleApi.glGetIntegerv(AngleApi.GL_TOTAL_MEMORY) / 1024} MB\n"
    }
}
