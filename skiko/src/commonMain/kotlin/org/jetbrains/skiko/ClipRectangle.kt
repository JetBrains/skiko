package org.jetbrains.skiko

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