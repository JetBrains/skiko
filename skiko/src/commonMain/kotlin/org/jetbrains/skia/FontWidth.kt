package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class FontWidth(val value: Int) {

    operator fun compareTo(other: FontWeight): Int = value.compareTo(other.value)
    companion object {
        val ULTRA_CONDENSED: FontWidth
            get() = FontWidth(1)
        val EXTRA_CONDENSED: FontWidth
            get() = FontWidth(2)
        val CONDENSED: FontWidth
            get() = FontWidth(3)
        val SEMI_CONDENSED: FontWidth
            get() = FontWidth(4)
        val NORMAL: FontWidth
            get() = FontWidth(5)
        val SEMI_EXPANDED: FontWidth
            get() = FontWidth(6)
        val EXPANDED: FontWidth
            get() = FontWidth(7)
        val EXTRA_EXPANDED: FontWidth
            get() = FontWidth(8)
        val ULTRA_EXPANDED: FontWidth
            get() = FontWidth(9)
    }
}