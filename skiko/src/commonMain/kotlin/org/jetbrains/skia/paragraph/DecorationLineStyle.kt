package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

@JvmInline
value class DecorationLineStyle internal constructor(val ordinal: Int) {
    companion object {
        val SOLID = DecorationLineStyle(0)
        val DOUBLE = DecorationLineStyle(1)
        val DOTTED = DecorationLineStyle(2)
        val DASHED = DecorationLineStyle(3)
        val WAVY = DecorationLineStyle(4)
    }
}