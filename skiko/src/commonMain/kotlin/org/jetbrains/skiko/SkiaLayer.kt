package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

expect open class SkiaLayer {
    var renderApi: GraphicsApi
    val contentScale: Float
    var fullscreen: Boolean
    var transparency: Boolean

    var app: SkikoApp?

    fun needRedraw()
}

fun SkiaLayer.setApp(app: SkikoApp) {
    this.app = app
}

internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)
