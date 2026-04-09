package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

@JvmInline
value class BaselineMode internal constructor(val ordinal: Int) {
    companion object {
        val ALPHABETIC = BaselineMode(0)
        val IDEOGRAPHIC = BaselineMode(1)
    }
}