package org.jetbrains.skiko

import org.jetbrains.skia.Picture

expect open class SkiaLayer {
    var renderApi: GraphicsApi
    val contentScale: Float
    var fullscreen: Boolean
    var transparency: Boolean

    var skikoView: SkikoView?

    // Actual type of attach container is platform-specific.
    fun attachTo(container: Any)
    fun detach()

    fun needRedraw()
}


internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)
