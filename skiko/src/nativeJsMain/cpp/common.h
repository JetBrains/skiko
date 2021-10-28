#ifndef SKIKO_COMMON_H
#define SKIKO_COMMON_H

// Note that this file is only common between Wasm and Native targets, JVM uses different headers.

#include <stdexcept>

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

#include "types.h"

KLong packTwoInts(KInt a, KInt b);

KLong packIPoint(SkIPoint p);

KLong packISize(SkISize p);

namespace skija {
    namespace SamplingMode {
        SkSamplingOptions unpack(KLong val);
        SkSamplingOptions unpackFrom2Ints(KInt val1, KInt val2);
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

    namespace RRect {
        SkRRect toSkRRect(KFloat left, KFloat top, KFloat right, KFloat bottom, KFloat* jradii, KInt size);
    }

    namespace FontStyle {
        SkFontStyle fromKotlin(KInt style);
        KInt toKotlin(const SkFontStyle& fs);
    }

    namespace ImageInfo {
       void writeImageInfoForInterop(SkImageInfo imageInfo, KInt* imageInfoResult, KNativePointer* colorSpacePtrsArray);
    }

    namespace SurfaceProps {
        std::unique_ptr<SkSurfaceProps> toSkSurfaceProps(KInt* surfacePropsInts);
    }
}

std::unique_ptr<SkMatrix> skMatrix(KFloat* matrixArray);
std::unique_ptr<SkM44> skM44(KFloat* matrixArray);
SkString skString(KNativePointer str);
std::vector<SkString> skStringVector(KInteropPointerArray arr, KInt size);

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

#ifdef SKIKO_WASM
#include <emscripten.h>
#define SKIKO_EXPORT EMSCRIPTEN_KEEPALIVE extern "C"
#else
#define SKIKO_EXPORT extern "C"
#endif

static inline KInt rawBits(KFloat f) {
    union {
        KFloat f;
        KInt i;
    } u;
    u.f = f;
    return u.i;
}
#endif /* SKIKO_COMMON_H */

