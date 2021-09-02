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
        if (java.lang.Float.compare(top, other.top) != 0) return false
        if (java.lang.Float.compare(ascent, other.ascent) != 0) return false
        if (java.lang.Float.compare(descent, other.descent) != 0) return false
        if (java.lang.Float.compare(bottom, other.bottom) != 0) return false
        if (java.lang.Float.compare(leading, other.leading) != 0) return false
        if (java.lang.Float.compare(avgCharWidth, other.avgCharWidth) != 0) return false
        if (java.lang.Float.compare(maxCharWidth, other.maxCharWidth) != 0) return false
        if (java.lang.Float.compare(xMin, other.xMin) != 0) return false
        if (java.lang.Float.compare(xMax, other.xMax) != 0) return false
        if (java.lang.Float.compare(xHeight, other.xHeight) != 0) return false
        if (java.lang.Float.compare(capHeight, other.capHeight) != 0) return false
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
        result = result * PRIME + java.lang.Float.floatToIntBits(top)
        result = result * PRIME + java.lang.Float.floatToIntBits(ascent)
        result = result * PRIME + java.lang.Float.floatToIntBits(descent)
        result = result * PRIME + java.lang.Float.floatToIntBits(bottom)
        result = result * PRIME + java.lang.Float.floatToIntBits(leading)
        result = result * PRIME + java.lang.Float.floatToIntBits(avgCharWidth)
        result = result * PRIME + java.lang.Float.floatToIntBits(maxCharWidth)
        result = result * PRIME + java.lang.Float.floatToIntBits(xMin)
        result = result * PRIME + java.lang.Float.floatToIntBits(xMax)
        result = result * PRIME + java.lang.Float.floatToIntBits(xHeight)
        result = result * PRIME + java.lang.Float.floatToIntBits(capHeight)
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