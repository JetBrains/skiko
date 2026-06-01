package org.jetbrains.skia

import kotlin.jvm.JvmInline


@JvmInline
value class FontWeight(val value: Int) {
    operator fun compareTo(other: FontWeight): Int = value.compareTo(other.value)

     companion object {
        val INVISIBLE = FontWeight(0)
        val THIN = FontWeight(100)
        val EXTRA_LIGHT = FontWeight(200)
        val LIGHT = FontWeight(300)
        val NORMAL = FontWeight(400)
        val MEDIUM = FontWeight(500)
        val SEMI_BOLD = FontWeight(600)
        val BOLD = FontWeight(700)
        val EXTRA_BOLD = FontWeight(800)
        val BLACK = FontWeight(900)
        val EXTRA_BLACK = FontWeight(1000)
    }
}