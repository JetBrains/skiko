package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class PaintMode internal constructor(val ordinal: Int) {
    companion object {
        val FILL = PaintMode(0)
        val STROKE = PaintMode(1)
        val STROKE_AND_FILL = PaintMode(2)
    }
}