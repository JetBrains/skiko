package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

/**
 * Whether edge pixels draw opaque or with partial transparency.
 */
enum class FontEdging {
    /**
     * no transparent pixels on glyph edges
     */
    ALIAS,

    /**
     * may have transparent pixels on glyph edges
     */
    ANTI_ALIAS,

    /**
     * glyph positioned in pixel using transparency
     */
    SUBPIXEL_ANTI_ALIAS;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}