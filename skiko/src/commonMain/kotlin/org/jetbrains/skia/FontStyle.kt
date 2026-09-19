package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class FontStyle internal constructor(val _value: Int) {
    constructor(weight: FontWeight, width: FontWidth, slant: FontSlant) : this(weight.value and 65535 or (width.value and 255 shl 16) or (slant.ordinal shl 24))
    inline val weight: FontWeight
        get() = FontWeight(_value and 65535)

    fun withWeight(weight: Int): FontStyle {
        return FontStyle(FontWeight(weight), width, slant)
    }

    inline val width: FontWidth
        get() = FontWidth(_value shr 16 and 255)

    fun withWidth(width: Int): FontStyle {
        return FontStyle(weight, FontWidth(width), slant)
    }

    val slant: FontSlant
        get() = FontSlant.entries[_value shr 24 and 255]

    fun withSlant(slant: FontSlant): FontStyle {
        return FontStyle(weight, width, slant)
    }

    override fun toString(): String = "FontStyle(weight=$weight, width=$width, slant=$slant)"

    companion object {
        val NORMAL = FontStyle(FontWeight.NORMAL, FontWidth.NORMAL, FontSlant.UPRIGHT)
        val BOLD = FontStyle(FontWeight.BOLD, FontWidth.NORMAL, FontSlant.UPRIGHT)
        val ITALIC = FontStyle(FontWeight.NORMAL, FontWidth.NORMAL, FontSlant.ITALIC)
        val BOLD_ITALIC = FontStyle(FontWeight.BOLD, FontWidth.NORMAL, FontSlant.ITALIC)
    }
}