package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class PathDirection internal constructor(val ordinal: Int) {
    companion object {
        /** Clockwise direction for adding closed contours.  */
        val CLOCKWISE = PathDirection(0)

        /** Counter-clockwise direction for adding closed contours.  */
        val COUNTER_CLOCKWISE = PathDirection(1)
    }
}