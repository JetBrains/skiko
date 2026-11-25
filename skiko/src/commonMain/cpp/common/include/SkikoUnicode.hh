#pragma once

#include "SkRefCnt.h"
#include "SkTypes.h"
#include "SkUnicode.h"

#if defined(SK_UNICODE_APPLE_IMPLEMENTATION)
#include "SkUnicode_apple.h"
#endif

#if defined(SK_UNICODE_ICU_IMPLEMENTATION)
#include "SkUnicode_icu.h"
#include "unicode/uchar.h"
#endif

namespace skiko {

// The SkUnicode implementation skiko is linked with, mirroring the backend the Skia build
// produced for this target: the native Apple unicode backend on macOS/iOS/tvOS, ICU elsewhere.
inline sk_sp<SkUnicode> BestAvailableUnicode() {
#if defined(SK_UNICODE_APPLE_IMPLEMENTATION)
    static sk_sp<SkUnicode> unicode = SkUnicodes::Apple::Make();
    return unicode;
#elif defined(SK_UNICODE_ICU_IMPLEMENTATION)
    static sk_sp<SkUnicode> unicode = SkUnicodes::ICU::Make();
    return unicode;
#else
#error "skiko requires an SkUnicode implementation (apple unicode or icu)"
#endif
}

// Unicode Bidi_Class of a code point, numbered like ICU's UCharDirection.
inline int charDirection(SkUnichar unichar) {
    return static_cast<int>(BestAvailableUnicode()->charDirection(unichar));
}

// Unicode binary property lookup by ICU UProperty number (as exposed by
// org.jetbrains.skia.icu.CharProperties). On ICU targets it is exact; the Apple unicode
// backend implements the subset SkUnicode exposes and reports false for the rest.
inline bool codePointHasBinaryProperty(SkUnichar unichar, int property) {
#if defined(SK_UNICODE_ICU_IMPLEMENTATION)
    return u_hasBinaryProperty(unichar, static_cast<UProperty>(property));
#else
    auto unicode = BestAvailableUnicode();
    switch (property) {
        case 5:   // DEFAULT_IGNORABLE_CODE_POINT
            return unicode->isDefaultIgnorable(unichar);
        case 17:  // IDEOGRAPHIC
            return unicode->isIdeographic(unichar);
        case 57:  // EMOJI
            return unicode->isEmoji(unichar);
        case 58:  // EMOJI_PRESENTATION
            return unicode->isEmojiPresentation(unichar);
        case 59:  // EMOJI_MODIFIER
            return unicode->isEmojiModifier(unichar);
        case 60:  // EMOJI_MODIFIER_BASE
            return unicode->isEmojiModifierBase(unichar);
        case 61:  // EMOJI_COMPONENT
            return unicode->isEmojiComponent(unichar);
        case 62:  // REGIONAL_INDICATOR
            return unicode->isRegionalIndicator(unichar);
        case 64:  // EXTENDED_PICTOGRAPHIC
            return unicode->isExtendedPictographic(unichar);
        default:
            return false;
    }
#endif
}

}  // namespace skiko
