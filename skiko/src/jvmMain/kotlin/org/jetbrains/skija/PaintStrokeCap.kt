package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

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

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}