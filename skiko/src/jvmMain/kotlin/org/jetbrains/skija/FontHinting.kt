package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

/**
 * Level of glyph outline adjustment
 */
enum class FontHinting {
    /**
     * glyph outlines unchanged
     */
    NONE,

    /**
     * minimal modification to improve constrast
     */
    SLIGHT,

    /**
     * glyph outlines modified to improve constrast
     */
    NORMAL,

    /**
     * modifies glyph outlines for maximum constrast
     */
    FULL;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}