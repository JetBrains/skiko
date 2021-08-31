package org.jetbrains.skija

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
        internal val _values = values()
    }
}