package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class ColorChannel internal constructor(val ordinal: Int) {
    companion object {
        val R = ColorChannel(0)
        val G = ColorChannel(1)
        val B = ColorChannel(2)
        val A = ColorChannel(3)
    }
}