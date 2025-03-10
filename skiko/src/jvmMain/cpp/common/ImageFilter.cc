#include <iostream>
#include <jni.h>
#include "SkSamplingOptions.h"
#include "interop.hh"
#include "SkColorFilter.h"
#include "SkImageFilter.h"
#include "SkImageFilters.h"
#include "SkPoint3.h"
#include "SkRect.h"
#include "interop.hh"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeArithmetic
  (JNIEnv* env, jclass jclass, jfloat k1, jfloat k2, jfloat k3, jfloat k4, jboolean enforcePMColor, jlong bgPtr, jlong fgPtr, jintArray cropInts) {
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::Arithmetic(k1, k2, k3, k4, enforcePMColor, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeBlend
  (JNIEnv* env, jclass jclass, jint blendModeInt, jlong bgPtr, jlong fgPtr, jintArray cropInts) {
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::Blend(blendMode, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeBlur
  (JNIEnv* env, jclass jclass, jfloat sigmaX, jfloat sigmaY, jint tileModeInt, jlong inputPtr, jintArray cropInts) {
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::Blur(sigmaX, sigmaY, tileMode, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeColorFilter
  (JNIEnv* env, jclass jclass, jlong colorFilterPtr, jlong inputPtr, jintArray cropInts) {
    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(colorFilterPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::ColorFilter(sk_ref_sp(colorFilter), sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeCompose
  (JNIEnv* env, jclass jclass, jlong outerPtr, jlong innerPtr) {
    SkImageFilter* outer = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(outerPtr));
    SkImageFilter* inner = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(innerPtr));
    SkImageFilter* ptr = SkImageFilters::Compose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeDisplacementMap
  (JNIEnv* env, jclass jclass, jint xChanInt, jint yChanInt, jfloat scale, jlong displacementPtr, jlong colorPtr, jintArray cropInts) {
    SkColorChannel xChan = static_cast<SkColorChannel>(xChanInt);
    SkColorChannel yChan = static_cast<SkColorChannel>(yChanInt);
    SkImageFilter* displacement = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(displacementPtr));
    SkImageFilter* color = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(colorPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::DisplacementMap(xChan, yChan, scale, sk_ref_sp(displacement), sk_ref_sp(color), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeDropShadow
  (JNIEnv* env, jclass jclass, jfloat dx, jfloat dy, jfloat sigmaX, jfloat sigmaY, jint color, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::DropShadow(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeDropShadowOnly
  (JNIEnv* env, jclass jclass, jfloat dx, jfloat dy, jfloat sigmaX, jfloat sigmaY, jint color, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::DropShadowOnly(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeImage
  (JNIEnv* env, jclass jclass, jlong imagePtr, jfloat l0, jfloat t0, jfloat r0, jfloat b0, jfloat l1, jfloat t1, jfloat r1, jfloat b1, jint samplingModeVal1, jint samplingModeVal2) {
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(imagePtr));
    SkImageFilter* ptr = SkImageFilters::Image(sk_ref_sp(image), SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, skija::SamplingMode::unpackFrom2Ints(env, samplingModeVal1, samplingModeVal2)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeMagnifier
  (JNIEnv* env, jclass jclass, jfloat l, jfloat t, jfloat r, jfloat b, jfloat zoomAmount, jfloat inset, jint samplingModeVal1, jint samplingModeVal2, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkSamplingOptions sampling = skija::SamplingMode::unpackFrom2Ints(env, samplingModeVal1, samplingModeVal2);
    SkImageFilter* ptr = SkImageFilters::Magnifier(SkRect{l, t, r, b}, zoomAmount, inset, sampling, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeMatrixConvolution
  (JNIEnv* env, jclass jclass, jint kernelW, jint kernelH, jfloatArray kernelArray, jfloat gain, jfloat bias, jint offsetX, jint offsetY, jint tileModeInt, jboolean convolveAlpha, jlong inputPtr, jintArray cropInts) {
    jfloat* kernel = env->GetFloatArrayElements(kernelArray, 0);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::MatrixConvolution(SkISize{kernelW, kernelH}, kernel, gain, bias, SkIPoint{offsetX, offsetY}, tileMode, convolveAlpha, sk_ref_sp(input), crop.get()).release();
    env->ReleaseFloatArrayElements(kernelArray, kernel, 0);
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeMatrixTransform
  (JNIEnv* env, jclass jclass, jfloatArray matrixArray, jint samplingModeVal1, jint samplingModeVal2, jlong inputPtr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArray);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    SkImageFilter* ptr = SkImageFilters::MatrixTransform(*matrix, skija::SamplingMode::unpackFrom2Ints(env, samplingModeVal1, samplingModeVal2), sk_ref_sp(input)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeMerge
  (JNIEnv* env, jclass jclass, jlongArray filtersArray, jint filtersArraySize, jintArray cropInts) {
    jlong* f = env->GetLongArrayElements(filtersArray, 0);
    std::vector<sk_sp<SkImageFilter>> filters(filtersArraySize);
    for (int i = 0; i < filtersArraySize; ++i) {
        SkImageFilter* fi = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(f[i]));
        filters[i] = sk_ref_sp(fi);
    }
    env->ReleaseLongArrayElements(filtersArray, f, 0);
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::Merge(filters.data(), filtersArraySize, crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeOffset
  (JNIEnv* env, jclass jclass, jfloat dx, jfloat dy, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::Offset(dx, dy, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeShader
  (JNIEnv* env, jclass jclass, jlong shaderPtr, jboolean ditherBoolean, jintArray cropInts) {
    SkShader* shader = reinterpret_cast<SkShader*>(static_cast<uintptr_t>(shaderPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilters::Dither dither = ditherBoolean ? SkImageFilters::Dither::kYes : SkImageFilters::Dither::kNo;
    SkImageFilter* ptr = SkImageFilters::Shader(sk_ref_sp(shader), dither, crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakePicture
  (JNIEnv* env, jclass jclass, jlong picturePtr, jfloat l, jfloat t, jfloat r, jfloat b) {
    SkPicture* picture = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(picturePtr));
    SkImageFilter* ptr = SkImageFilters::Picture(sk_ref_sp(picture), SkRect{l, t, r, b}).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeRuntimeShader
  (JNIEnv* env, jclass jclass, jlong runtimeShaderBuilderPtr, jstring childShaderName, jlong inputPtr) {
    SkRuntimeEffectBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeEffectBuilder*>(static_cast<uintptr_t>(runtimeShaderBuilderPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));

    SkImageFilter* ptr = SkImageFilters::RuntimeShader(*runtimeShaderBuilder, skString(env, childShaderName).c_str(), sk_ref_sp(input)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeRuntimeShaderFromArray
  (JNIEnv* env, jclass jclass, jlong runtimeShaderBuilderPtr, jobjectArray childShaderNamesArr, jlongArray inputPtrsArray, jint _inputCount) {
    SkRuntimeEffectBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeEffectBuilder*>(static_cast<uintptr_t>(runtimeShaderBuilderPtr));

    jsize inputCount = env->GetArrayLength(inputPtrsArray);
    jlong* inputPtrs = env->GetLongArrayElements(inputPtrsArray, 0);
    std::vector<sk_sp<SkImageFilter>> inputChildren(inputCount);
    for (int i = 0; i < inputCount; ++i) {
        SkImageFilter* filter = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtrs[i]));
        inputChildren[i] = sk_ref_sp(filter);
    }
    env->ReleaseLongArrayElements(inputPtrsArray, inputPtrs, 0);

    std::vector<SkString> childShaderNameStrings = skStringVector(env, childShaderNamesArr);
    std::vector<std::string_view> childShaderNames(childShaderNameStrings.size());
    for (int i = 0; i < inputCount; ++i)
        childShaderNames[i] = childShaderNameStrings[i].c_str();

    SkImageFilter* ptr = SkImageFilters::RuntimeShader(*runtimeShaderBuilder, childShaderNames.data(), inputChildren.data(), inputCount).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeTile
  (JNIEnv* env, jclass jclass, jfloat l0, jfloat t0, jfloat r0, jfloat b0, jfloat l1, jfloat t1, jfloat r1, jfloat b1, jlong inputPtr) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    SkImageFilter* ptr = SkImageFilters::Tile(SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, sk_ref_sp(input)).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeDilate
  (JNIEnv* env, jclass jclass, float rx, jfloat ry, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::Dilate(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeErode
  (JNIEnv* env, jclass jclass, float rx, jfloat ry, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::Erode(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeDistantLitDiffuse
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::DistantLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakePointLitDiffuse
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::PointLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeSpotLitDiffuse
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat z0, jfloat x1, jfloat y1, jfloat z1, jfloat falloffExponent, jfloat cutoffAngle, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::SpotLitDiffuse(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeDistantLitSpecular
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::DistantLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakePointLitSpecular
  (JNIEnv* env, jclass jclass, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::PointLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_ImageFilterKt__1nMakeSpotLitSpecular
  (JNIEnv* env, jclass jclass, jfloat x0, jfloat y0, jfloat z0, jfloat x1, jfloat y1, jfloat z1, jfloat falloffExponent, jfloat cutoffAngle, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jintArray cropInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropInts);
    SkImageFilter* ptr = SkImageFilters::SpotLitSpecular(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
