package org.jetbrains.skija.paragraph

enum class TextStyleAttribute {
    NONE, ALL_ATTRIBUTES, FONT, FOREGROUND, BACKGROUND, SHADOW, DECORATIONS, LETTER_SPACING, WORD_SPACING, FONT_EXACT;

    companion object {
        internal val _values = values()
    }
}