#include <iostream>
#include "SkColorFilter.h"
#include "SkShader.h"
#include "SkGradientShader.h"
#include "SkPerlinNoiseShader.h"
#include "SkSize.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeWithLocalMatrix
  (KNativePointer ptr, KFloat* localMatrixArr) {
    SkShader* instance = reinterpret_cast<SkShader*>((ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(localMatrixArr);
    SkShader* newPtr = instance->makeWithLocalMatrix(*localMatrix).release();
    return reinterpret_cast<KNativePointer>(newPtr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeWithColorFilter
  (KNativePointer ptr, KNativePointer filterPtr) {
    SkShader* instance = reinterpret_cast<SkShader*>((ptr));
    SkColorFilter* filter = reinterpret_cast<SkColorFilter*>((filterPtr));
    SkShader* newPtr = instance->makeWithColorFilter(sk_ref_sp(filter)).release();
    return reinterpret_cast<KNativePointer>(newPtr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeLinearGradient
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KInt* colorsArray, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    SkColor* colors = reinterpret_cast<SkColor*>(colorsArray);
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, colors, pos, count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeLinearGradientCS
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, colors, colorSpace, pos, count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeRadialGradient
  (KFloat x, KFloat y, KFloat r, KInt* colorsArray, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkColor* colors = reinterpret_cast<SkColor*>(colorsArray);
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, colors, pos, count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeRadialGradientCS
  (KFloat x, KFloat y, KFloat r, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, colors, colorSpace, pos, count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient
  (KFloat x0, KFloat y0, KFloat r0, KFloat x1, KFloat y1, KFloat r1, KInt* colorsArray, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkColor* colors = reinterpret_cast<SkColor*>(colorsArray);
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, colors, pos, count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS
  (KFloat x0, KFloat y0, KFloat r0, KFloat x1, KFloat y1, KFloat r1, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, colors, colorSpace, pos, count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeSweepGradient
  (KFloat x, KFloat y, KFloat start, KFloat end, KInt* colorsArray, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkColor* colors = reinterpret_cast<SkColor*>(colorsArray);
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, colors, pos, count, tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeSweepGradientCS
  (KFloat x, KFloat y, KFloat start, KFloat end, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt count, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = reinterpret_cast<float*>(posArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, colors, colorSpace, pos, count, tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeEmpty() {
    SkShader* ptr = SkShaders::Empty().release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeColor(KInt color) {
    SkShader* ptr = SkShaders::Color(color).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeColorCS(KFloat r, KFloat g, KFloat b, KFloat a, KNativePointer colorSpacePtr) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkShader* ptr = SkShaders::Color(SkColor4f{r, g, b, a}, sk_ref_sp<SkColorSpace>(colorSpace)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeBlend(KInt blendModeInt, KNativePointer dstPtr,  KNativePointer srcPtr) {
    SkShader* dst = reinterpret_cast<SkShader*>((dstPtr));
    SkShader* src = reinterpret_cast<SkShader*>((srcPtr));
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkShader* ptr = SkShaders::Blend(blendMode, sk_ref_sp<SkShader>(dst), sk_ref_sp<SkShader>(src)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeFractalNoise
  (KFloat baseFrequencyX, KFloat baseFrequencyY, KInt numOctaves, KFloat seed, KInt tileW, KInt tileH) {
    const SkISize tileSize = SkISize::Make(tileW, tileH);
    SkShader* ptr = SkShaders::MakeFractalNoise(baseFrequencyX, baseFrequencyY, numOctaves, seed, &tileSize).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTurbulence
  (KFloat baseFrequencyX, KFloat baseFrequencyY, KInt numOctaves, KFloat seed, KInt tileW, KInt tileH) {
    const SkISize tileSize = SkISize::Make(tileW, tileH);
    SkShader* ptr = SkShaders::MakeTurbulence(baseFrequencyX, baseFrequencyY, numOctaves, seed, &tileSize).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
