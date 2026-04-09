package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 * Verb instructs [Path] how to interpret one or more [Point] values and an optional conic weight,
 * manage contours, and terminate a [Path].
 */
@JvmInline
value class PathVerb internal constructor(val ordinal: Int) {

    companion object {
        /**
         * [PathSegmentIterator.next] returns 1 point.
         */
        val MOVE = PathVerb(0)

        /**
         * [PathSegmentIterator.next] returns 2 points.
         */
        val LINE = PathVerb(1)

        /**
         * [PathSegmentIterator.next] returns 3 points.
         */
        val QUAD = PathVerb(2)

        /**
         * [PathSegmentIterator.next] returns 3 points plus [PathSegmentIterator.conicWeight].
         */
        val CONIC = PathVerb(3)

        /**
         * [PathSegmentIterator.next] returns 4 points.
         */
        val CUBIC = PathVerb(4)

        /**
         * [PathSegmentIterator.next] returns 1 point, the contour's move-to point.
         */
        val CLOSE = PathVerb(5)

        /**
         * [PathSegmentIterator.next] returns 0 points.
         */
        val DONE = PathVerb(6)
    }
}