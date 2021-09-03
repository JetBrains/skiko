package org.jetbrains.skia.paragraph

class LineMetrics(
    /**
     * The index in the text buffer the line begins.
     */
    val startIndex: Long,
    /**
     * The index in the text buffer the line ends.
     */
    val endIndex: Long,
    /**
     * The index in the text buffer the line ends.
     */
    val endExcludingWhitespaces: Long,
    /**
     * The index in the text buffer the line begins.
     */
    val endIncludingNewline: Long, val isHardBreak: Boolean,
    /**
     * The final computed ascent for the line. This can be impacted by the strut, height,
     * scaling, as well as outlying runs that are very tall. The top edge is
     * `getBaseline() - getAscent()` and the bottom edge is `getBaseline() + getDescent()`.
     * Ascent and descent are provided as positive numbers. These values are the cumulative metrics for the entire line.
     */
    val ascent: Double,
    /**
     * The final computed descent for the line. This can be impacted by the strut, height,
     * scaling, as well as outlying runs that are very tall. The top edge is
     * `getBaseline() - getAscent()` and the bottom edge is `getBaseline() + getDescent()`.
     * Ascent and descent are provided as positive numbers. These values are the cumulative metrics for the entire line.
     */
    val descent: Double,
    /**
     * The final computed descent for the line. This can be impacted by the strut, height,
     * scaling, as well as outlying runs that are very tall. The top edge is
     * `getBaseline() - getAscent()` and the bottom edge is `getBaseline() + getDescent()`.
     * Ascent and descent are provided as positive numbers. These values are the cumulative metrics for the entire line.
     */
    val unscaledAscent: Double,
    /**
     * Total height of the paragraph including the current line.
     */
    val height: Double,
    /**
     * Width of the line.
     */
    val width: Double,
    /**
     * The left edge of the line.
     */
    val left: Double,
    /**
     * The y position of the baseline for this line from the top of the paragraph.
     */
    val baseline: Double,
    /**
     * Zero indexed line number
     */
    val lineNumber: Long
) {
    /**
     * The final computed ascent for the line. This can be impacted by the strut, height,
     * scaling, as well as outlying runs that are very tall. The top edge is
     * `getBaseline() - getAscent()` and the bottom edge is `getBaseline() + getDescent()`.
     * Ascent and descent are provided as positive numbers. These values are the cumulative metrics for the entire line.
     */
    /**
     * Total height of the paragraph including the current line.
     */
    /**
     * Width of the line.
     */
    /**
     * The left edge of the line.
     */
    /**
     * The y position of the baseline for this line from the top of the paragraph.
     */
    /**
     * Zero indexed line number
     */

    /**
     * The height of the current line, equals to `Math.round(getAscent() + getDescent())`.
     */
    val lineHeight: Double
        get() = ascent + descent

    /**
     * The right edge of the line, equals to `getLeft() + getWidth()`
     */
    val right: Double
        get() = left + width

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is LineMetrics) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (startIndex != other.startIndex) return false
        if (endIndex != other.endIndex) return false
        if (endExcludingWhitespaces != other.endExcludingWhitespaces) return false
        if (endIncludingNewline != other.endIncludingNewline) return false
        if (isHardBreak != other.isHardBreak) return false
        if (ascent.compareTo(other.ascent) != 0) return false
        if (descent.compareTo(other.descent) != 0) return false
        if (unscaledAscent.compareTo(other.unscaledAscent) != 0) return false
        if (height.compareTo(other.height) != 0) return false
        if (width.compareTo(other.width) != 0) return false
        if (left.compareTo(other.left) != 0) return false
        if (baseline.compareTo(other.baseline) != 0) return false
        return lineNumber == other.lineNumber
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is LineMetrics
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_startIndex` = startIndex
        result = result * PRIME + (`$_startIndex` ushr 32 xor `$_startIndex`).toInt()
        val `$_endIndex` = endIndex
        result = result * PRIME + (`$_endIndex` ushr 32 xor `$_endIndex`).toInt()
        val `$_endExcludingWhitespaces` = endExcludingWhitespaces
        result = result * PRIME + (`$_endExcludingWhitespaces` ushr 32 xor `$_endExcludingWhitespaces`).toInt()
        val `$_endIncludingNewline` = endIncludingNewline
        result = result * PRIME + (`$_endIncludingNewline` ushr 32 xor `$_endIncludingNewline`).toInt()
        result = result * PRIME + if (isHardBreak) 79 else 97
        val `$_ascent` = ascent.toBits()
        result = result * PRIME + (`$_ascent` ushr 32 xor `$_ascent`).toInt()
        val `$_descent` = descent.toBits()
        result = result * PRIME + (`$_descent` ushr 32 xor `$_descent`).toInt()
        val `$_unscaledAscent` = unscaledAscent.toBits()
        result = result * PRIME + (`$_unscaledAscent` ushr 32 xor `$_unscaledAscent`).toInt()
        val `$_height` = height.toBits()
        result = result * PRIME + (`$_height` ushr 32 xor `$_height`).toInt()
        val `$_width` = width.toBits()
        result = result * PRIME + (`$_width` ushr 32 xor `$_width`).toInt()
        val `$_left` = left.toBits()
        result = result * PRIME + (`$_left` ushr 32 xor `$_left`).toInt()
        val `$_baseline` = baseline.toBits()
        result = result * PRIME + (`$_baseline` ushr 32 xor `$_baseline`).toInt()
        val `$_lineNumber` = lineNumber
        result = result * PRIME + (`$_lineNumber` ushr 32 xor `$_lineNumber`).toInt()
        return result
    }

    override fun toString(): String {
        return "LineMetrics(_startIndex=$startIndex, _endIndex=$endIndex, _endExcludingWhitespaces=$endExcludingWhitespaces, _endIncludingNewline=$endIncludingNewline, _hardBreak=$isHardBreak, _ascent=$ascent, _descent=$descent, _unscaledAscent=$unscaledAscent, _height=$height, _width=$width, _left=$left, _baseline=$baseline, _lineNumber=$lineNumber)"
    }
}