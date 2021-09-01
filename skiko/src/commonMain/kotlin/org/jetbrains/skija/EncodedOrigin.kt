package org.jetbrains.skija

enum class EncodedOrigin {
    _UNUSED,

    /**
     * Default
     */
    TOP_LEFT,

    /**
     * Reflected across y-axis
     */
    TOP_RIGHT,

    /**
     * Rotated 180
     */
    BOTTOM_RIGHT,

    /**
     * Reflected across x-axis
     */
    BOTTOM_LEFT,

    /**
     * Reflected across x-axis, Rotated 90 CCW
     */
    LEFT_TOP,

    /**
     * Rotated 90 CW
     */
    RIGHT_TOP,

    /**
     * Reflected across x-axis, Rotated 90 CW
     */
    RIGHT_BOTTOM,

    /**
     * Rotated 90 CCW
     */
    LEFT_BOTTOM;

    /**
     * Given an encoded origin and the width and height of the source data, returns a matrix
     * that transforms the source rectangle with upper left corner at [0, 0] and origin to a correctly
     * oriented destination rectangle of [0, 0, w, h].
     */
    fun toMatrix(w: Int, h: Int): Matrix33 {
        return when (this) {
            TOP_LEFT -> Matrix33.Companion.IDENTITY
            TOP_RIGHT -> Matrix33(-1f, 0f, w.toFloat(), 0f, 1f, 0f, 0f, 0f, 1f)
            BOTTOM_RIGHT -> Matrix33(-1f, 0f, w.toFloat(), 0f, -1f, h.toFloat(), 0f, 0f, 1f)
            BOTTOM_LEFT -> Matrix33(1f, 0f, 0f, 0f, -1f, h.toFloat(), 0f, 0f, 1f)
            LEFT_TOP -> Matrix33(0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
            RIGHT_TOP -> Matrix33(0f, -1f, w.toFloat(), 1f, 0f, 0f, 0f, 0f, 1f)
            RIGHT_BOTTOM -> Matrix33(0f, -1f, w.toFloat(), -1f, 0f, h.toFloat(), 0f, 0f, 1f)
            LEFT_BOTTOM -> Matrix33(0f, 1f, 0f, -1f, 0f, h.toFloat(), 0f, 0f, 1f)
            else -> throw IllegalArgumentException("Unsupported origin $this")
        }
    }

    /**
     * Return true if the encoded origin includes a 90 degree rotation, in which case the width
     * and height of the source data are swapped relative to a correctly oriented destination.
     */
    fun swapsWidthHeight(): Boolean {
        return when (this) {
            LEFT_TOP, RIGHT_TOP, RIGHT_BOTTOM, LEFT_BOTTOM -> true
            else -> false
        }
    }
}