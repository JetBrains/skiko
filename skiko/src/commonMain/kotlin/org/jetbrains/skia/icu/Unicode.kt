package org.jetbrains.skia.icu

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Stats

/**
 * CharDirection represents Bidi_Class Unicode character property.
 * Numeric constant values match to ICU's UCharDirection enum.
 *
 * See https://unicode-org.github.io/icu-docs/apidoc/dev/icu4c/uchar_8h.html
 * See https://www.unicode.org/reports/tr9/
 */
@Suppress("MemberVisibilityCanBePrivate", "unused", "SpellCheckingInspection")
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
    fun of(codePoint: Int): Int {
        Stats.onNativeCall()
        return _nCharDirection(codePoint)
    }
}

/**
 * Bundles functions to inspect Unicode character properties.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused", "SpellCheckingInspection")
object CharProperties {
    init {
        staticLoad()
    }

    const val ALPHABETIC = 0
    const val BINARY_START = ALPHABETIC
    const val ASCII_HEX_DIGIT = 1
    const val BIDI_CONTROL = 2
    const val BIDI_MIRRORED = 3
    const val DASH = 4
    const val DEFAULT_IGNORABLE_CODE_POINT = 5
    const val DEPRECATED = 6
    const val DIACRITIC = 7
    const val EXTENDER = 8
    const val FULL_COMPOSITION_EXCLUSION = 9
    const val GRAPHEME_BASE = 10
    const val GRAPHEME_EXTEND = 11
    const val GRAPHEME_LINK = 12
    const val HEX_DIGIT = 13
    const val HYPHEN = 14
    const val ID_CONTINUE = 15
    const val ID_START = 16
    const val IDEOGRAPHIC = 17
    const val IDS_BINARY_OPERATOR = 18
    const val IDS_TRINARY_OPERATOR = 19
    const val JOIN_CONTROL = 20
    const val LOGICAL_ORDER_EXCEPTION = 21
    const val LOWERCASE = 22
    const val MATH = 23
    const val NONCHARACTER_CODE_POINT = 24
    const val QUOTATION_MARK = 25
    const val RADICAL = 26
    const val SOFT_DOTTED = 27
    const val TERMINAL_PUNCTUATION = 28
    const val UNIFIED_IDEOGRAPH = 29
    const val UPPERCASE = 30
    const val WHITE_SPACE = 31
    const val XID_CONTINUE = 32
    const val XID_START = 33
    const val CASE_SENSITIVE = 34
    const val S_TERM = 35
    const val VARIATION_SELECTOR = 36
    const val NFD_INERT = 37
    const val NFKD_INERT = 38
    const val NFC_INERT = 39
    const val NFKC_INERT = 40
    const val SEGMENT_STARTER = 41
    const val PATTERN_SYNTAX = 42
    const val PATTERN_WHITE_SPACE = 43
    const val POSIX_ALNUM = 44
    const val POSIX_BLANK = 45
    const val POSIX_GRAPH = 46
    const val POSIX_PRINT = 47
    const val POSIX_XDIGIT = 48
    const val CASED = 49
    const val CASE_IGNORABLE = 50
    const val CHANGES_WHEN_LOWERCASED = 51
    const val CHANGES_WHEN_UPPERCASED = 52
    const val CHANGES_WHEN_TITLECASED = 53
    const val CHANGES_WHEN_CASEFOLDED = 54
    const val CHANGES_WHEN_CASEMAPPED = 55
    const val CHANGES_WHEN_NFKC_CASEFOLDED = 56
    const val EMOJI = 57
    const val EMOJI_PRESENTATION = 58
    const val EMOJI_MODIFIER = 59
    const val EMOJI_MODIFIER_BASE = 60
    const val EMOJI_COMPONENT = 61
    const val REGIONAL_INDICATOR = 62
    const val PREPENDED_CONCATENATION_MARK = 63
    const val EXTENDED_PICTOGRAPHIC = 64
    const val BIDI_CLASS = 0x1000
    const val INT_START = BIDI_CLASS
    const val BLOCK = 0x1001
    const val CANONICAL_COMBINING_CLASS = 0x1002
    const val DECOMPOSITION_TYPE = 0x1003
    const val EAST_ASIAN_WIDTH = 0x1004
    const val GENERAL_CATEGORY = 0x1005
    const val JOINING_GROUP = 0x1006
    const val JOINING_TYPE = 0x1007
    const val LINE_BREAK = 0x1008
    const val NUMERIC_TYPE = 0x1009
    const val SCRIPT = 0x100A
    const val HANGUL_SYLLABLE_TYPE = 0x100B
    const val NFD_QUICK_CHECK = 0x100C
    const val NFKD_QUICK_CHECK = 0x100D
    const val NFC_QUICK_CHECK = 0x100E
    const val NFKC_QUICK_CHECK = 0x100F
    const val LEAD_CANONICAL_COMBINING_CLASS = 0x1010
    const val TRAIL_CANONICAL_COMBINING_CLASS = 0x1011
    const val GRAPHEME_CLUSTER_BREAK = 0x1012
    const val SENTENCE_BREAK = 0x1013
    const val WORD_BREAK = 0x1014
    const val BIDI_PAIRED_BRACKET_TYPE = 0x1015
    const val INDIC_POSITIONAL_CATEGORY = 0x1016
    const val INDIC_SYLLABIC_CATEGORY = 0x1017
    const val VERTICAL_ORIENTATION = 0x1018
    const val GENERAL_CATEGORY_MASK = 0x2000
    const val MASK_START = GENERAL_CATEGORY_MASK
    const val NUMERIC_VALUE = 0x3000
    const val DOUBLE_START = NUMERIC_VALUE
    const val AGE = 0x4000
    const val STRING_START = AGE
    const val BIDI_MIRRORING_GLYPH = 0x4001
    const val CASE_FOLDING = 0x4002
    const val LOWERCASE_MAPPING = 0x4004
    const val NAME = 0x4005
    const val SIMPLE_CASE_FOLDING = 0x4006
    const val SIMPLE_LOWERCASE_MAPPING = 0x4007
    const val SIMPLE_TITLECASE_MAPPING = 0x4008
    const val SIMPLE_UPPERCASE_MAPPING = 0x4009
    const val TITLECASE_MAPPING = 0x400A
    const val UPPERCASE_MAPPING = 0x400C
    const val BIDI_PAIRED_BRACKET = 0x400D
    const val SCRIPT_EXTENSIONS = 0x7000
    const val OTHER_PROPERTY_START = SCRIPT_EXTENSIONS
    const val INVALID_CODE   = -1

    /**
     * Returns whether the given codepoint has the given binary property.
     *
     * @param codePoint The codepoint.
     * @param property A binary property; one of the constants defined in [CharProperties].
     */
    fun codePointHasBinaryProperty(codePoint: Int, property: Int): Boolean {
        Stats.onNativeCall()
        return _nCodePointHasBinaryProperty(codePoint, property)
    }
}

@ExternalSymbolName("org_jetbrains_skia_icu_Unicode__1nCharDirection")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_icu_Unicode__1nCharDirection")
private external fun _nCharDirection(codePoint: Int): Int

@ExternalSymbolName("org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty")
private external fun _nCodePointHasBinaryProperty(codePoint: Int, property: Int): Boolean

