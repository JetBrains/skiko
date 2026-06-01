package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

@JvmInline
value class HeightMode internal constructor(val ordinal: Int) {
    companion object {
        val ALL = HeightMode(0)
        val DISABLE_FIRST_ASCENT = HeightMode(1)
        val DISABLE_LAST_DESCENT = HeightMode(2)
        val DISABLE_ALL = HeightMode(3)
    }
}
