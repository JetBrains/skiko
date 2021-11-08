package org.jetbrains.skia.shaper

import org.jetbrains.skia.FontFeature
import org.jetbrains.skia.FontMgr

class ShapingOptions(
    internal val fontMgr: FontMgr?,
    internal val features: Array<FontFeature>?,
    internal val isLeftToRight: Boolean,
    /**
     * If enabled, fallback font runs will not be broken by whitespace from original font
     */
    internal val isApproximateSpaces: Boolean,
    /**
     * If enabled, fallback font runs will not be broken by punctuation from original font
     */
    internal val isApproximatePunctuation: Boolean
) {

    internal fun _booleanPropsToInt(): Int {
        var i = 0
        if (isLeftToRight) i = i or 0x04
        if (isApproximateSpaces) i = i or 0x02
        if (isApproximatePunctuation) i = i or 0x01
        return i
    }

    /**
     * If enabled, fallback font runs will not be broken by whitespace from original font
     */
    /**
     * If enabled, fallback font runs will not be broken by punctuation from original font
     */
    fun withFeatures(features: Array<FontFeature>?): ShapingOptions {
        return ShapingOptions(fontMgr, features, isLeftToRight, isApproximateSpaces, isApproximatePunctuation)
    }

    fun withFeatures(featuresString: String?): ShapingOptions {
        return if (featuresString == null) withFeatures(null as Array<FontFeature>?) else withFeatures(
            FontFeature.Companion.parse(
                featuresString
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ShapingOptions) return false
        if (isLeftToRight != other.isLeftToRight) return false
        if (isApproximateSpaces != other.isApproximateSpaces) return false
        if (isApproximatePunctuation != other.isApproximatePunctuation) return false
        if (if (this.fontMgr == null) other.fontMgr != null else this.fontMgr != other.fontMgr) return false
        return features.contentDeepEquals(other.features)
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (isLeftToRight) 79 else 97
        result = result * PRIME + if (isApproximateSpaces) 79 else 97
        result = result * PRIME + if (isApproximatePunctuation) 79 else 97
        result = result * PRIME + fontMgr.hashCode()
        result = result * PRIME + features.contentDeepHashCode()
        return result
    }

    override fun toString(): String {
        return "ShapingOptions(_fontMgr=" + fontMgr + ", _features=" + features.contentDeepToString() + ", _leftToRight=" + isLeftToRight + ", _approximateSpaces=" + isApproximateSpaces + ", _approximatePunctuation=" + isApproximatePunctuation + ")"
    }

    fun withFontMgr(_fontMgr: FontMgr?): ShapingOptions {
        return if (fontMgr === _fontMgr) this else ShapingOptions(
            _fontMgr,
            features,
            isLeftToRight,
            isApproximateSpaces,
            isApproximatePunctuation
        )
    }

    fun withLeftToRight(_leftToRight: Boolean): ShapingOptions {
        return if (isLeftToRight == _leftToRight) this else ShapingOptions(
            fontMgr,
            features,
            _leftToRight,
            isApproximateSpaces,
            isApproximatePunctuation
        )
    }

    /**
     * If enabled, fallback font runs will not be broken by whitespace from original font
     * @return `this`.
     */
    fun withApproximateSpaces(_approximateSpaces: Boolean): ShapingOptions {
        return if (isApproximateSpaces == _approximateSpaces) this else ShapingOptions(
            fontMgr,
            features,
            isLeftToRight,
            _approximateSpaces,
            isApproximatePunctuation
        )
    }

    /**
     * If enabled, fallback font runs will not be broken by punctuation from original font
     * @return `this`.
     */
    fun withApproximatePunctuation(_approximatePunctuation: Boolean): ShapingOptions {
        return if (isApproximatePunctuation == _approximatePunctuation) this else ShapingOptions(
            fontMgr,
            features,
            isLeftToRight,
            isApproximateSpaces,
            _approximatePunctuation
        )
    }

    companion object {
        val DEFAULT = ShapingOptions(null, null, true, true, true)
    }
}
