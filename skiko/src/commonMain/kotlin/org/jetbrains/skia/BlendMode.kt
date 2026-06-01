package org.jetbrains.skia

import kotlin.jvm.JvmInline

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
@JvmInline
value class BlendMode internal constructor(val ordinal: Int) {
    companion object {

        /** Replaces destination with zero: fully transparent. r = 0 */
        val CLEAR = BlendMode(0)
        /** Replaces destination. r = s */
        val SRC = BlendMode(1)
        /** Preserves destination. r = d */
        val DST = BlendMode(2)
        /** Source over destination. r = s + (1-sa)*d */
        val SRC_OVER = BlendMode(3)
        /** Destination over source. r = d + (1-da)*s */
        val DST_OVER = BlendMode(4)
        /** Source trimmed inside destination. r = s * da */
        val SRC_IN = BlendMode(5)
        /** Destination trimmed by source. r = d * sa */
        val DST_IN = BlendMode(6)
        /** Source trimmed outside destination. r = s * (1-da) */
        val SRC_OUT = BlendMode(7)
        /** Destination trimmed outside source. r = d * (1-sa) */
        val DST_OUT = BlendMode(8)
        /** Source inside destination blended with destination. r = s*da + d*(1-sa) */
        val SRC_ATOP = BlendMode(9)
        /** Destination inside source blended with source. r = d*sa + s*(1-da) */
        val DST_ATOP = BlendMode(10)
        /** Each of source and destination trimmed outside the other. r = s*(1-da) + d*(1-sa) */
        val XOR = BlendMode(11)
        /** Sum of colors. r = min(s + d, 1) */
        val PLUS = BlendMode(12)
        /** Product of premultiplied colors; darkens destination. r = s*d */
        val MODULATE = BlendMode(13)
        /** Multiply inverse of pixels, inverting result; brightens destination. r = s + d - s*d */
        val SCREEN = BlendMode(14)
        /** Multiply or screen, depending on destination.  */
        val OVERLAY = BlendMode(15)
        /** Darker of source and destination. rc = s + d - max(s*da, d*sa), ra = kSrcOver */
        val DARKEN = BlendMode(16)
        /** Lighter of source and destination. rc = s + d - min(s*da, d*sa), ra = kSrcOver */
        val LIGHTEN = BlendMode(17)
        /** Brighten destination to reflect source.  */
        val COLOR_DODGE = BlendMode(18)
        /** Darken destination to reflect source.  */
        val COLOR_BURN = BlendMode(19)
        /** Multiply or screen, depending on source.  */
        val HARD_LIGHT = BlendMode(20)
        /** Lighten or darken, depending on source.  */
        val SOFT_LIGHT = BlendMode(21)
        /** Subtract darker from lighter with higher contrast. rc = s + d - 2*(min(s*da, d*sa)), ra = kSrcOver */
        val DIFFERENCE = BlendMode(22)
        /** Subtract darker from lighter with lower contrast. rc = s + d - two(s*d), ra = kSrcOver */
        val EXCLUSION = BlendMode(23)
        /** Multiply source with destination, darkening image. r = s*(1-da) + d*(1-sa) + s*d */
        val MULTIPLY = BlendMode(24)
        /** Hue of source with saturation and luminosity of destination.  */
        val HUE = BlendMode(25)
        /** Saturation of source with hue and luminosity of destination.  */
        val SATURATION = BlendMode(26)
        /** Hue and saturation of source with luminosity of destination.  */
        val COLOR = BlendMode(27)
        /** Luminosity of source with hue and saturation of destination.  */
        val LUMINOSITY = BlendMode(28)

    }
}