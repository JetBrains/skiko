package org.jetbrains.skia

class FontMetrics(
    /**
     * greatest extent above origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    val top: Float,
    /**
     * distance to reserve above baseline, typically negative
     */
    val ascent: Float,
    /**
     * distance to reserve below baseline, typically positive
     */
    val descent: Float,
    /**
     * greatest extent below origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    val bottom: Float,
    /**
     * distance to add between lines, typically positive or zero
     */
    val leading: Float,
    /**
     * average character width, zero if unknown
     */
    val avgCharWidth: Float,
    /**
     * maximum character width, zero if unknown
     */
    val maxCharWidth: Float,
    /**
     * greatest extent to left of origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    val xMin: Float,
    /**
     * greatest extent to right of origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    val xMax: Float,
    /**
     * height of lower-case 'x', zero if unknown, typically negative
     */
    val xHeight: Float,
    /**
     * height of an upper-case letter, zero if unknown, typically negative
     */
    val capHeight: Float,
    /**
     * underline thickness
     */
    val underlineThickness: Float?,
    /**
     * distance from baseline to top of stroke, typically positive
     */
    val underlinePosition: Float?,
    /**
     * strikeout thickness
     */
    val strikeoutThickness: Float?,
    /**
     * distance from baseline to bottom of stroke, typically negative
     */
    val strikeoutPosition: Float?
) {
    /**
     * greatest extent above origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    /**
     * distance to reserve above baseline, typically negative
     */
    /**
     * distance to reserve below baseline, typically positive
     */
    /**
     * greatest extent below origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    /**
     * distance to add between lines, typically positive or zero
     */
    /**
     * average character width, zero if unknown
     */
    /**
     * maximum character width, zero if unknown
     */
    /**
     * greatest extent to left of origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    /**
     * greatest extent to right of origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    /**
     * height of lower-case 'x', zero if unknown, typically negative
     */
    /**
     * height of an upper-case letter, zero if unknown, typically negative
     */
    /**
     * underline thickness
     */
    /**
     * distance from baseline to top of stroke, typically positive
     */
    /**
     * strikeout thickness
     */
    /**
     * distance from baseline to bottom of stroke, typically negative
     */
    val height: Float
        get() = descent - ascent

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontMetrics) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (top.compareTo(other.top) != 0) return false
        if (ascent.compareTo(other.ascent) != 0) return false
        if (descent.compareTo(other.descent) != 0) return false
        if (bottom.compareTo(other.bottom) != 0) return false
        if (leading.compareTo(other.leading) != 0) return false
        if (avgCharWidth.compareTo(other.avgCharWidth) != 0) return false
        if (maxCharWidth.compareTo(other.maxCharWidth) != 0) return false
        if (xMin.compareTo(other.xMin) != 0) return false
        if (xMax.compareTo(other.xMax) != 0) return false
        if (xHeight.compareTo(other.xHeight) != 0) return false
        if (capHeight.compareTo(other.capHeight) != 0) return false
        val `this$_underlineThickness`: Any? = underlineThickness
        val `other$_underlineThickness`: Any? = other.underlineThickness
        if (if (`this$_underlineThickness` == null) `other$_underlineThickness` != null else `this$_underlineThickness` != `other$_underlineThickness`) return false
        val `this$_underlinePosition`: Any? = underlinePosition
        val `other$_underlinePosition`: Any? = other.underlinePosition
        if (if (`this$_underlinePosition` == null) `other$_underlinePosition` != null else `this$_underlinePosition` != `other$_underlinePosition`) return false
        val `this$_strikeoutThickness`: Any? = strikeoutThickness
        val `other$_strikeoutThickness`: Any? = other.strikeoutThickness
        if (if (`this$_strikeoutThickness` == null) `other$_strikeoutThickness` != null else `this$_strikeoutThickness` != `other$_strikeoutThickness`) return false
        val `this$_strikeoutPosition`: Any? = strikeoutPosition
        val `other$_strikeoutPosition`: Any? = other.strikeoutPosition
        return if (if (`this$_strikeoutPosition` == null) `other$_strikeoutPosition` != null else `this$_strikeoutPosition` != `other$_strikeoutPosition`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontMetrics
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + top.toBits()
        result = result * PRIME + ascent.toBits()
        result = result * PRIME + descent.toBits()
        result = result * PRIME + bottom.toBits()
        result = result * PRIME + leading.toBits()
        result = result * PRIME + avgCharWidth.toBits()
        result = result * PRIME + maxCharWidth.toBits()
        result = result * PRIME + xMin.toBits()
        result = result * PRIME + xMax.toBits()
        result = result * PRIME + xHeight.toBits()
        result = result * PRIME + capHeight.toBits()
        val `$_underlineThickness`: Any? = underlineThickness
        result = result * PRIME + (`$_underlineThickness`?.hashCode() ?: 43)
        val `$_underlinePosition`: Any? = underlinePosition
        result = result * PRIME + (`$_underlinePosition`?.hashCode() ?: 43)
        val `$_strikeoutThickness`: Any? = strikeoutThickness
        result = result * PRIME + (`$_strikeoutThickness`?.hashCode() ?: 43)
        val `$_strikeoutPosition`: Any? = strikeoutPosition
        result = result * PRIME + (`$_strikeoutPosition`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "FontMetrics(_top=" + top + ", _ascent=" + ascent + ", _descent=" + descent + ", _bottom=" + bottom + ", _leading=" + leading + ", _avgCharWidth=" + avgCharWidth + ", _maxCharWidth=" + maxCharWidth + ", _xMin=" + xMin + ", _xMax=" + xMax + ", _xHeight=" + xHeight + ", _capHeight=" + capHeight + ", _underlineThickness=" + underlineThickness + ", _underlinePosition=" + underlinePosition + ", _strikeoutThickness=" + strikeoutThickness + ", _strikeoutPosition=" + strikeoutPosition + ")"
    }
}