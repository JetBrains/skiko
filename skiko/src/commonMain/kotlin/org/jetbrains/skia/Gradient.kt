package org.jetbrains.skia

/**
 * Specification for the colors in a gradient.
 */
class Gradient(
    val colors: Colors,
    val interpolation: Interpolation = Interpolation()
) {
    /**
     * Specification for the colors in a gradient.
     *
     * @param colors  The span of colors for the gradient.
     * @param positions Relative positions of each color across the gradient. If empty,
     *                  the the colors are distributed evenly. If this is not null, the values
     *                  must lie between 0.0 and 1.0, and be strictly increasing. If the first
     *                  value is not 0.0, then an additional color stop is added at position 0.0,
     *                  with the same color as colors[0]. If the the last value is less than 1.0,
     *                  then an additional color stop is added at position 1.0, with the same color
     *                  as colors[count - 1].
     * @param tileMode Tiling mode for the gradient.
     * @param colorSpace Optional colorspace associated with the span of colors. If this is null,
     *                   the colors are treated as sRGB.
     */
    class Colors(
        val colors: Array<Color4f>,
        val positions: FloatArray? = null,
        val tileMode: FilterTileMode,
        val colorSpace: ColorSpace? = null
    ) {
        init {
            require(positions == null || colors.size == positions.size) {
                "colors.length ${colors.size} != positions.length ${positions!!.size}"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other !is Colors) return false
            if (!colors.contentEquals(other.colors)) return false
            if (!(positions?.contentEquals(other.positions) ?: (other.positions == null))) return false
            if (tileMode != other.tileMode) return false
            return colorSpace == other.colorSpace
        }

        override fun hashCode(): Int {
            val prime = 59
            var result = 1
            result = result * prime + colors.contentHashCode()
            result = result * prime + (positions?.contentHashCode() ?: 43)
            result = result * prime + tileMode.hashCode()
            result = result * prime + (colorSpace?.hashCode() ?: 43)
            return result
        }
    }

    /**
     * Description of the colors and interpolation method.
     */
    class Interpolation(
        val inPremul: InPremul = InPremul.NO,
        val colorSpace: ColorSpace = ColorSpace.DESTINATION,
        val hueMethod: HueMethod = HueMethod.SHORTER
    ) {
        enum class InPremul {
            NO,
            YES
        }

        enum class ColorSpace {
            // Default Skia behavior: interpolate in the color space of the destination surface
            DESTINATION,
            // https://www.w3.org/TR/css-color-4/#interpolation-space
            SRGB_LINEAR,
            LAB,
            OKLAB,
            // This is the same as OKLAB, except it has a simplified version of the CSS gamut
            // mapping algorithm (https://www.w3.org/TR/css-color-4/#css-gamut-mapping)
            // into REC2020 space applied to it.
            // Warning: This space is experimental and should not be used in production.
            OKLAB_GAMUT_MAP,
            LCH,
            OKLCH,
            // This is the same as OKLCH, except it has the same gamut mapping applied to it
            // as OKLAB_GAMUT_MAP does.
            // Warning: This space is experimental and should not be used in production.
            OKLCH_GAMUT_MAP,
            SRGB,
            HSL,
            HWB,
            DISPLAY_P3,
            REC2020,
            PROPHOTO_RGB,
            A98_RGB
        }

        enum class HueMethod {
            // https://www.w3.org/TR/css-color-4/#hue-interpolation
            SHORTER,
            LONGER,
            INCREASING,
            DECREASING
        }

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other !is Interpolation) return false
            if (inPremul != other.inPremul) return false
            if (colorSpace != other.colorSpace) return false
            return hueMethod == other.hueMethod
        }

        override fun hashCode(): Int {
            val prime = 59
            var result = 1
            result = result * prime + inPremul.hashCode()
            result = result * prime + colorSpace.hashCode()
            result = result * prime + hueMethod.hashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Gradient) return false
        if (colors != other.colors) return false
        return interpolation == other.interpolation
    }

    override fun hashCode(): Int {
        val prime = 59
        var result = 1
        result = result * prime + colors.hashCode()
        result = result * prime + interpolation.hashCode()
        return result
    }
}
