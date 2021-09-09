#include "common.h"
#include "src/utils/SkUTF.h"
#include <stdio.h>

jlong packTwoInts(int32_t a, int32_t b) {
    return (uint64_t (a) << 32) | b;
}

jlong packIPoint(SkIPoint p) {
    return packTwoInts(p.fX, p.fY);
}

jlong packISize(SkISize p) {
    return packTwoInts(p.fWidth, p.fHeight);
}

namespace skija {

    namespace SamplingMode {
        SkSamplingOptions unpack(jlong val) {
            if (0x8000000000000000 & val) {
                val = val & 0x7FFFFFFFFFFFFFFF;
                float* ptr = reinterpret_cast<float*>(&val);
                return SkSamplingOptions(SkCubicResampler {ptr[1], ptr[0]});
            } else {
                int32_t filter = (int32_t) ((val >> 32) & 0xFFFFFFFF);
                int32_t mipmap = (int32_t) (val & 0xFFFFFFFF);
                return SkSamplingOptions(static_cast<SkFilterMode>(filter), static_cast<SkMipmapMode>(mipmap));
            }
        }
    }
}

skija::UtfIndicesConverter::UtfIndicesConverter(const char* chars8, size_t len8):
  fStart8(chars8),
  fPtr8(chars8),
  fEnd8(chars8 + len8),
  fPos16(0)
{}

skija::UtfIndicesConverter::UtfIndicesConverter(const SkString& str):
  skija::UtfIndicesConverter::UtfIndicesConverter(str.c_str(), str.size())
{}

size_t skija::UtfIndicesConverter::from16To8(uint32_t i16) {
    if (i16 >= fPos16) {
        // if new i16 >= last fPos16, continue from where we started
    } else {
        fPtr8 = fStart8;
        fPos16 = 0;
    }

    while (fPtr8 < fEnd8 && fPos16 < i16) {
        SkUnichar u = SkUTF::NextUTF8(&fPtr8, fEnd8);
        fPos16 += (uint32_t) SkUTF::ToUTF16(u);
    }

    return fPtr8 - fStart8;
}

uint32_t skija::UtfIndicesConverter::from8To16(size_t i8) {
    if (i8 >= (size_t) (fPtr8 - fStart8)) {
        // if new i8 >= last fPtr8, continue from where we started
    } else {
        fPtr8 = fStart8;
        fPos16 = 0;
    }

    while (fPtr8 < fEnd8 && (size_t) (fPtr8 - fStart8) < i8) {
        SkUnichar u = SkUTF::NextUTF8(&fPtr8, fEnd8);
        fPos16 += (uint32_t) SkUTF::ToUTF16(u);
    }

    return fPos16;
}

void TODO(const char* message) {
    printf("TODO: %s\n", message);
    fflush(stdout);
    abort();
}

