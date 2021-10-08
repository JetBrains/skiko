package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

expect open class SkiaLayer {
    var renderApi: GraphicsApi
    val contentScale: Float
}

internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)
