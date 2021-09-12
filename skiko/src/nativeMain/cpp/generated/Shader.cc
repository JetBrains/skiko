
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkShader.h"
#include "SkGradientShader.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_Shader__1nMakeWithColorFilter
  (kref __Kinstance, jlong ptr, jlong filterPtr) {
    SkShader* instance = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(ptr));
    SkColorFilter* filter = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(filterPtr));
    SkShader* newPtr = instance->makeWithColorFilter(sk_ref_sp(filter)).release();
    return reinterpret_cast<jlong>(newPtr);
}


extern "C" jlong org_jetbrains_skia_Shader__1nMakeLinearGradient
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeLinearGradient");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeLinearGradient
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_Shader__1nMakeLinearGradientCS
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeLinearGradientCS");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeLinearGradientCS
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(posArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_Shader__1nMakeRadialGradient
  (kref __Kinstance, jfloat x, jfloat y, jfloat r, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeRadialGradient");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeRadialGradient
  (kref __Kinstance, jfloat x, jfloat y, jfloat r, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_Shader__1nMakeRadialGradientCS
  (kref __Kinstance, jfloat x, jfloat y, jfloat r, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeRadialGradientCS");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeRadialGradientCS
  (kref __Kinstance, jfloat x, jfloat y, jfloat r, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(posArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat r0, jfloat x1, jfloat y1, jfloat r1, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat r0, jfloat x1, jfloat y1, jfloat r1, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat r0, jfloat x1, jfloat y1, jfloat r1, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat r0, jfloat x1, jfloat y1, jfloat r1, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(posArray), tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_Shader__1nMakeSweepGradient
  (kref __Kinstance, jfloat x, jfloat y, jfloat start, jfloat end, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeSweepGradient");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeSweepGradient
  (kref __Kinstance, jfloat x, jfloat y, jfloat start, jfloat end, jintArray colorsArray, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, reinterpret_cast<SkColor*>(colors), pos, env->GetArrayLength(colorsArray), tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_Shader__1nMakeSweepGradientCS
  (kref __Kinstance, jfloat x, jfloat y, jfloat start, jfloat end, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeSweepGradientCS");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Shader__1nMakeSweepGradientCS
  (kref __Kinstance, jfloat x, jfloat y, jfloat start, jfloat end, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, env->GetArrayLength(colorsArray), tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_Shader__1nMakeEmpty(kref __Kinstance) {
    SkShader* ptr = SkShaders::Empty().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_Shader__1nMakeColor(kref __Kinstance, jint color) {
    SkShader* ptr = SkShaders::Color(color).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_Shader__1nMakeColorCS(kref __Kinstance, jfloat r, jfloat g, jfloat b, jfloat a, jlong colorSpacePtr) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkShader* ptr = SkShaders::Color(SkColor4f{r, g, b, a}, sk_ref_sp<SkColorSpace>(colorSpace)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_Shader__1nMakeBlend(kref __Kinstance, jint blendModeInt, jlong dstPtr, jlong srcPtr) {
    SkShader* dst = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(dstPtr));
    SkShader* src = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(srcPtr));
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkShader* ptr = SkShaders::Blend(blendMode, sk_ref_sp<SkShader>(dst), sk_ref_sp<SkShader>(src)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_Shader__1nMakeFractalNoise
  (jfloat baseFrequencyX, jfloat baseFrequencyY, jint numOctaves, jfloat seed, jintArray tilesArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeFractalNoise");
}
#if 0
extern "C" jlong org_jetbrains_skia_Shader__1nMakeFractalNoise
  (jfloat baseFrequencyX, jfloat baseFrequencyY, jint numOctaves, jfloat seed, jintArray tilesArray) {
    int len = env->GetArrayLength(tilesArray);
    std::vector<SkISize> tiles(len / 2);
    jint* arr = env->GetIntArrayElements(tilesArray, 0);
    for (int i = 0; i < len; i += 2)
        tiles[i / 2] = {arr[i], arr[i+1]};
    env->ReleaseIntArrayElements(tilesArray, arr, 0);
    SkShader* ptr = SkPerlinNoiseShader::MakeFractalNoise(baseFrequencyX, baseFrequencyY, numOctaves, seed, tiles.data()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif

extern "C" jlong org_jetbrains_skia_Shader__1nMakeTurbulence
  (jfloat baseFrequencyX, jfloat baseFrequencyY, jint numOctaves, jfloat seed, jintArray tilesArray) {
    TODO("implement org_jetbrains_skia_Shader__1nMakeTurbulence");
}
#if 0
extern "C" jlong org_jetbrains_skia_Shader__1nMakeTurbulence
  (JNIEnv* env, jclass jclass, jfloat baseFrequencyX, jfloat baseFrequencyY, jint numOctaves, jfloat seed, jintArray tilesArray) {
    int len = env->GetArrayLength(tilesArray);
    std::vector<SkISize> tiles(len / 2);
    jint* arr = env->GetIntArrayElements(tilesArray, 0);
    for (int i = 0; i < len; i += 2)
        tiles[i / 2] = {arr[i], arr[i+1]};
    env->ReleaseIntArrayElements(tilesArray, arr, 0);
    SkShader* ptr = SkPerlinNoiseShader::MakeTurbulence(baseFrequencyX, baseFrequencyY, numOctaves, seed, tiles.data()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif
