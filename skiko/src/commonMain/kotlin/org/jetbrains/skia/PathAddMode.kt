package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 * PathAddMode chooses how addPath() appends paths.
 * Adding one Path to another can extend the last contour or start a new contour.
 */
@JvmInline
value class PathAddMode internal constructor(val ordinal: Int) {
    companion object {
        /**
         * Contours are appended to the destination path as new contours.
         */
        val APPEND = PathAddMode(0)

        /**
         * Extends the last contour of the destination path with the first contour
         * of the source path, connecting them with a line.
         */
        val EXTEND = PathAddMode(1)
    }
}
