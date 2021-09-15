
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkShader.h"
#include "SkGradientShader.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeWithColorFilter
  (KNativePointer ptr, KNativePointer filterPtr) {
    SkShader* instance = reinterpret_cast<SkShader*>((ptr));
    SkColorFilter* filter = reinterpret_cast<SkColorFilter*>((filterPtr));
    SkShader* newPtr = instance->makeWithColorFilter(sk_ref_sp(filter)).release();
    return reinterpret_cast<KNativePointer>(newPtr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeLinearGradient
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeLinearGradient");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeLinearGradient
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    KInt* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeLinearGradientCS
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeLinearGradientCS");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeLinearGradientCS
  (KFloat x0, KFloat y0, KFloat x1, KFloat y1, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(posArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeRadialGradient
  (KFloat x, KFloat y, KFloat r, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeRadialGradient");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeRadialGradient
  (KFloat x, KFloat y, KFloat r, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    KInt* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeRadialGradientCS
  (KFloat x, KFloat y, KFloat r, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeRadialGradientCS");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeRadialGradientCS
  (KFloat x, KFloat y, KFloat r, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(posArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient
  (KFloat x0, KFloat y0, KFloat r0, KFloat x1, KFloat y1, KFloat r1, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient
  (KFloat x0, KFloat y0, KFloat r0, KFloat x1, KFloat y1, KFloat r1, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    KInt* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS
  (KFloat x0, KFloat y0, KFloat r0, KFloat x1, KFloat y1, KFloat r1, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS
  (KFloat x0, KFloat y0, KFloat r0, KFloat x1, KFloat y1, KFloat r1, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(posArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeSweepGradient
  (KFloat x, KFloat y, KFloat start, KFloat end, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeSweepGradient");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeSweepGradient
  (KFloat x, KFloat y, KFloat start, KFloat end, KInt* colorsArray, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    KInt* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeSweepGradientCS
  (KFloat x, KFloat y, KFloat start, KFloat end, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeSweepGradientCS");
}
     
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeSweepGradientCS
  (KFloat x, KFloat y, KFloat start, KFloat end, KFloat* colorsArray, KNativePointer colorSpacePtr, KFloat* posArray, KInt tileModeInt, KInt flags, KFloat* matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(colorsArray), tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeEmpty(KInteropPointer __Kinstance) {
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

extern "C" KNativePointer org_jetbrains_skia_Shader__1nMakeBlend(KInt blendModeInt, KNativePointer dstPtr,  KNativePointer srcPtr) {
    SkShader* dst = reinterpret_cast<SkShader*>((dstPtr));
    SkShader* src = reinterpret_cast<SkShader*>((srcPtr));
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkShader* ptr = SkShaders::Blend(blendMode, sk_ref_sp<SkShader>(dst), sk_ref_sp<SkShader>(src)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeFractalNoise
  (KFloat baseFrequencyX, KFloat baseFrequencyY, KInt numOctaves, KFloat seed, KInt* tilesArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeFractalNoise");
}
#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeFractalNoise
  (KFloat baseFrequencyX, KFloat baseFrequencyY, KInt numOctaves, KFloat seed, KInt* tilesArray) {
    int len = env->GetArrayLength(tilesArray);
    std::vector<SkISize> tiles(len / 2);
    KInt* arr = env->GetIntArrayElements(tilesArray, 0);
    for (int i = 0; i < len; i += 2)
        tiles[i / 2] = {arr[i], arr[i+1]};
    env->ReleaseIntArrayElements(tilesArray, arr, 0);
    SkShader* ptr = SkPerlinNoiseShader::MakeFractalNoise(baseFrequencyX, baseFrequencyY, numOctaves, seed, tiles.data()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTurbulence
  (KFloat baseFrequencyX, KFloat baseFrequencyY, KInt numOctaves, KFloat seed, KInt* tilesArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeTurbulence");
}
#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Shader__1nMakeTurbulence
  (JNIEnv* env, jclass jclass, KFloat baseFrequencyX, KFloat baseFrequencyY, KInt numOctaves, KFloat seed, KInt* tilesArray) {
    int len = env->GetArrayLength(tilesArray);
    std::vector<SkISize> tiles(len / 2);
    KInt* arr = env->GetIntArrayElements(tilesArray, 0);
    for (int i = 0; i < len; i += 2)
        tiles[i / 2] = {arr[i], arr[i+1]};
    env->ReleaseIntArrayElements(tilesArray, arr, 0);
    SkShader* ptr = SkPerlinNoiseShader::MakeTurbulence(baseFrequencyX, baseFrequencyY, numOctaves, seed, tiles.data()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif
