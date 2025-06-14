#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "SkColorFilter.h"
#include "SkShader.h"
#include "SkGradientShader.h"
#include "SkPerlinNoiseShader.h"
#include "SkSize.h"
#include "interop.hh"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeWithLocalMatrix
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray localMatrixArr) {
    SkShader* instance = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    SkShader* newPtr = instance->makeWithLocalMatrix(*localMatrix).release();
    return reinterpret_cast<jlong>(newPtr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeWithColorFilter
  (JNIEnv* env, jclass jclass, jlong ptr, jlong filterPtr) {
    SkShader* instance = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(ptr));
    SkColorFilter* filter = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(filterPtr));
    SkShader* newPtr = instance->makeWithColorFilter(sk_ref_sp(filter)).release();
    return reinterpret_cast<jlong>(newPtr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeLinearGradient
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jintArray colorsArray, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, reinterpret_cast<SkColor*>(colors), pos, _count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeLinearGradientCS
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeLinear(pts, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, _count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeRadialGradient
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat r, jintArray colorsArray, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, reinterpret_cast<SkColor*>(colors), pos, _count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeRadialGradientCS
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat r, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeRadial(SkPoint::Make(x, y), r, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, _count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeTwoPointConicalGradient
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat r0, jfloat x1, jfloat y1, jfloat r1, jintArray colorsArray, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, reinterpret_cast<SkColor*>(colors), pos, _count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeTwoPointConicalGradientCS
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat r0, jfloat x1, jfloat y1, jfloat r1, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeTwoPointConical(SkPoint::Make(x0, y0), r0, SkPoint::Make(x1, y1), r1, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, _count, tileMode, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeSweepGradient
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat start, jfloat end, jintArray colorsArray, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    jint* colors = env->GetIntArrayElements(colorsArray, nullptr);
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, reinterpret_cast<SkColor*>(colors), pos, _count, tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseIntArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeSweepGradientCS
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat start, jfloat end, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray posArray, jint _count, jint tileModeInt, jint flags, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* pos = posArray == nullptr ? nullptr : env->GetFloatArrayElements(posArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkShader* ptr = SkGradientShader::MakeSweep(x, y, reinterpret_cast<SkColor4f*>(colors), colorSpace, pos, _count, tileMode, start, end, static_cast<uint32_t>(flags), localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (posArray != nullptr)
        env->ReleaseFloatArrayElements(posArray, pos, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt_Shader_1nMakeEmpty(JNIEnv* env, jclass jclass) {
    SkShader* ptr = SkShaders::Empty().release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeColor(JNIEnv* env, jclass jclass, jint color) {
    SkShader* ptr = SkShaders::Color(color).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeColorCS(JNIEnv* env, jclass jclass, jfloat r, jfloat g, jfloat b, jfloat a, jlong colorSpacePtr) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkShader* ptr = SkShaders::Color(SkColor4f{r, g, b, a}, sk_ref_sp<SkColorSpace>(colorSpace)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeBlend(JNIEnv* env, jclass jclass, jint blendModeInt, jlong dstPtr, jlong srcPtr) {
    SkShader* dst = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(dstPtr));
    SkShader* src = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(srcPtr));
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkShader* ptr = SkShaders::Blend(blendMode, sk_ref_sp<SkShader>(dst), sk_ref_sp<SkShader>(src)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeFractalNoise
  (JNIEnv* env, jclass jclass, jfloat baseFrequencyX, jfloat baseFrequencyY, jint numOctaves, jfloat seed, jint tileW, jint tileH) {
    const SkISize tileSize = SkISize::Make(tileW, tileH);
    SkShader* ptr = SkShaders::MakeFractalNoise(baseFrequencyX, baseFrequencyY, numOctaves, seed, &tileSize).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeTurbulence
  (JNIEnv* env, jclass jclass, jfloat baseFrequencyX, jfloat baseFrequencyY, jint numOctaves, jfloat seed, jint tileW, jint tileH) {
    const SkISize tileSize = SkISize::Make(tileW, tileH);
    SkShader* ptr = SkShaders::MakeTurbulence(baseFrequencyX, baseFrequencyY, numOctaves, seed, &tileSize).release();
    return reinterpret_cast<jlong>(ptr);
}
