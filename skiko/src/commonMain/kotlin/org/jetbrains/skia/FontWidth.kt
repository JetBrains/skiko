package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class FontWidth(val value: Int) {
    operator fun compareTo(other: FontWeight): Int = value.compareTo(other.value)

    companion object {
        val ULTRA_CONDENSED = FontWidth(1)
        val EXTRA_CONDENSED = FontWidth(2)
        val CONDENSED = FontWidth(3)
        val SEMI_CONDENSED = FontWidth(4)
        val NORMAL = FontWidth(5)
        val SEMI_EXPANDED = FontWidth(6)
        val EXPANDED = FontWidth(7)
        val EXTRA_EXPANDED = FontWidth(8)
        val ULTRA_EXPANDED = FontWidth(9)
    }
}