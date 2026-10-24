package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class FontWeight(val value: Int) {

    operator fun compareTo(other: FontWeight): Int = value.compareTo(other.value)

    companion object {
        val INVISIBLE: FontWeight
            get() = FontWeight(0)
        val THIN: FontWeight
            get() = FontWeight(100)
        val EXTRA_LIGHT: FontWeight
            get() = FontWeight(200)
        val LIGHT: FontWeight
            get() = FontWeight(300)
        val NORMAL: FontWeight
            get() = FontWeight(400)
        val MEDIUM: FontWeight
            get() = FontWeight(500)
        val SEMI_BOLD: FontWeight
            get() = FontWeight(600)
        val BOLD: FontWeight
            get() = FontWeight(700)
        val EXTRA_BOLD: FontWeight
            get() = FontWeight(800)
        val BLACK: FontWeight
            get() = FontWeight(900)
        val EXTRA_BLACK: FontWeight
            get() = FontWeight(1000)
    }
}