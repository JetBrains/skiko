package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

interface SkiaRenderer {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

expect open class SkiaLayer {
    internal val backedLayer: HardwareLayer
    var renderApi: GraphicsApi
}

internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)
