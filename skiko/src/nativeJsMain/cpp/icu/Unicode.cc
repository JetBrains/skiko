#include "common.h"
#include "third_party/externals/icu/source/common/unicode/uchar.h"

SKIKO_EXPORT KInt org_jetbrains_skia_icu_Unicode__1nCharDirection(KInt codePoint) {
    return u_charDirection(codePoint);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty(KInt codePoint, KInt property) {
    return u_hasBinaryProperty(codePoint, (UProperty)property);
}
