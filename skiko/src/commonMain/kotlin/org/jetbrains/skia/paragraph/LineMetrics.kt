package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.*

class LineMetrics(
    /**
     * The index in the text buffer the line begins.
     */
    val startIndex: Int,
    /**
     * The index in the text buffer the line ends.
     */
    val endIndex: Int,
    /**
     * The index in the text buffer the line ends.
     */
    val endExcludingWhitespaces: Int,
    /**
     * The index in the text buffer the line begins.
     */
    val endIncludingNewline: Int,
    val isHardBreak: Boolean,
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
    val lineNumber: Int
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

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is LineMetrics) return false
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

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + startIndex
        result = result * PRIME + endIndex
        result = result * PRIME + endExcludingWhitespaces
        result = result * PRIME + endIncludingNewline
        result = result * PRIME + if (isHardBreak) 79 else 97
        result = result * PRIME + ascent.toBits().toInt()
        result = result * PRIME + descent.toBits().toInt()
        result = result * PRIME + unscaledAscent.toBits().toInt()
        result = result * PRIME + height.toBits().toInt()
        result = result * PRIME + width.toBits().toInt()
        result = result * PRIME + left.toBits().toInt()
        result = result * PRIME + baseline.toBits().toInt()
        result = result * PRIME + lineNumber
        return result
    }

    internal companion object : ArrayInteropDecoder<LineMetrics> {
        override fun getArraySize(array: InteropPointer) = LineMetrics_nGetArraySize(array)
        override fun disposeArray(array: InteropPointer) = LineMetrics_nDisposeArray(array)
        override fun getArrayElement(array: InteropPointer, index: Int): LineMetrics {
            val intArray = IntArray(6)
            val doubleArray = DoubleArray(7)
            interopScope {
                LineMetrics_nGetArrayElement(array, index, toInterop(intArray), toInterop(doubleArray))
            }
            return LineMetrics(
                intArray[0],
                intArray[1],
                intArray[2],
                intArray[3],
                intArray[4] != 0,
                doubleArray[0],
                doubleArray[1],
                doubleArray[2],
                doubleArray[3],
                doubleArray[4],
                doubleArray[5],
                doubleArray[6],
                intArray[5]
            )
        }
    }

    override fun toString(): String {
        return "LineMetrics(_startIndex=$startIndex, _endIndex=$endIndex, _endExcludingWhitespaces=$endExcludingWhitespaces, _endIncludingNewline=$endIncludingNewline, _hardBreak=$isHardBreak, _ascent=$ascent, _descent=$descent, _unscaledAscent=$unscaledAscent, _height=$height, _width=$width, _left=$left, _baseline=$baseline, _lineNumber=$lineNumber)"
    }
}

@ExternalSymbolName("org_jetbrains_skia_paragraph_LineMetrics__1nGetArraySize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_LineMetrics__1nGetArraySize")
private external fun LineMetrics_nGetArraySize(array: InteropPointer): Int
@ExternalSymbolName("org_jetbrains_skia_paragraph_LineMetrics__1nDisposeArray")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_LineMetrics__1nDisposeArray")
private external fun LineMetrics_nDisposeArray(array: InteropPointer)
@ExternalSymbolName("org_jetbrains_skia_paragraph_LineMetrics__1nGetArrayElement")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_LineMetrics__1nGetArrayElement")
private external fun LineMetrics_nGetArrayElement(array: InteropPointer, index: Int, longArgs: InteropPointer, doubleArgs: InteropPointer)
