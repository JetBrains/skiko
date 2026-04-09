package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

@JvmInline
value class Direction internal constructor(val ordinal: Int) {
    companion object {
        val RTL = Direction(0)
        val LTR = Direction(1)
    }
}
