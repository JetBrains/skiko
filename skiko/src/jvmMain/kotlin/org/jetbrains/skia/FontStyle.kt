package org.jetbrains.skia

class FontStyle {
    val _value: Int

    constructor(weight: Int, width: Int, slant: FontSlant) {
        _value = weight and 65535 or (width and 255 shl 16) or (slant.ordinal shl 24)
    }

    internal constructor(value: Int) {
        _value = value
    }

    val weight: Int
        get() = _value and 65535

    fun withWeight(weight: Int): FontStyle {
        return FontStyle(weight, width, slant)
    }

    val width: Int
        get() = _value shr 16 and 255

    fun withWidth(width: Int): FontStyle {
        return FontStyle(weight, width, slant)
    }

    val slant: FontSlant
        get() = FontSlant.values().get(_value shr 24 and 255)

    fun withSlant(slant: FontSlant): FontStyle {
        return FontStyle(weight, width, slant)
    }

    override fun toString(): String {
        return "FontStyle(weight=$weight, width=$width, slant=\'$slant)"
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontStyle) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        return if (_value != other._value) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontStyle
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + _value
        return result
    }

    companion object {
        val NORMAL = FontStyle(FontWeight.Companion.NORMAL, FontWidth.Companion.NORMAL, FontSlant.UPRIGHT)
        val BOLD = FontStyle(FontWeight.Companion.BOLD, FontWidth.Companion.NORMAL, FontSlant.UPRIGHT)
        val ITALIC = FontStyle(FontWeight.Companion.NORMAL, FontWidth.Companion.NORMAL, FontSlant.ITALIC)
        val BOLD_ITALIC = FontStyle(FontWeight.Companion.BOLD, FontWidth.Companion.NORMAL, FontSlant.ITALIC)
    }
}