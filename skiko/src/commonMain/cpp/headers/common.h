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

KLong packTwoInts(KInt a, KInt b);

KLong packIPoint(SkIPoint p);

KLong packISize(SkISize p);

namespace skija {
    namespace SamplingMode {
        SkSamplingOptions unpack(KLong val);
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
inline T interopToPtr(KNativePointer ptr) {
    return reinterpret_cast<T>(ptr);
}

template <typename T>
KNativePointer ptrToInterop(T* ptr) {
    return ptr;
}

#ifdef __clang__
__attribute__((noreturn))
void TODO(const char*);
#else
void TODO(const char*);
#endif

#endif /* SKIKO_COMMON_H */

