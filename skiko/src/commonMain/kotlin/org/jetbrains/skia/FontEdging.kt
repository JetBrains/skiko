package org.jetbrains.skia

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
}