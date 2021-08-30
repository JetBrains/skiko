package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class FilterTileMode {
    /** Replicate the edge color if the shader draws outside of its original bounds.  */
    CLAMP,

    /** Repeat the shader's image horizontally and vertically.  */
    REPEAT,

    /** Repeat the shader's image horizontally and vertically, alternating mirror images so that adjacent images always seam.  */
    MIRROR,

    /** Only draw within the original domain, return transparent-black everywhere else.  */
    DECAL;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}