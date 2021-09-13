
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkImageFilter.h"
#include "SkImageFilters.h"
#include "SkPoint3.h"
#include "SkRect.h"
#include "common.h"


extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeAlphaThreshold
  (kref __Kinstance, jlong regionPtr, jfloat innerMin, jfloat outerMax, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeAlphaThreshold");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeAlphaThreshold
  (kref __Kinstance, jlong regionPtr, jfloat innerMin, jfloat outerMax, jlong inputPtr, jobject cropObj) {
    SkRegion* region = reinterpret_cast<SkRegion*>(static_cast<uintptr_t>(regionPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::AlphaThreshold(*region, innerMin, outerMax, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeArithmetic
  (kref __Kinstance, jfloat k1, jfloat k2, jfloat k3, jfloat k4, jboolean enforcePMColor, jlong bgPtr, jlong fgPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeArithmetic");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeArithmetic
  (kref __Kinstance, jfloat k1, jfloat k2, jfloat k3, jfloat k4, jboolean enforcePMColor, jlong bgPtr, jlong fgPtr, jobject cropObj) {
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Arithmetic(k1, k2, k3, k4, enforcePMColor, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeBlend
  (kref __Kinstance, jint blendModeInt, jlong bgPtr, jlong fgPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeBlend");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeBlend
  (kref __Kinstance, jint blendModeInt, jlong bgPtr, jlong fgPtr, jobject cropObj) {
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Blend(blendMode, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeBlur
  (kref __Kinstance, jfloat sigmaX, jfloat sigmaY, jint tileModeInt, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeBlur");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeBlur
  (kref __Kinstance, jfloat sigmaX, jfloat sigmaY, jint tileModeInt, jlong inputPtr, jobject cropObj) {
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Blur(sigmaX, sigmaY, tileMode, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeColorFilter
  (kref __Kinstance, jlong colorFilterPtr, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeColorFilter");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeColorFilter
  (kref __Kinstance, jlong colorFilterPtr, jlong inputPtr, jobject cropObj) {
    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(colorFilterPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::ColorFilter(sk_ref_sp(colorFilter), sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeCompose
  (kref __Kinstance, jlong outerPtr, jlong innerPtr) {
    SkImageFilter* outer = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(outerPtr));
    SkImageFilter* inner = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(innerPtr));
    SkImageFilter* ptr = SkImageFilters::Compose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap
  (kref __Kinstance, jint xChanInt, jint yChanInt, jfloat scale, jlong displacementPtr, jlong colorPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap
  (kref __Kinstance, jint xChanInt, jint yChanInt, jfloat scale, jlong displacementPtr, jlong colorPtr, jobject cropObj) {
    SkColorChannel xChan = static_cast<SkColorChannel>(xChanInt);
    SkColorChannel yChan = static_cast<SkColorChannel>(yChanInt);
    SkImageFilter* displacement = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(displacementPtr));
    SkImageFilter* color = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(colorPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DisplacementMap(xChan, yChan, scale, sk_ref_sp(displacement), sk_ref_sp(color), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDropShadow
  (kref __Kinstance, jfloat dx, jfloat dy, jfloat sigmaX, jfloat sigmaY, jint color, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDropShadow");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDropShadow
  (kref __Kinstance, jfloat dx, jfloat dy, jfloat sigmaX, jfloat sigmaY, jint color, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DropShadow(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly
  (kref __Kinstance, jfloat dx, jfloat dy, jfloat sigmaX, jfloat sigmaY, jint color, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly
  (kref __Kinstance, jfloat dx, jfloat dy, jfloat sigmaX, jfloat sigmaY, jint color, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DropShadowOnly(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeImage
  (kref __Kinstance, jlong imagePtr, jfloat l0, jfloat t0, jfloat r0, jfloat b0, jfloat l1, jfloat t1, jfloat r1, jfloat b1, jlong samplingMode) {
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(imagePtr));
    SkImageFilter* ptr = SkImageFilters::Image(sk_ref_sp(image), SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, skija::SamplingMode::unpack(samplingMode)).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMagnifier
  (kref __Kinstance, jfloat l, jfloat t, jfloat r, jfloat b, jfloat inset, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMagnifier");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMagnifier
  (kref __Kinstance, jfloat l, jfloat t, jfloat r, jfloat b, jfloat inset, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Magnifier(SkRect{l, t, r, b}, inset, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution
  (kref __Kinstance, jint kernelW, jint kernelH, jfloatArray kernelArray, jfloat gain, jfloat bias, jint offsetX, jint offsetY, jint tileModeInt, jboolean convolveAlpha, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution
  (kref __Kinstance, jint kernelW, jint kernelH, jfloatArray kernelArray, jfloat gain, jfloat bias, jint offsetX, jint offsetY, jint tileModeInt, jboolean convolveAlpha, jlong inputPtr, jobject cropObj) {
    jfloat* kernel = env->GetFloatArrayElements(kernelArray, 0);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::MatrixConvolution(SkISize{kernelW, kernelH}, kernel, gain, bias, SkIPoint{offsetX, offsetY}, tileMode, convolveAlpha, sk_ref_sp(input), crop.get()).release();
    env->ReleaseFloatArrayElements(kernelArray, kernel, 0);
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform
  (kref __Kinstance, jfloatArray matrixArray, jlong samplingMode, jlong inputPtr) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform
  (kref __Kinstance, jfloatArray matrixArray, jlong samplingMode, jlong inputPtr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArray);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    SkImageFilter* ptr = SkImageFilters::MatrixTransform(*matrix, skija::SamplingMode::unpack(samplingMode), sk_ref_sp(input)).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMerge
  (kref __Kinstance, jlongArray filtersArray, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMerge");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeMerge
  (kref __Kinstance, jlongArray filtersArray, jobject cropObj) {
    jlong* f = env->GetLongArrayElements(filtersArray, 0);
    jsize len = env->GetArrayLength(filtersArray);
    std::vector<sk_sp<SkImageFilter>> filters(len);
    for (int i = 0; i < len; ++i) {
        SkImageFilter* fi = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(f[i]));
        filters[i] = sk_ref_sp(fi);
    }
    env->ReleaseLongArrayElements(filtersArray, f, 0);
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Merge(filters.data(), len, crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeOffset
  (kref __Kinstance, jfloat dx, jfloat dy, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeOffset");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeOffset
  (kref __Kinstance, jfloat dx, jfloat dy, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Offset(dx, dy, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakePaint
  (kref __Kinstance, jlong paintPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakePaint");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakePaint
  (kref __Kinstance, jlong paintPtr, jobject cropObj) {
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Paint(*paint, crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif


extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakePicture
  (kref __Kinstance, jlong picturePtr, jfloat l, jfloat t, jfloat r, jfloat b) {
    SkPicture* picture = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(picturePtr));
    SkImageFilter* ptr = SkImageFilters::Picture(sk_ref_sp(picture), SkRect{l, t, r, b}).release();
    return reinterpret_cast<jlong>(ptr);
}

extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeTile
  (kref __Kinstance, jfloat l0, jfloat t0, jfloat r0, jfloat b0, jfloat l1, jfloat t1, jfloat r1, jfloat b1, jlong inputPtr) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    SkImageFilter* ptr = SkImageFilters::Tile(SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, sk_ref_sp(input)).release();
    return reinterpret_cast<jlong>(ptr);
}


extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDilate
  (kref __Kinstance, float rx, jfloat ry, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDilate");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDilate
  (kref __Kinstance, float rx, jfloat ry, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Dilate(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeErode
  (kref __Kinstance, float rx, jfloat ry, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeErode");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeErode
  (kref __Kinstance, float rx, jfloat ry, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Erode(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DistantLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::PointLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat z0, jfloat x1, jfloat y1, jfloat z1, jfloat falloffExponent, jfloat cutoffAngle, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat z0, jfloat x1, jfloat y1, jfloat z1, jfloat falloffExponent, jfloat cutoffAngle, jint lightColor, jfloat surfaceScale, jfloat kd, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::SpotLitDiffuse(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DistantLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular
  (kref __Kinstance, jfloat x, jfloat y, jfloat z, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::PointLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif



extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat z0, jfloat x1, jfloat y1, jfloat z1, jfloat falloffExponent, jfloat cutoffAngle, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jobject cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular
  (kref __Kinstance, jfloat x0, jfloat y0, jfloat z0, jfloat x1, jfloat y1, jfloat z1, jfloat falloffExponent, jfloat cutoffAngle, jint lightColor, jfloat surfaceScale, jfloat ks, jfloat shininess, jlong inputPtr, jobject cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>(static_cast<uintptr_t>(inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::SpotLitSpecular(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<jlong>(ptr);
}
#endif

