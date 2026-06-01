package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class PathEllipseArc internal constructor(val ordinal: Int) {
    companion object {
        /** Smaller of arc pair.  */
        val SMALLER = PathEllipseArc(0)

        /** Larger of arc pair.  */
        val LARGER = PathEllipseArc(1)
    }
}