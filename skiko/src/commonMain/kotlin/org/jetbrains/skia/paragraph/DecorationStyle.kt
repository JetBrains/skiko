package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult

class DecorationStyle(
    val _underline: Boolean,
    val _overline: Boolean,
    val _lineThrough: Boolean,
    val _gaps: Boolean,
    val color: Int,
    lineStyle: DecorationLineStyle,
    thicknessMultiplier: Float
) {
    val _lineStyle: DecorationLineStyle
    val thicknessMultiplier: Float

    internal constructor(
        underline: Boolean,
        overline: Boolean,
        lineThrough: Boolean,
        gaps: Boolean,
        color: Int,
        lineStyle: Int,
        thicknessMultiplier: Float
    ) : this(
        underline,
        overline,
        lineThrough,
        gaps,
        color,
        DecorationLineStyle.entries[lineStyle],
        thicknessMultiplier
    ) {
    }

    fun hasUnderline(): Boolean {
        return _underline
    }

    fun hasOverline(): Boolean {
        return _overline
    }

    fun hasLineThrough(): Boolean {
        return _lineThrough
    }

    fun hasGaps(): Boolean {
        return _gaps
    }

    val lineStyle: DecorationLineStyle
        get() = _lineStyle

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is DecorationStyle) return false
        if (_underline != other._underline) return false
        if (_overline != other._overline) return false
        if (_lineThrough != other._lineThrough) return false
        if (_gaps != other._gaps) return false
        if (color != other.color) return false
        if (thicknessMultiplier.compareTo(other.thicknessMultiplier) != 0) return false
        return this.lineStyle == other.lineStyle
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (_underline) 79 else 97
        result = result * PRIME + if (_overline) 79 else 97
        result = result * PRIME + if (_lineThrough) 79 else 97
        result = result * PRIME + if (_gaps) 79 else 97
        result = result * PRIME + color
        result = result * PRIME + thicknessMultiplier.toBits()
        result = result * PRIME + lineStyle.hashCode()
        return result
    }

    override fun toString(): String {
        return "DecorationStyle(_underline=$_underline, _overline=$_overline, _lineThrough=$_lineThrough, _gaps=$_gaps, _color=$color, _lineStyle=$lineStyle, _thicknessMultiplier=$thicknessMultiplier)"
    }

    fun withUnderline(_underline: Boolean): DecorationStyle {
        return if (this._underline == _underline) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withOverline(_overline: Boolean): DecorationStyle {
        return if (this._overline == _overline) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withLineThrough(_lineThrough: Boolean): DecorationStyle {
        return if (this._lineThrough == _lineThrough) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withGaps(_gaps: Boolean): DecorationStyle {
        return if (this._gaps == _gaps) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withColor(_color: Int): DecorationStyle {
        return if (color == _color) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            _color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withLineStyle(_lineStyle: DecorationLineStyle): DecorationStyle {
        return if (this._lineStyle === _lineStyle) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withThicknessMultiplier(_thicknessMultiplier: Float): DecorationStyle {
        return if (thicknessMultiplier == _thicknessMultiplier) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            _thicknessMultiplier
        )
    }

    companion object {
        val NONE: DecorationStyle =
            DecorationStyle(false, false, false, true, -16777216, DecorationLineStyle.SOLID, 1.0f)
    }

    init {
        _lineStyle = lineStyle
        this.thicknessMultiplier = thicknessMultiplier
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
        lineStyle = rawData[2],
        thicknessMultiplier = Float.fromBits(rawData[3])
    )
}

internal fun DecorationStyle.Companion.fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit) = fromRawData(withResult(IntArray(4), block))
