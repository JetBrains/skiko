package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class TextStyleAttribute {
    NONE, ALL_ATTRIBUTES, FONT, FOREGROUND, BACKGROUND, SHADOW, DECORATIONS, LETTER_SPACING, WORD_SPACING, FONT_EXACT;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}