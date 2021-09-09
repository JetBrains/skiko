#ifndef SKIKO_COMMON_H
#define SKIKO_COMMON_H

#include "SkCodec.h"
#include "SkFontMetrics.h"
#include "SkFontStyle.h"
#include "SkImageInfo.h"
#include "SkMatrix.h"
#include "SkM44.h"
#include "SkPaint.h"
#include "SkRefCnt.h"
#include "SkRect.h"
#include "SkRRect.h"
#include "SkScalar.h"
#include "SkShaper.h"
#include "SkString.h"
#include "SkSurfaceProps.h"
#include <stdexcept>
#include "types.h"

jlong packTwoInts(int32_t a, int32_t b);

jlong packIPoint(SkIPoint p);

jlong packISize(SkISize p);

namespace skija {
    namespace SamplingMode {
        SkSamplingOptions unpack(jlong val);
    }

    class UtfIndicesConverter {
    public:
        UtfIndicesConverter(const char* chars8, size_t len8);
        UtfIndicesConverter(const SkString& s);

        const char* fStart8;
        const char* fPtr8;
        const char* fEnd8;
        uint32_t fPos16;

        size_t from16To8(uint32_t i16);
        uint32_t from8To16(size_t i8);
    };
}


template <typename T>
inline T jlongToPtr(jlong ptr) {
    return reinterpret_cast<T>(static_cast<uintptr_t>(ptr));
}

template <typename T>
jlong ptrToJlong(T* ptr) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr));
}

void TODO(const char*);

#endif /* SKIKO_COMMON_H */

