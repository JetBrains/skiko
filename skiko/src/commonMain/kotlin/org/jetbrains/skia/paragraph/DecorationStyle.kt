package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult

class DecorationStyle(
    val underline: Boolean,
    val overline: Boolean,
    val lineThrough: Boolean,
    val gaps: Boolean,
    val color: Int,
    val lineStyle: DecorationLineStyle,
    val thicknessMultiplier: Float
) {
    inline fun hasUnderline(): Boolean {
        return underline
    }

    inline fun hasOverline(): Boolean {
        return overline
    }

    inline fun hasLineThrough(): Boolean {
        return lineThrough
    }

    inline fun hasGaps(): Boolean {
        return gaps
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is DecorationStyle) return false
        if (underline != other.underline) return false
        if (overline != other.overline) return false
        if (lineThrough != other.lineThrough) return false
        if (gaps != other.gaps) return false
        if (color != other.color) return false
        if (thicknessMultiplier.compareTo(other.thicknessMultiplier) != 0) return false
        return this.lineStyle == other.lineStyle
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (underline) 79 else 97
        result = result * PRIME + if (overline) 79 else 97
        result = result * PRIME + if (lineThrough) 79 else 97
        result = result * PRIME + if (gaps) 79 else 97
        result = result * PRIME + color
        result = result * PRIME + thicknessMultiplier.toBits()
        result = result * PRIME + lineStyle.hashCode()
        return result
    }

    override fun toString(): String {
        return "DecorationStyle(underline=$underline, overline=$overline, lineThrough=$lineThrough, gaps=$gaps, color=$color, lineStyle=$lineStyle, thicknessMultiplier=$thicknessMultiplier)"
    }

    fun withUnderline(underline: Boolean): DecorationStyle {
        return if (this.underline == underline) this else DecorationStyle(
            underline,
            overline,
            lineThrough,
            gaps,
            color,
            lineStyle,
            thicknessMultiplier
        )
    }

    fun withOverline(overline: Boolean): DecorationStyle {
        return if (this.overline == overline) this else DecorationStyle(
            underline,
            overline,
            lineThrough,
            gaps,
            color,
            lineStyle,
            thicknessMultiplier
        )
    }

    fun withLineThrough(lineThrough: Boolean): DecorationStyle {
        return if (this.lineThrough == lineThrough) this else DecorationStyle(
            underline,
            overline,
            lineThrough,
            gaps,
            color,
            lineStyle,
            thicknessMultiplier
        )
    }

    fun withGaps(gaps: Boolean): DecorationStyle {
        return if (this.gaps == gaps) this else DecorationStyle(
            underline,
            overline,
            lineThrough,
            gaps,
            color,
            lineStyle,
            thicknessMultiplier
        )
    }

    fun withColor(color: Int): DecorationStyle {
        return if (color == color) this else DecorationStyle(
            underline,
            overline,
            lineThrough,
            gaps,
            color,
            lineStyle,
            thicknessMultiplier
        )
    }

    fun withLineStyle(lineStyle: DecorationLineStyle): DecorationStyle {
        return if (this.lineStyle == lineStyle) this else DecorationStyle(
            underline,
            overline,
            lineThrough,
            gaps,
            color,
            lineStyle,
            thicknessMultiplier
        )
    }

    fun withThicknessMultiplier(thicknessMultiplier: Float): DecorationStyle {
        return if (thicknessMultiplier == thicknessMultiplier) this else DecorationStyle(
            underline,
            overline,
            lineThrough,
            gaps,
            color,
            lineStyle,
            thicknessMultiplier
        )
    }

    companion object {
        val NONE: DecorationStyle =
            DecorationStyle(false, false, false, true, -16777216, DecorationLineStyle.SOLID, 1.0f)
    }
}


private fun DecorationStyle.Companion.fromRawData(rawData: IntArray): DecorationStyle {
    val decorationStyleFlags = rawData[0]

    return DecorationStyle(
        underline = ((decorationStyleFlags shr 0) and 1) == 1,
        overline = ((decorationStyleFlags shr 1) and 1) == 1,
        lineThrough = ((decorationStyleFlags shr 2) and 1) == 1,
        gaps = ((decorationStyleFlags shr 3) and 1) == 1,
        color = rawData[1],
        lineStyle = DecorationLineStyle(rawData[2]),
        thicknessMultiplier = Float.fromBits(rawData[3])
    )
}

internal fun DecorationStyle.Companion.fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit) = fromRawData(withResult(IntArray(4), block))
