package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

expect open class SkiaLayer(properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties()) {
    var renderApi: GraphicsApi
    val contentScale: Float
    var fullscreen: Boolean
    var transparency: Boolean

    fun needRedraw()
}

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

open class GenericRenderer(
    val layer: SkiaLayer,
    val displayScene: (Canvas, Int, Int, Long) -> Unit
): SkiaRenderer {
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        displayScene(canvas, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)
        layer.needRedraw()
    }
}

internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)
