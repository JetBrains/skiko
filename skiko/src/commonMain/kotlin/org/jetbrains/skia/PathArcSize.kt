package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 * Enum for choosing smaller or larger arc when adding arcs to paths.
 */
@JvmInline
value class PathArcSize internal constructor(val ordinal: Int) {
    companion object {
        /** Smaller of arc pair */
        val SMALL = PathArcSize(0)

        /** Larger of arc pair */
        val LARGE = PathArcSize(1)
    }
}