package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

/**
 *
 * Join specifies how corners are drawn when a shape is stroked. Join
 * affects the four corners of a stroked rectangle, and the connected segments in a
 * stroked path.
 *
 *
 * Choose miter join to draw sharp corners. Choose round join to draw a circle with a
 * radius equal to the stroke width on top of the corner. Choose bevel join to minimally
 * connect the thick strokes.
 *
 *
 * The fill path constructed to describe the stroked path respects the join setting but may
 * not contain the actual join. For instance, a fill path constructed with round joins does
 * not necessarily include circles at each connected segment.
 */
enum class PaintStrokeJoin {
    /**
     * extends to miter limit
     */
    MITER,

    /**
     * adds circle
     */
    ROUND,

    /**
     * connects outside edges
     */
    BEVEL;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}