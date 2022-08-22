#include <iostream>
#include "SkColorFilter.h"
#include "SkImageFilter.h"
#include "SkImageFilters.h"
#include "SkPoint3.h"
#include "SkRect.h"
#include "common.h"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeAlphaThreshold
  (KNativePointer regionPtr, KFloat innerMin, KFloat outerMax, KNativePointer inputPtr, KInt* cropInts) {
    SkRegion* region = reinterpret_cast<SkRegion*>((regionPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropInts);
    SkImageFilter* ptr = SkImageFilters::AlphaThreshold(*region, innerMin, outerMax, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeArithmetic
  (KFloat k1, KFloat k2, KFloat k3, KFloat k4, KBoolean enforcePMColor, KNativePointer bgPtr, KNativePointer fgPtr, KInt* cropInts) {
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>((bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>((fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropInts);
    SkImageFilter* ptr = SkImageFilters::Arithmetic(k1, k2, k3, k4, enforcePMColor, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeBlend
  (KInt blendModeInt, KNativePointer bgPtr, KNativePointer fgPtr, KInt* cropInts) {
    SkBlendMode blendMode = static_cast<SkBlendMode>(blendModeInt);
    SkImageFilter* bg = reinterpret_cast<SkImageFilter*>((bgPtr));
    SkImageFilter* fg = reinterpret_cast<SkImageFilter*>((fgPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropInts);
    SkImageFilter* ptr = SkImageFilters::Blend(blendMode, sk_ref_sp(bg), sk_ref_sp(fg), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeBlur
  (KFloat sigmaX, KFloat sigmaY, KInt tileModeInt, KNativePointer inputPtr, KInt* cropRectInts) {
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::Blur(sigmaX, sigmaY, tileMode, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeColorFilter
  (KNativePointer colorFilterPtr, KNativePointer inputPtr, KInt* cropRectInts) {
    SkColorFilter* colorFilter = reinterpret_cast<SkColorFilter*>((colorFilterPtr));
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::ColorFilter(sk_ref_sp(colorFilter), sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeCompose
  (KNativePointer outerPtr, KNativePointer innerPtr) {
    SkImageFilter* outer = reinterpret_cast<SkImageFilter*>((outerPtr));
    SkImageFilter* inner = reinterpret_cast<SkImageFilter*>((innerPtr));
    SkImageFilter* ptr = SkImageFilters::Compose(sk_ref_sp(outer), sk_ref_sp(inner)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap
  (KInt xChanInt, KInt yChanInt, KFloat scale, KNativePointer displacementPtr, KNativePointer colorPtr, KInt* cropRectInts) {
    SkColorChannel xChan = static_cast<SkColorChannel>(xChanInt);
    SkColorChannel yChan = static_cast<SkColorChannel>(yChanInt);
    SkImageFilter* displacement = reinterpret_cast<SkImageFilter*>((displacementPtr));
    SkImageFilter* color = reinterpret_cast<SkImageFilter*>((colorPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::DisplacementMap(xChan, yChan, scale, sk_ref_sp(displacement), sk_ref_sp(color), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDropShadow
  (KFloat dx, KFloat dy, KFloat sigmaX, KFloat sigmaY, KInt color, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::DropShadow(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly
  (KFloat dx, KFloat dy, KFloat sigmaX, KFloat sigmaY, KInt color, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::DropShadowOnly(dx, dy, sigmaX, sigmaY, color, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeImage
  (KNativePointer imagePtr, KFloat l0, KFloat t0, KFloat r0, KFloat b0, KFloat l1, KFloat t1, KFloat r1, KFloat b1, KInt samplingModeVal1, KInt samplingModeVal2) {
    SkImage* image = reinterpret_cast<SkImage*>((imagePtr));
    SkImageFilter* ptr = SkImageFilters::Image(sk_ref_sp(image), SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, skija::SamplingMode::unpackFrom2Ints(samplingModeVal1, samplingModeVal2)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMagnifier
  (KFloat l, KFloat t, KFloat r, KFloat b, KFloat inset, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::Magnifier(SkRect{l, t, r, b}, inset, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution
  (KInt kernelW, KInt kernelH, KFloat* kernelArray, KFloat gain, KFloat bias, KInt offsetX, KInt offsetY, KInt tileModeInt, KBoolean convolveAlpha, KNativePointer inputPtr, KInt* cropRectInts) {
    SkTileMode tileMode = static_cast<SkTileMode>(tileModeInt);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::MatrixConvolution(SkISize{kernelW, kernelH}, kernelArray, gain, bias, SkIPoint{offsetX, offsetY}, tileMode, convolveAlpha, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform
  (KFloat* matrixArray, KInt samplingModeVal1, KInt samplingModeVal2, KNativePointer inputPtr) {
    std::unique_ptr<SkMatrix> matrix = skMatrix(matrixArray);
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    SkImageFilter* ptr = SkImageFilters::MatrixTransform(*matrix, skija::SamplingMode::unpackFrom2Ints(samplingModeVal1, samplingModeVal2), sk_ref_sp(input)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeMerge
  (KNativePointer* filtersArray, KInt filtersArraySize, KInt* cropRectInts) {
    std::vector<sk_sp<SkImageFilter>> filters(filtersArraySize);
    for (int i = 0; i < filtersArraySize; ++i) {
        SkImageFilter* fi = reinterpret_cast<SkImageFilter*>(filtersArray[i]);
        filters[i] = sk_ref_sp(fi);
    }
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::Merge(filters.data(), filtersArraySize, crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeOffset
  (KFloat dx, KFloat dy, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::Offset(dx, dy, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePaint
  (KNativePointer paintPtr, KInt* cropRectInts) {
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::Paint(*paint, crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePicture
  (KNativePointer picturePtr, KFloat l, KFloat t, KFloat r, KFloat b) {
    SkPicture* picture = reinterpret_cast<SkPicture*>((picturePtr));
    SkImageFilter* ptr = SkImageFilters::Picture(sk_ref_sp(picture), SkRect{l, t, r, b}).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeRuntimeShader
  (KNativePointer runtimeShaderBuilderPtr, KInteropPointer childShaderName, KNativePointer inputPtr) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(runtimeShaderBuilderPtr);
    sk_sp<SkImageFilter> input = sk_ref_sp<SkImageFilter>(reinterpret_cast<SkImageFilter*>(inputPtr));

    SkImageFilter* ptr = SkImageFilters::RuntimeShader(*runtimeShaderBuilder, reinterpret_cast<char *>(childShaderName), input).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeRuntimeShaderFromArray
  (KNativePointer runtimeShaderBuilderPtr, KInteropPointerArray childShaderNamesArr, KNativePointerArray inputPtrsArray, KInt inputCount) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(runtimeShaderBuilderPtr);

    KNativePointer* inputPtrs = reinterpret_cast<KNativePointer*>(inputPtrsArray);
    std::vector<sk_sp<SkImageFilter>> inputChildren(inputCount);
    for (size_t i = 0; i < inputCount; i++) {
        SkImageFilter* si = reinterpret_cast<SkImageFilter*>(inputPtrs[i]);
        inputChildren[i] = sk_ref_sp(si);
    }

    std::vector<SkString> childShaderNameStrings = skStringVector(childShaderNamesArr, inputCount);
    std::vector<std::string_view> childShaderNames(childShaderNameStrings.size());
    for (int i = 0; i < childShaderNames.size(); ++i)
        childShaderNames[i] = childShaderNameStrings[i].c_str();

    SkImageFilter* ptr = SkImageFilters::RuntimeShader(*runtimeShaderBuilder, childShaderNames.data(), inputChildren.data(), inputCount).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeTile
  (KFloat l0, KFloat t0, KFloat r0, KFloat b0, KFloat l1, KFloat t1, KFloat r1, KFloat b1, KNativePointer inputPtr) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    SkImageFilter* ptr = SkImageFilters::Tile(SkRect{l0, t0, r0, b0}, SkRect{l1, t1, r1, b1}, sk_ref_sp(input)).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDilate
  (float rx, KFloat ry, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::Dilate(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeErode
  (float rx, KFloat ry, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::Erode(rx, ry, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::DistantLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::PointLitDiffuse(SkPoint3{x, y, z}, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse
  (KFloat x0, KFloat y0, KFloat z0, KFloat x1, KFloat y1, KFloat z1, KFloat falloffExponent, KFloat cutoffAngle, KInt lightColor, KFloat surfaceScale, KFloat kd, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::SpotLitDiffuse(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, kd, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::DistantLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular
  (KFloat x, KFloat y, KFloat z, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::PointLitSpecular(SkPoint3{x, y, z}, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular
  (KFloat x0, KFloat y0, KFloat z0, KFloat x1, KFloat y1, KFloat z1, KFloat falloffExponent, KFloat cutoffAngle, KInt lightColor, KFloat surfaceScale, KFloat ks, KFloat shininess, KNativePointer inputPtr, KInt* cropRectInts) {
    SkImageFilter* input = reinterpret_cast<SkImageFilter*>((inputPtr));
    std::unique_ptr<SkIRect> crop = skija::IRect::toSkIRect(cropRectInts);
    SkImageFilter* ptr = SkImageFilters::SpotLitSpecular(SkPoint3{x0, y0, z0}, SkPoint3{x1, y1, z1}, falloffExponent, cutoffAngle, lightColor, surfaceScale, ks, shininess, sk_ref_sp(input), crop.get()).release();
    return reinterpret_cast<KNativePointer>(ptr);
}
