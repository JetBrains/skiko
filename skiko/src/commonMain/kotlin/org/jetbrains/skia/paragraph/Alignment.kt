package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

@JvmInline
value class Alignment internal constructor(val ordinal: Int) {
    companion object {
        val LEFT = Alignment(0)
        val RIGHT = Alignment(1)
        val CENTER = Alignment(2)
        val JUSTIFY = Alignment(3)
        val START = Alignment(4)
        val END = Alignment(5)
    }
}