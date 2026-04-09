package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class ClipMode internal constructor(val ordinal: Int) {
    companion object {
        val DIFFERENCE = ClipMode(0)
        val INTERSECT = ClipMode(1)
    }
}