package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

/**
 * Generic layer for Skiko rendering.
 */
expect open class SkiaLayer {
    /**
     * Current graphics API used for rendering.
     */
    var renderApi: GraphicsApi

    /**
     * Current content scale.
     */
    val contentScale: Float

    /**
     * If rendering is full screen.
     */
    var fullscreen: Boolean
    /**
     * If transparency is enabled.
     */
    var transparency: Boolean

    /**
     * Underlying platform component.
     */
    val component: Any?

    /**
     * Current view used for rendering.
     */
    var skikoView: SkikoView?

    /**
     * Attach this SkikoView to platform container.
     * Actual type of attach container is platform-specific.
     */
    fun attachTo(container: Any)

    /**
     * Detach this SkikoView from platform container.
     */
    fun detach()

    /**
     * Force redraw.
     */
    fun needRedraw()

    /**
     * Drawing function.
     */
    internal fun draw(canvas: Canvas)
}


internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)

