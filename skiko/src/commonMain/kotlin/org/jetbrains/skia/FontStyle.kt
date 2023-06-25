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
        get() = FontSlant.values()[_value shr 24 and 255]

    fun withSlant(slant: FontSlant): FontStyle {
        return FontStyle(weight, width, slant)
    }

    override fun toString(): String = "FontStyle(weight=$weight, width=$width, slant=$slant)"

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontStyle) return false
        return _value == other._value
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + _value
        return result
    }

    companion object {
        val NORMAL = FontStyle(FontWeight.NORMAL, FontWidth.NORMAL, FontSlant.UPRIGHT)
        val BOLD = FontStyle(FontWeight.BOLD, FontWidth.NORMAL, FontSlant.UPRIGHT)
        val ITALIC = FontStyle(FontWeight.NORMAL, FontWidth.NORMAL, FontSlant.ITALIC)
        val BOLD_ITALIC = FontStyle(FontWeight.BOLD, FontWidth.NORMAL, FontSlant.ITALIC)
    }
}
