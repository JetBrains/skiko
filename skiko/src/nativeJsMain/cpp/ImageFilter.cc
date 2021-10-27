
// This file has been auto generated.

#include <iostream>
#include "SkColorFilter.h"
#include "SkImageFilter.h"
#include "SkImageFilters.h"
#include "SkPoint3.h"
#include "SkRect.h"
#include "common.h"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeAlphaThreshold
  (KNativePointer regionPtr, KFloat innerMin, KFloat outerMax, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeAlphaThreshold");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeAlphaThreshold
  (KNativePointer regionPtr, KFloat innerMin, KFloat outerMax, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::AlphaThreshold(*region, innerMin, outerMax, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeArithmetic
  (KFloat k1, KFloat k2, KFloat k3, KFloat k4, KBoolean enforcePMColor, KNativePointer bgPtr, KNativePointer fgPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeArithmetic");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeArithmetic
  (KFloat k1, KFloat k2, KFloat k3, KFloat k4, KBoolean enforcePMColor, KNativePointer bgPtr, KNativePointer fgPtr, KInteropPointer cropObj) {
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>((bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>((fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Arithmetic(k1, k2, k3, k4, enforcePMColor, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeBlend
  (KInt blendModeInt, KNativePointer bgPtr, KNativePointer fgPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeBlend");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeBlend
  (KInt blendModeInt, KNativePointer bgPtr, KNativePointer fgPtr, KInteropPointer cropObj) {
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>((bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>((fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Blend(blendMode, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeBlur
  (KFloat sigmaX, KFloat sigmaY, KInt tileModeInt, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeBlur");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeBlur
  (KFloat sigmaX, KFloat sigmaY, KInt tileModeInt, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Blur(sigmaX, sigmaY, tileMode, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeColorFilter
  (KNativePointer colorFilterPtr, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeColorFilter");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeColorFilter
  (KNativePointer colorFilterPtr, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>((colorFilterPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::ColorFilter(sk_ref_sp(colorFilter), sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeCompose
  (KNativePointer outerPtr, KNativePointer innerPtr) {
    SkImageFilter* outer = reinterpret_cast<SkImageFilter*>((outerPtr));
    SkImageFilter* inner = reinterpret_cast<SkImageFilter*>((innerPtr));
    SkImageFilter* ptr = SkImageFilters::Compose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap
  (KInt xChanInt, KInt yChanInt, KFloat scale, KNativePointer displacementPtr, KNativePointer colorPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap
  (KInt xChanInt, KInt yChanInt, KFloat scale, KNativePointer displacementPtr, KNativePointer colorPtr, KInteropPointer cropObj) {
    SkColorChannel xChan = static_cast<SkColorChannel>(xChanInt);
    SkColorChannel yChan = static_cast<SkColorChannel>(yChanInt);
    SkImageFilter* displacement = reinterpret_cast<SkImageFilter*>((displacementPtr));
    SkImageFilter* color = reinterpret_cast<SkImageFilter*>((colorPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DisplacementMap(xChan, yChan, scale, sk_ref_sp(displacement), sk_ref_sp(color), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDropShadow
  (KFloat dx, KFloat dy, KFloat sigmaX, KFloat sigmaY, KInt color, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDropShadow");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDropShadow
  (KFloat dx, KFloat dy, KFloat sigmaX, KFloat sigmaY, KInt color, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DropShadow(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly
  (KFloat dx, KFloat dy, KFloat sigmaX, KFloat sigmaY, KInt color, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly
  (KFloat dx, KFloat dy, KFloat sigmaX, KFloat sigmaY, KInt color, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DropShadowOnly(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeImage
  (KNativePointer imagePtr, KFloat l0, KFloat t0, KFloat r0, KFloat b0, KFloat l1, KFloat t1, KFloat r1, KFloat b1, KInt* samplingMode) {
    SkImage* image = reinterpret_cast<SkImage*>((imagePtr));
    SkImageFilter* ptr = SkImageFilters::Image(sk_ref_sp(image), SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, skija::SamplingMode::unpackFrom2Ints(samplingMode)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMagnifier
  (KFloat l, KFloat t, KFloat r, KFloat b, KFloat inset, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMagnifier");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMagnifier
  (KFloat l, KFloat t, KFloat r, KFloat b, KFloat inset, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Magnifier(SkRect{l, t, r, b}, inset, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution
  (KInt kernelW, KInt kernelH, KFloat* kernelArray, KFloat gain, KFloat bias, KInt offsetX, KInt offsetY, KInt tileModeInt, KBoolean convolveAlpha, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution
  (KInt kernelW, KInt kernelH, KFloat* kernelArray, KFloat gain, KFloat bias, KInt offsetX, KInt offsetY, KInt tileModeInt, KBoolean convolveAlpha, KNativePointer inputPtr, KInteropPointer cropObj) {
    KFloat* kernel = env->GetFloatArrayElements(kernelArray, 0);
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::MatrixConvolution(SkISize{kernelW, kernelH}, kernel, gain, bias, SkIPoint{offsetX, offsetY}, tileMode, convolveAlpha, sk_ref_sp(input), crop.get()).release();
    env->ReleaseFloatArrayElements(kernelArray, kernel, 0);
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform
  (KFloat* matrixArray, KNativePointer samplingMode, KNativePointer inputPtr) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform
  (KFloat* matrixArray, KNativePointer samplingMode, KNativePointer inputPtr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(env, matrixArray);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    SkImageFilter* ptr = SkImageFilters::MatrixTransform(*matrix, skija::SamplingMode::unpack(samplingMode), sk_ref_sp(input)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMerge
  (KNativePointerArray filtersArray, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeMerge");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMerge
  (KNativePointerArray filtersArray, KInteropPointer cropObj) {
    KNativePointer* f = env->GetLongArrayElements(filtersArray, 0);
    jsize len = env->GetArrayLength(filtersArray);
    std::vector<sk_sp<SkImageFilter>> filters(len);
    for (int i = 0; i < len; ++i) {
        SkImageFilter* fi = reinterpret_cast<SkImageFilter*>((f[i]));
        filters[i] = sk_ref_sp(fi);
    }
    env->ReleaseLongArrayElements(filtersArray, f, 0);
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Merge(filters.data(), len, crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeOffset
  (KFloat dx, KFloat dy, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeOffset");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeOffset
  (KFloat dx, KFloat dy, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Offset(dx, dy, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePaint
  (KNativePointer paintPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakePaint");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePaint
  (KNativePointer paintPtr, KInteropPointer cropObj) {
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Paint(*paint, crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePicture
  (KNativePointer picturePtr, KFloat l, KFloat t, KFloat r, KFloat b) {
    SkPicture* picture = reinterpret_cast<SkPicture*>((picturePtr));
    SkImageFilter* ptr = SkImageFilters::Picture(sk_ref_sp(picture), SkRect{l, t, r, b}).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeTile
  (KFloat l0, KFloat t0, KFloat r0, KFloat b0, KFloat l1, KFloat t1, KFloat r1, KFloat b1, KNativePointer inputPtr) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    SkImageFilter* ptr = SkImageFilters::Tile(SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, sk_ref_sp(input)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDilate
  (float rx, KFloat ry, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDilate");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDilate
  (float rx, KFloat ry, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Dilate(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeErode
  (float rx, KFloat ry, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeErode");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeErode
  (float rx, KFloat ry, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::Erode(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DistantLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::PointLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse
  (KFloat x0, KFloat y0, KFloat z0, KFloat x1, KFloat y1, KFloat z1, KFloat falloffExponent, KFloat cutoffAngle, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse
  (KFloat x0, KFloat y0, KFloat z0, KFloat x1, KFloat y1, KFloat z1, KFloat falloffExponent, KFloat cutoffAngle, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::SpotLitDiffuse(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::DistantLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::PointLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular
  (KFloat x0, KFloat y0, KFloat z0, KFloat x1, KFloat y1, KFloat z1, KFloat falloffExponent, KFloat cutoffAngle, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInteropPointer cropObj) {
    TODO("implement org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular
  (KFloat x0, KFloat y0, KFloat z0, KFloat x1, KFloat y1, KFloat z1, KFloat falloffExponent, KFloat cutoffAngle, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInteropPointer cropObj) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(env, cropObj);
    SkImageFilter* ptr = SkImageFilters::SpotLitSpecular(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
#endif

