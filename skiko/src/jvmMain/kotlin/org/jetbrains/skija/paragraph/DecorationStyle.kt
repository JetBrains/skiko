package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

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

    @ApiStatus.Internal
    constructor(
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
        DecorationLineStyle._values.get(lineStyle),
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

    val lineStyle: org.jetbrains.skija.paragraph.DecorationLineStyle
        get() = _lineStyle

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is DecorationStyle) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (_underline != other._underline) return false
        if (_overline != other._overline) return false
        if (_lineThrough != other._lineThrough) return false
        if (_gaps != other._gaps) return false
        if (color != other.color) return false
        if (java.lang.Float.compare(thicknessMultiplier, other.thicknessMultiplier) != 0) return false
        val `this$_lineStyle`: Any = lineStyle
        val `other$_lineStyle`: Any = other.lineStyle
        return if (if (`this$_lineStyle` == null) `other$_lineStyle` != null else `this$_lineStyle` != `other$_lineStyle`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is DecorationStyle
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (_underline) 79 else 97
        result = result * PRIME + if (_overline) 79 else 97
        result = result * PRIME + if (_lineThrough) 79 else 97
        result = result * PRIME + if (_gaps) 79 else 97
        result = result * PRIME + color
        result = result * PRIME + java.lang.Float.floatToIntBits(thicknessMultiplier)
        val `$_lineStyle`: Any = lineStyle
        result = result * PRIME + (`$_lineStyle`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "DecorationStyle(_underline=" + _underline + ", _overline=" + _overline + ", _lineThrough=" + _lineThrough + ", _gaps=" + _gaps + ", _color=" + color + ", _lineStyle=" + lineStyle + ", _thicknessMultiplier=" + thicknessMultiplier + ")"
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