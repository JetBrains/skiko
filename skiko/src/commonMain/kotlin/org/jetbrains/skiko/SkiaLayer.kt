package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

expect open class SkiaLayer {
    var renderApi: GraphicsApi
    val contentScale: Float
    var fullscreen: Boolean
    var transparency: Boolean

    var renderer: SkiaRenderer?

    fun needRedraw()
}

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

open class GenericRenderer(
    val layer: SkiaLayer,
    val app: SkiaRenderer
): SkiaRenderer {
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        app.onRender(canvas, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)
        layer.needRedraw()
    }
}

internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)
