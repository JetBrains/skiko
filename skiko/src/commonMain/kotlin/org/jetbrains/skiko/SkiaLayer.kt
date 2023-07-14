package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PixelGeometry

expect class SkiaLayerContainer

interface SkiaLayerInterface {
    var skikoView: SkikoView?
    fun needRedraw()
    fun attachTo(container: SkiaLayerContainer)
    fun detach()
}

/**
 * Generic layer for Skiko rendering.
 */
expect class SkiaLayer: SkiaLayerInterface {
    /**
     * Current graphics API used for rendering.
     */
    var renderApi: GraphicsApi

    /**
     * Current content scale.
     */
    val contentScale: Float

    /**
     * Pixel geometry corresponding to graphics device which renders this layer
     */
    val pixelGeometry: PixelGeometry

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
    override var skikoView: SkikoView?

    /**
     * Attach this SkikoView to platform container.
     * Actual type of attach container is platform-specific.
     */
    override fun attachTo(container: SkiaLayerContainer)

    /**
     * Detach this SkikoView from platform container.
     */
    override fun detach()

    /**
     * Force redraw.
     */
    override fun needRedraw()

    /**
     * Drawing function.
     */
    internal fun draw(canvas: Canvas)
}


internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)

