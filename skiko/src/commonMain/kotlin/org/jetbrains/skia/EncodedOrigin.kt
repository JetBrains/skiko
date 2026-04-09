package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class EncodedOrigin internal constructor(val ordinal: Int) {
    companion object {
        /**
         * Do not use
         */
        val UNUSED = EncodedOrigin(0)

        /**
         * Default
         */
        val TOP_LEFT = EncodedOrigin(1)

        /**
         * Reflected across y-axis
         */
        val TOP_RIGHT = EncodedOrigin(2)

        /**
         * Rotated 180
         */
        val BOTTOM_RIGHT = EncodedOrigin(3)

        /**
         * Reflected across x-axis
         */
        val BOTTOM_LEFT = EncodedOrigin(4)

        /**
         * Reflected across x-axis, Rotated 90 CCW
         */
        val LEFT_TOP = EncodedOrigin(5)

        /**
         * Rotated 90 CW
         */
        val RIGHT_TOP = EncodedOrigin(6)

        /**
         * Reflected across x-axis, Rotated 90 CW
         */
        val RIGHT_BOTTOM = EncodedOrigin(7)

        /**
         * Rotated 90 CCW
         */
        val LEFT_BOTTOM = EncodedOrigin(8)
    }

    /**
     * Given an encoded origin and the width and height of the source data, returns a matrix
     * that transforms the source rectangle with upper left corner at [0, 0] and origin to a correctly
     * oriented destination rectangle of [0, 0, w, h].
     */
    fun toMatrix(w: Int, h: Int): Matrix33 {
        return when (this) {
            TOP_LEFT -> Matrix33.IDENTITY
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