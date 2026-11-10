#include "common.h"

#ifdef SKIKO_USE_ICU

#include "third_party/externals/icu/source/common/unicode/uchar.h"

SKIKO_EXPORT KInt org_jetbrains_skia_icu_Unicode__1nCharDirection(KInt codePoint) {
    return u_charDirection(codePoint);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty(KInt codePoint, KInt property) {
    return u_hasBinaryProperty(codePoint, (UProperty)property);
}

#else

// ICU is excluded from this binary. The Unicode Character Database is not available,
// so the bidirectional category is served by Skiko's own embedded UAX #9 tables
// (`SkikoBidi`) and the binary properties are approximated with the code point ranges
// below. Callers can use `Unicode.isIcuAvailable` to detect this build.

#include "skunicode_client/SkikoBidi.h"

namespace {

// Values of ICU's UProperty (see unicode/uchar.h) that Skiko/Compose actually query.
constexpr KInt kPropertyBidiControl = 2;            // UCHAR_BIDI_CONTROL
constexpr KInt kPropertyWhiteSpace = 31;            // UCHAR_WHITE_SPACE
constexpr KInt kPropertyEmoji = 57;                 // UCHAR_EMOJI
constexpr KInt kPropertyEmojiPresentation = 58;     // UCHAR_EMOJI_PRESENTATION
constexpr KInt kPropertyEmojiModifier = 59;         // UCHAR_EMOJI_MODIFIER
constexpr KInt kPropertyEmojiModifierBase = 60;     // UCHAR_EMOJI_MODIFIER_BASE
constexpr KInt kPropertyEmojiComponent = 61;        // UCHAR_EMOJI_COMPONENT
constexpr KInt kPropertyRegionalIndicator = 62;     // UCHAR_REGIONAL_INDICATOR
constexpr KInt kPropertyExtendedPictographic = 64;  // UCHAR_EXTENDED_PICTOGRAPHIC

// UCharDirection values produced by SkikoBidi::CharDirection.
constexpr KInt kDirBlockSeparator = 7;
constexpr KInt kDirSegmentSeparator = 8;
constexpr KInt kDirWhiteSpaceNeutral = 9;

bool isRegionalIndicator(KInt c) {
    return c >= 0x1F1E6 && c <= 0x1F1FF;
}

// Approximation of Emoji=Yes (see emoji-data.txt): the symbol blocks that are entirely
// or mostly emoji, plus the scattered legacy code points.
bool isEmoji(KInt c) {
    switch (c) {
        case 0x0023: case 0x002A:
        case 0x00A9: case 0x00AE:
        case 0x203C: case 0x2049: case 0x2122: case 0x2139:
        case 0x2328: case 0x23CF: case 0x24C2:
        case 0x3030: case 0x303D: case 0x3297: case 0x3299:
            return true;
        default:
            break;
    }
    return (c >= 0x0030 && c <= 0x0039) ||
           (c >= 0x2194 && c <= 0x21AA) ||
           (c >= 0x231A && c <= 0x231B) ||
           (c >= 0x23E9 && c <= 0x23FA) ||
           (c >= 0x25AA && c <= 0x25FE) ||
           (c >= 0x2600 && c <= 0x27BF) ||
           (c >= 0x2934 && c <= 0x2935) ||
           (c >= 0x2B00 && c <= 0x2BFF) ||
           (c >= 0x1F000 && c <= 0x1FAFF);
}

// Approximation of Emoji_Presentation=Yes: the default emoji code points of the symbol
// blocks plus the pictographic planes.
bool isEmojiPresentation(KInt c) {
    switch (c) {
        case 0x23F0: case 0x23F3:
        case 0x267F: case 0x2693: case 0x26A1: case 0x26CE: case 0x26D4:
        case 0x26EA: case 0x26F5: case 0x26FA: case 0x26FD: case 0x2705:
        case 0x2728: case 0x274C: case 0x274E: case 0x2757: case 0x27B0:
        case 0x27BF: case 0x2B50: case 0x2B55:
            return true;
        default:
            break;
    }
    return (c >= 0x231A && c <= 0x231B) ||
           (c >= 0x23E9 && c <= 0x23EC) ||
           (c >= 0x25FD && c <= 0x25FE) ||
           (c >= 0x2614 && c <= 0x2615) ||
           (c >= 0x2648 && c <= 0x2653) ||
           (c >= 0x26AA && c <= 0x26AB) ||
           (c >= 0x26BD && c <= 0x26BE) ||
           (c >= 0x26C4 && c <= 0x26C5) ||
           (c >= 0x26F2 && c <= 0x26F3) ||
           (c >= 0x270A && c <= 0x270B) ||
           (c >= 0x2753 && c <= 0x2755) ||
           (c >= 0x2795 && c <= 0x2797) ||
           (c >= 0x2B1B && c <= 0x2B1C) ||
           isRegionalIndicator(c) ||
           (c >= 0x1F300 && c <= 0x1F5FF) ||
           (c >= 0x1F600 && c <= 0x1F64F) ||
           (c >= 0x1F680 && c <= 0x1F6FF) ||
           (c >= 0x1F900 && c <= 0x1F9FF) ||
           (c >= 0x1FA70 && c <= 0x1FAFF);
}

bool isEmojiModifier(KInt c) {
    return c >= 0x1F3FB && c <= 0x1F3FF;
}

bool isEmojiComponent(KInt c) {
    return c == 0x200D || c == 0x20E3 || c == 0xFE0F ||
           c == 0x0023 || c == 0x002A ||
           (c >= 0x0030 && c <= 0x0039) ||
           isRegionalIndicator(c) || isEmojiModifier(c) ||
           (c >= 0xE0020 && c <= 0xE007F);
}

}  // namespace

SKIKO_EXPORT KInt org_jetbrains_skia_icu_Unicode__1nCharDirection(KInt codePoint) {
    return SkikoBidi::CharDirection(codePoint);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty(KInt codePoint, KInt property) {
    switch (property) {
        case kPropertyWhiteSpace: {
            const KInt direction = SkikoBidi::CharDirection(codePoint);
            return direction == kDirWhiteSpaceNeutral ||
                   direction == kDirSegmentSeparator ||
                   direction == kDirBlockSeparator;
        }
        case kPropertyBidiControl:
            return (codePoint >= 0x202A && codePoint <= 0x202E) ||
                   (codePoint >= 0x2066 && codePoint <= 0x2069) ||
                   codePoint == 0x061C || codePoint == 0x200E || codePoint == 0x200F;
        case kPropertyEmoji:
            return isEmoji(codePoint);
        case kPropertyEmojiPresentation:
            return isEmojiPresentation(codePoint);
        case kPropertyEmojiModifier:
            return isEmojiModifier(codePoint);
        case kPropertyEmojiModifierBase:
            // Mostly the human pictographs; approximated by the pictographic planes.
            return codePoint >= 0x1F300 && codePoint <= 0x1FAFF;
        case kPropertyEmojiComponent:
            return isEmojiComponent(codePoint);
        case kPropertyRegionalIndicator:
            return isRegionalIndicator(codePoint);
        case kPropertyExtendedPictographic:
            // A superset of Emoji=Yes, without the ASCII characters that only become
            // emoji as part of a sequence.
            return codePoint > 0x00FF && isEmoji(codePoint);
        default:
            // Not derivable without the Unicode Character Database.
            return false;
    }
}

#endif  // SKIKO_USE_ICU

SKIKO_EXPORT KBoolean org_jetbrains_skia_icu_Unicode__1nIsIcuAvailable() {
#ifdef SKIKO_USE_ICU
    return true;
#else
    return false;
#endif
}
