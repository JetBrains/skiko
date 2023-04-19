#include "common.h"
#include "third_party/externals/icu/source/common/unicode/uchar.h"

SKIKO_EXPORT KInt org_jetbrains_skia_paragraph_Direction_unicodeCharDirection(KInt codePoint) {
    return u_charDirection(codePoint);
}
