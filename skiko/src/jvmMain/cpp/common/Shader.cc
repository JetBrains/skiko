#include <iostream>
#include <jni.h>
#include "SkColorFilter.h"
#include "SkGradient.h"
#include "SkPerlinNoiseShader.h"
#include "SkShader.h"
#include "SkSize.h"
#include "interop.hh"

static SkGradient makeGradient(const SkColor4f* colors,
                               sk_sp<SkColorSpace> colorSpace,
                               const float* positions,
                               int count,
                               SkTileMode tileMode,
                               jint inPremul,
                               jint interpolationColorSpace,
                               jint hueMethod) {
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
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat x1, jfloat y1, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray positionsArray, jint count, jint tileModeInt, jint inPremul, jint interpolationColorSpace, jint hueMethod, jfloatArray matrixArray) {
    SkPoint pts[2] {SkPoint::Make(x0, y0), SkPoint::Make(x1, y1)};
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* positions = positionsArray == nullptr ? nullptr : env->GetFloatArrayElements(positionsArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkGradient gradient = makeGradient(reinterpret_cast<SkColor4f*>(colors), colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::LinearGradient(pts, gradient, localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (positionsArray != nullptr)
        env->ReleaseFloatArrayElements(positionsArray, positions, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeRadialGradient
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat radius, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray positionsArray, jint count, jint tileModeInt, jint inPremul, jint interpolationColorSpace, jint hueMethod, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* positions = positionsArray == nullptr ? nullptr : env->GetFloatArrayElements(positionsArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkGradient gradient = makeGradient(reinterpret_cast<SkColor4f*>(colors), colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::RadialGradient(SkPoint::Make(x, y), radius, gradient, localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (positionsArray != nullptr)
        env->ReleaseFloatArrayElements(positionsArray, positions, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeTwoPointConicalGradient
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat startRadius, jfloat x1, jfloat y1, jfloat endRadius, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray positionsArray, jint count, jint tileModeInt, jint inPremul, jint interpolationColorSpace, jint hueMethod, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* positions = positionsArray == nullptr ? nullptr : env->GetFloatArrayElements(positionsArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkGradient gradient = makeGradient(reinterpret_cast<SkColor4f*>(colors), colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::TwoPointConicalGradient(SkPoint::Make(x0, y0), startRadius, SkPoint::Make(x1, y1), endRadius, gradient, localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (positionsArray != nullptr)
        env->ReleaseFloatArrayElements(positionsArray, positions, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ShaderKt__1nMakeSweepGradient
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat startAngle, jfloat endAngle, jfloatArray colorsArray, jlong colorSpacePtr, jfloatArray positionsArray, jint count, jint tileModeInt, jint inPremul, jint interpolationColorSpace, jint hueMethod, jfloatArray matrixArray) {
    float* colors = env->GetFloatArrayElements(colorsArray, nullptr);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    float* positions = positionsArray == nullptr ? nullptr : env->GetFloatArrayElements(positionsArray, nullptr);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, matrixArray);
    SkGradient gradient = makeGradient(reinterpret_cast<SkColor4f*>(colors), colorSpace, positions, count, tileMode, inPremul, interpolationColorSpace, hueMethod);
    SkShader* ptr = SkShaders::SweepGradient(SkPoint::Make(x, y), startAngle, endAngle, gradient, localMatrix.get()).release();
    env->ReleaseFloatArrayElements(colorsArray, colors, 0);
    if (positionsArray != nullptr)
        env->ReleaseFloatArrayElements(positionsArray, positions, 0);
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
