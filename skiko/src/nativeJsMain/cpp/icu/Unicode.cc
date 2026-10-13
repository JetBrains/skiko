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

// ICU is excluded from this binary. The Unicode Character Database is not
// available, so only a coarse approximation of the bidirectional category is
// reported and binary properties are always unset. Callers should use
// `Unicode.isIcuAvailable` to detect this and ask the browser instead.
namespace {

// Values of ICU's UCharDirection, kept in sync with org.jetbrains.skia.icu.Unicode.
constexpr KInt kLeftToRight = 0;
constexpr KInt kRightToLeft = 1;
constexpr KInt kRightToLeftArabic = 13;

bool isRightToLeft(KInt c) {
    return (c >= 0x0590 && c <= 0x05FF) ||
           (c >= 0x07C0 && c <= 0x085F) ||
           (c >= 0xFB1D && c <= 0xFB4F) ||
           (c >= 0x10800 && c <= 0x10FFF) ||
           (c >= 0x1E800 && c <= 0x1EC6F);
}

bool isArabic(KInt c) {
    return (c >= 0x0600 && c <= 0x07BF) ||
           (c >= 0x0860 && c <= 0x08FF) ||
           (c >= 0xFB50 && c <= 0xFDFF) ||
           (c >= 0xFE70 && c <= 0xFEFF) ||
           (c >= 0x1EC70 && c <= 0x1ECBF) ||
           (c >= 0x1ED00 && c <= 0x1ED4F) ||
           (c >= 0x1EE00 && c <= 0x1EEFF);
}

}  // namespace

SKIKO_EXPORT KInt org_jetbrains_skia_icu_Unicode__1nCharDirection(KInt codePoint) {
    if (isArabic(codePoint)) return kRightToLeftArabic;
    if (isRightToLeft(codePoint)) return kRightToLeft;
    return kLeftToRight;
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty(KInt codePoint, KInt property) {
    return false;
}

#endif  // SKIKO_USE_ICU

SKIKO_EXPORT KBoolean org_jetbrains_skia_icu_Unicode__1nIsIcuAvailable() {
#ifdef SKIKO_USE_ICU
    return true;
#else
    return false;
#endif
}
