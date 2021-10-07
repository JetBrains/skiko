package org.jetbrains.skia.paragraph

class PlaceholderStyle(
    val width: Float,
    val height: Float,
    alignment: PlaceholderAlignment,
    baselineMode: BaselineMode,
    baseline: Float
) {
    val _alignment: PlaceholderAlignment
    val _baselineMode: BaselineMode
    /**
     *
     * Distance from the top edge of the rect to the baseline position. This
     * baseline will be aligned against the alphabetic baseline of the surrounding
     * text.
     *
     *
     * Positive values drop the baseline lower (positions the rect higher) and
     * small or negative values will cause the rect to be positioned underneath
     * the line. When baseline == height, the bottom edge of the rect will rest on
     * the alphabetic baseline.
     */
    /**
     *
     * Distance from the top edge of the rect to the baseline position. This
     * baseline will be aligned against the alphabetic baseline of the surrounding
     * text.
     *
     *
     * Positive values drop the baseline lower (positions the rect higher) and
     * small or negative values will cause the rect to be positioned underneath
     * the line. When baseline == height, the bottom edge of the rect will rest on
     * the alphabetic baseline.
     */
    val baseline: Float
    val alignment: PlaceholderAlignment
        get() = _alignment
    val baselineMode: BaselineMode
        get() = _baselineMode

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PlaceholderStyle) return false
        if (width.compareTo(other.width) != 0) return false
        if (height.compareTo(other.height) != 0) return false
        if (baseline.compareTo(other.baseline) != 0) return false
        if (this.alignment != other.alignment) return false
        return this.baselineMode == other.baselineMode
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + width.toBits()
        result = result * PRIME + height.toBits()
        result = result * PRIME + baseline.toBits()
        result = result * PRIME + alignment.hashCode()
        result = result * PRIME + baselineMode.hashCode()
        return result
    }

    override fun toString(): String {
        return "PlaceholderStyle(_width=$width, _height=$height, _alignment=$alignment, _baselineMode=$baselineMode, _baseline=$baseline)"
    }

    init {
        _alignment = alignment
        _baselineMode = baselineMode
        this.baseline = baseline
    }
}