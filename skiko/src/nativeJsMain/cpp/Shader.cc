#include <iostream>
#include "SkColorFilter.h"
#include "SkGradient.h"
#include "SkPerlinNoiseShader.h"
#include "SkShader.h"
#include "SkSize.h"
#include "common.h"

static SkGradient makeGradient(const SkColor4f* colors,
                               sk_sp<SkColorSpace> colorSpace,
                               const float* positions,
                               int count,
                               SkTileMode tileMode,
                               KInt inPremul,
                               KInt interpolationColorSpace,
                               KInt hueMethod) {
    SkGradient::Interpolation interpolation{
        inPremul == 0 ? SkGradient::Interpolation::InPremul::kNo
                      : SkGradient::Interpolation::InPremul::kYes,
        static_cast<SkGradient::Interpolation::ColorSpace>(interpolationColorSpace),
        static_cast<SkGradient::Interpolation::HueMethod>(hueMethod)
    };
    return SkGradient(
        SkGradient::Colors(
            SkSpan<const SkColor4f>(colors, count),
            positions == nullptr ? SkSpan<const float>() : SkSpan<const float>(positions, count),
            tileMode,
            std::move(colorSpace)),
        interpolation);
}

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
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* positionsArray, KInt count, KInt tileModeInt, KInt inPremul, KInt interpolationColorSpace, KInt hueMethod, KFloat* matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* positions = reinterpret_cast<float*>(positionsArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkGradient gradient = makeGradient(colors, colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::LinearGradient(pts, gradient, localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeRadialGradient
  (KFloat x, KFloat y, KFloat radius, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* positionsArray, KInt count, KInt tileModeInt, KInt inPremul, KInt interpolationColorSpace, KInt hueMethod, KFloat* matrixArray) {
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* positions = reinterpret_cast<float*>(positionsArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkGradient gradient = makeGradient(colors, colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::RadialGradient(SkPoint::Make(x, y), radius, gradient, localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient
  (KFloat x0, KFloat y0, KFloat startRadius, KFloat x1, KFloat y1, KFloat endRadius, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* positionsArray, KInt count, KInt tileModeInt, KInt inPremul, KInt interpolationColorSpace, KInt hueMethod, KFloat* matrixArray) {
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* positions = reinterpret_cast<float*>(positionsArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkGradient gradient = makeGradient(colors, colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::TwoPointConicalGradient(SkPoint::Make(x0, y0), startRadius, SkPoint::Make(x1, y1), endRadius, gradient, localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeSweepGradient
  (KFloat x, KFloat y, KFloat startAngle, KFloat endAngle, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* positionsArray, KInt count, KInt tileModeInt, KInt inPremul, KInt interpolationColorSpace, KInt hueMethod, KFloat* matrixArray) {
    SkColor4f* colors = reinterpret_cast<SkColor4f*>(colorsArray);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* positions = reinterpret_cast<float*>(positionsArray);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(matrixArray);
    SkGradient gradient = makeGradient(colors, colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::SweepGradient(SkPoint::Make(x, y), startAngle, endAngle, gradient, localMatrix.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

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
