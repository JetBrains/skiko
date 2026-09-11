package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ClipMode

/**
 * Rectangle used for clipping.
 */
interface ClipRectangle {
    val x: Float
    val y: Float
    val width: Float
    val height: Float
}

/**
 * Returns a [ClipRectangle] with the specified values.
 */
internal fun ClipRectangle(x: Float, y: Float, width: Float, height: Float) = object : ClipRectangle {
    override val x: Float = x
    override val y: Float = y
    override val width: Float = width
    override val height: Float = height
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Canvas.cutoutFromClip(rect: ClipRectangle, scale: Float) {
    val x = rect.x
    val y = rect.y
    clipRect(
        left = x * scale,
        top = y * scale,
        right = (x + rect.width) * scale,
        bottom = (y + rect.height) * scale,
        mode = ClipMode.DIFFERENCE,
        antiAlias = true
    )
}
