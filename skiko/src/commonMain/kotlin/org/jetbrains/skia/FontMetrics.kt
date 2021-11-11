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

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontMetrics) return false
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
        if (if (this.underlineThickness == null) other.underlineThickness != null else this.underlineThickness != other.underlineThickness) return false
        if (if (this.underlinePosition == null) other.underlinePosition != null else this.underlinePosition != other.underlinePosition) return false
        if (if (this.strikeoutThickness == null) other.strikeoutThickness != null else this.strikeoutThickness != other.strikeoutThickness) return false
        return !if (this.strikeoutPosition == null) other.strikeoutPosition != null else this.strikeoutPosition != other.strikeoutPosition
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
        result = result * PRIME + underlineThickness.hashCode()
        result = result * PRIME + underlinePosition.hashCode()
        result = result * PRIME + strikeoutThickness.hashCode()
        result = result * PRIME + strikeoutPosition.hashCode()
        return result
    }

    override fun toString(): String {
        return "FontMetrics(_top=$top, _ascent=$ascent, _descent=$descent, _bottom=$bottom, _leading=$leading, _avgCharWidth=$avgCharWidth, _maxCharWidth=$maxCharWidth, _xMin=$xMin, _xMax=$xMax, _xHeight=$xHeight, _capHeight=$capHeight, _underlineThickness=$underlineThickness, _underlinePosition=$underlinePosition, _strikeoutThickness=$strikeoutThickness, _strikeoutPosition=$strikeoutPosition)"
    }

    companion object
}

private inline fun Float.asNumberOrNull(): Float? = if (isNaN()) null else this

internal fun FontMetrics.Companion.fromRawData(rawData: FloatArray) = FontMetrics(
        rawData[0],
        rawData[1],
        rawData[2],
        rawData[3],
        rawData[4],
        rawData[5],
        rawData[6],
        rawData[7],
        rawData[8],
        rawData[9],
        rawData[10],
        rawData[11].asNumberOrNull(),
        rawData[12].asNumberOrNull(),
        rawData[13].asNumberOrNull(),
        rawData[14].asNumberOrNull()
    )