package org.jetbrains.skija.shaper

import org.jetbrains.skija.FontFeature
import org.jetbrains.skija.FontMgr
import java.util.*

class ShapingOptions(
    internal val fontMgr: FontMgr?,
    internal val features: Array<FontFeature?>?,
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

    /**
     * If enabled, fallback font runs will not be broken by whitespace from original font
     */
    /**
     * If enabled, fallback font runs will not be broken by punctuation from original font
     */
    fun withFeatures(features: Array<FontFeature?>?): ShapingOptions {
        return ShapingOptions(fontMgr, features, isLeftToRight, isApproximateSpaces, isApproximatePunctuation)
    }

    fun withFeatures(featuresString: String?): ShapingOptions {
        return if (featuresString == null) withFeatures(null as Array<FontFeature?>?) else withFeatures(
            FontFeature.Companion.parse(
                featuresString
            )
        )
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ShapingOptions) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (isLeftToRight != other.isLeftToRight) return false
        if (isApproximateSpaces != other.isApproximateSpaces) return false
        if (isApproximatePunctuation != other.isApproximatePunctuation) return false
        val `this$_fontMgr`: Any? = fontMgr
        val `other$_fontMgr`: Any? = other.fontMgr
        if (if (`this$_fontMgr` == null) `other$_fontMgr` != null else `this$_fontMgr` != `other$_fontMgr`) return false
        return if (!Arrays.deepEquals(features, other.features)) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ShapingOptions
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (isLeftToRight) 79 else 97
        result = result * PRIME + if (isApproximateSpaces) 79 else 97
        result = result * PRIME + if (isApproximatePunctuation) 79 else 97
        val `$_fontMgr`: Any? = fontMgr
        result = result * PRIME + (`$_fontMgr`?.hashCode() ?: 43)
        result = result * PRIME + Arrays.deepHashCode(features)
        return result
    }

    override fun toString(): String {
        return "ShapingOptions(_fontMgr=" + fontMgr + ", _features=" + Arrays.deepToString(features) + ", _leftToRight=" + isLeftToRight + ", _approximateSpaces=" + isApproximateSpaces + ", _approximatePunctuation=" + isApproximatePunctuation + ")"
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