package org.jetbrains.skia.icu

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * CharDirection represents Bidi_Class Unicode character property.
 * Numeric constant values match to ICU's UCharDirection enum.
 *
 * See https://unicode-org.github.io/icu-docs/apidoc/dev/icu4c/uchar_8h.html
 * See https://www.unicode.org/reports/tr9/
 */
object CharDirection {
    init {
        staticLoad()
    }

    const val LEFT_TO_RIGHT = 0
    const val RIGHT_TO_LEFT = 1
    const val EUROPEAN_NUMBER = 2
    const val EUROPEAN_NUMBER_SEPARATOR = 3
    const val EUROPEAN_NUMBER_TERMINATOR = 4
    const val ARABIC_NUMBER = 5
    const val COMMON_NUMBER_SEPARATOR = 6
    const val BLOCK_SEPARATOR = 7
    const val SEGMENT_SEPARATOR = 8
    const val WHITE_SPACE_NEUTRAL = 9
    const val OTHER_NEUTRAL = 10
    const val LEFT_TO_RIGHT_EMBEDDING = 11
    const val LEFT_TO_RIGHT_OVERRIDE = 12
    const val RIGHT_TO_LEFT_ARABIC = 13
    const val RIGHT_TO_LEFT_EMBEDDING = 14
    const val RIGHT_TO_LEFT_OVERRIDE = 15
    const val POP_DIRECTIONAL_FORMAT = 16
    const val DIR_NON_SPACING_MARK = 17
    const val BOUNDARY_NEUTRAL = 18
    const val FIRST_STRONG_ISOLATE = 19
    const val LEFT_TO_RIGHT_ISOLATE = 20
    const val RIGHT_TO_LEFT_ISOLATE = 21
    const val POP_DIRECTIONAL_ISOLATE = 22

    /**
     * Returns the bidirectional category value for the code point.
     * Same as java.lang.Character.getDirectionality()
     */
    fun of(codePoint: Int): Int = charDirection(codePoint)
}

@ExternalSymbolName("org_jetbrains_skia_icu_Unicode_charDirection")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_icu_Unicode_charDirection")
private external fun charDirection(codePoint: Int): Int
