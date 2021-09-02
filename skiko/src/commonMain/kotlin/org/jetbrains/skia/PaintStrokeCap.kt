package org.jetbrains.skia

/**
 * Cap draws at the beginning and end of an open path contour.
 */
enum class PaintStrokeCap {
    /**
     * No stroke extension
     */
    BUTT,

    /**
     * adds circle
     */
    ROUND,

    /**
     * adds square
     */
    SQUARE;
}