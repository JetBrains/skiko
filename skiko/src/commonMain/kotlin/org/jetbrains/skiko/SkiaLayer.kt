package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PixelGeometry

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
     * The color, in ARGB format, with which the layer is cleared before rendering.
     */
    var backgroundColor: Int

    /**
     * Underlying platform component.
     */
    val component: Any?

    /**
     * Current view used for rendering.
     */
    var renderDelegate: SkikoRenderDelegate?

    /**
     * Attach this [SkikoRenderDelegate] to platform container.
     * Actual type of attach container is platform-specific.
     */
    fun attachTo(container: Any)

    /**
     * Detach this [SkikoRenderDelegate] from platform container.
     */
    fun detach()

    /**
     * Request redrawing of the content; The [renderDelegate] will be asked to re-render, and the result will be drawn
     * on the screen.
     *
     * @param throttledToVsync Whether to throttle calling [renderDelegate]'s [SkikoRenderDelegate.onRender] to at most
     * once between vsync signals (if vsync is enabled).
     */
    fun needRender(throttledToVsync: Boolean = true)

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    fun needRedraw()  // TODO: Remove this sometime after 2026-07

    /**
     * Drawing function.
     */
    internal fun draw(canvas: Canvas)
}


internal class PictureHolder(val instance: Picture, val width: Int, val height: Int)

