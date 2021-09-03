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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is PlaceholderStyle) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (width.compareTo(other.width) != 0) return false
        if (height.compareTo(other.height) != 0) return false
        if (baseline.compareTo(other.baseline) != 0) return false
        val `this$_alignment`: Any = alignment
        val `other$_alignment`: Any = other.alignment
        if (`this$_alignment` != `other$_alignment`) return false
        val `this$_baselineMode`: Any = baselineMode
        val `other$_baselineMode`: Any = other.baselineMode
        return `this$_baselineMode` == `other$_baselineMode`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is PlaceholderStyle
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + width.toBits()
        result = result * PRIME + height.toBits()
        result = result * PRIME + baseline.toBits()
        val `$_alignment`: Any = alignment
        result = result * PRIME + (`$_alignment`.hashCode())
        val `$_baselineMode`: Any = baselineMode
        result = result * PRIME + (`$_baselineMode`.hashCode())
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