package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 * Cap draws at the beginning and end of an open path contour.
 */
@JvmInline
value class PaintStrokeCap internal constructor(val ordinal: Int) {
    companion object {
        /**
         * No stroke extension
         */
        val BUTT = PaintStrokeCap(0)

        /**
         * adds circle
         */
        val ROUND = PaintStrokeCap(1)

        /**
         * adds square
         */
        val SQUARE = PaintStrokeCap(2)
    }
}