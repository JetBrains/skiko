package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

@JvmInline
value class TextStyleAttribute internal constructor(val ordinal: Int) {
    companion object {
        val NONE = TextStyleAttribute(0)
        val ALL_ATTRIBUTES = TextStyleAttribute(1)
        val FONT = TextStyleAttribute(2)
        val FOREGROUND = TextStyleAttribute(3)
        val BACKGROUND = TextStyleAttribute(4)
        val SHADOW = TextStyleAttribute(5)
        val DECORATIONS = TextStyleAttribute(6)
        val LETTER_SPACING = TextStyleAttribute(7)
        val WORD_SPACING = TextStyleAttribute(8)
        val FONT_EXACT = TextStyleAttribute(9)
    }
}
