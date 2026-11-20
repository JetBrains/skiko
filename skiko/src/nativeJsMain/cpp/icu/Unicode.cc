#include "common.h"
#include "SkikoUnicode.hh"

SKIKO_EXPORT KInt org_jetbrains_skia_icu_Unicode__1nCharDirection(KInt codePoint) {
    return skiko::charDirection(codePoint);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty(KInt codePoint, KInt property) {
    return skiko::codePointHasBinaryProperty(codePoint, property);
}
