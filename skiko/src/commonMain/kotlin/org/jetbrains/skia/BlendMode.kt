package org.jetbrains.skia

/**
 *  Blends are operators that take in two colors (source, destination) and return a new color.
 *  Many of these operate the same on all 4 components: red, green, blue, alpha. For these,
 *  we just document what happens to one component, rather than naming each one separately.
 *
 *  Different SkColorTypes have different representations for color components:
 *      8-bit: 0..255
 *      6-bit: 0..63
 *      5-bit: 0..31
 *      4-bit: 0..15
 *     floats: 0...1
 *
 *  The documentation is expressed as if the component values are always 0..1 (floats).
 *
 *  For brevity, the documentation uses the following abbreviations
 *  s  : source
 *  d  : destination
 *  sa : source alpha
 *  da : destination alpha
 *
 *  Results are abbreviated
 *  r  : if all 4 components are computed in the same manner
 *  ra : result alpha component
 *  rc : result "color": red, green, blue components
 */
enum class BlendMode {
    /** Replaces destination with zero: fully transparent. r = 0 */
    CLEAR,

    /** Replaces destination. r = s */
    SRC,

    /** Preserves destination. r = d */
    DST,

    /** Source over destination. r = s + (1-sa)*d */
    SRC_OVER,

    /** Destination over source. r = d + (1-da)*s */
    DST_OVER,

    /** Source trimmed inside destination. r = s * da */
    SRC_IN,

    /** Destination trimmed by source. r = d * sa */
    DST_IN,

    /** Source trimmed outside destination. r = s * (1-da) */
    SRC_OUT,

    /** Destination trimmed outside source. r = d * (1-sa) */
    DST_OUT,

    /** Source inside destination blended with destination. r = s*da + d*(1-sa) */
    SRC_ATOP,

    /** Destination inside source blended with source. r = d*sa + s*(1-da) */
    DST_ATOP,

    /** Each of source and destination trimmed outside the other. r = s*(1-da) + d*(1-sa) */
    XOR,

    /** Sum of colors. r = min(s + d, 1) */
    PLUS,

    /** Product of premultiplied colors; darkens destination. r = s*d */
    MODULATE,

    /** Multiply inverse of pixels, inverting result; brightens destination. r = s + d - s*d */
    SCREEN,

    /** Multiply or screen, depending on destination.  */
    OVERLAY,

    /** Darker of source and destination. rc = s + d - max(s*da, d*sa), ra = kSrcOver */
    DARKEN,

    /** Lighter of source and destination. rc = s + d - min(s*da, d*sa), ra = kSrcOver */
    LIGHTEN,

    /** Brighten destination to reflect source.  */
    COLOR_DODGE,

    /** Darken destination to reflect source.  */
    COLOR_BURN,

    /** Multiply or screen, depending on source.  */
    HARD_LIGHT,

    /** Lighten or darken, depending on source.  */
    SOFT_LIGHT,

    /** Subtract darker from lighter with higher contrast. rc = s + d - 2*(min(s*da, d*sa)), ra = kSrcOver */
    DIFFERENCE,

    /** Subtract darker from lighter with lower contrast. rc = s + d - two(s*d), ra = kSrcOver */
    EXCLUSION,

    /** Multiply source with destination, darkening image. r = s*(1-da) + d*(1-sa) + s*d */
    MULTIPLY,

    /** Hue of source with saturation and luminosity of destination.  */
    HUE,

    /** Saturation of source with hue and luminosity of destination.  */
    SATURATION,

    /** Hue and saturation of source with luminosity of destination.  */
    COLOR,

    /** Luminosity of source with hue and saturation of destination.  */
    LUMINOSITY;
}