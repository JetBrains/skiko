
// This file has been auto generated.

#include "SkPixmap.h"
#include "common.h"

static void deletePixmap(SkPixmap *pixmap) {
    delete pixmap;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nGetFinalizer
  () {
    return reinterpret_cast<KNativePointer>(&deletePixmap);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nMakeNull
  () {
    return ptrToInterop(new SkPixmap());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nMake
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes) {
    SkColorSpace* colorSpace = interopToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return ptrToInterop(new SkPixmap(
        imageInfo, interopToPtr<void*>(pixelsPtr), rowBytes));
}

SKIKO_EXPORT void org_jetbrains_skia_Pixmap__1nReset
  (KNativePointer ptr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    pixmap->reset();
}

SKIKO_EXPORT void org_jetbrains_skia_Pixmap__1nResetWithInfo
  (KNativePointer ptr,
  KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = interopToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    pixmap->reset(imageInfo, interopToPtr<void*>(pixelsPtr), rowBytes);
}

SKIKO_EXPORT void org_jetbrains_skia_Pixmap__1nSetColorSpace
  (KNativePointer ptr, KNativePointer colorSpacePtr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = interopToPtr<SkColorSpace*>(colorSpacePtr);
    pixmap->setColorSpace(sk_ref_sp<SkColorSpace>(colorSpace));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nExtractSubset
  (KNativePointer ptr,
  KNativePointer subsetPtr, KInt l, KInt t, KInt w, KInt h) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkPixmap* dst = interopToPtr<SkPixmap*>(subsetPtr);
    return pixmap->extractSubset(dst, { l, t, w, h });
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Pixmap__1nGetInfo
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Pixmap__1nGetInfo");
}

SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nGetRowBytes
  (KNativePointer ptr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->rowBytes());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nGetAddr
  (KNativePointer ptr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return const_cast<void*>(pixmap->addr());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nGetRowBytesAsPixels
  (KNativePointer ptr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->rowBytesAsPixels());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nComputeByteSize
  (KNativePointer ptr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->computeByteSize());
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nComputeIsOpaque
  (KNativePointer ptr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KBoolean>(pixmap->computeIsOpaque());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nGetColor
  (KNativePointer ptr, KInt x, KInt y) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->getColor(x, y));
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Pixmap__1nGetAlphaF
  (KNativePointer ptr, KInt x, KInt y) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KFloat>(pixmap->getAlphaf(x, y));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nGetAddrAt
  (KNativePointer ptr, KInt x, KInt y) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return const_cast<KNativePointer>(pixmap->addr(x, y));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixels
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = interopToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return static_cast<KBoolean>(pixmap->readPixels(imageInfo, interopToPtr<void*>(pixelsPtr), rowBytes));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixelsFromPoint
  (KNativePointer ptr,
  KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes,
  KInt srcX, KInt srcY) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = interopToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return static_cast<KBoolean>(pixmap->readPixels(imageInfo, interopToPtr<void*>(pixelsPtr), rowBytes, srcX, srcY));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixelsToPixmap
  (KNativePointer ptr, KNativePointer dstPixmapPtr) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = interopToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<KBoolean>(pixmap->readPixels(*dstPixmap));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixelsToPixmapFromPoint
  (KNativePointer ptr, KNativePointer dstPixmapPtr, KInt srcX, KInt srcY) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = interopToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<KBoolean>(pixmap->readPixels(*dstPixmap, srcX, srcY));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nScalePixels
  (KNativePointer ptr, KNativePointer dstPixmapPtr, KInt samplingOptionsVal1, KInt samplingOptionsVal2) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = interopToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<KBoolean>(pixmap->scalePixels(*dstPixmap, skija::SamplingMode::unpackFrom2Ints(samplingOptionsVal1, samplingOptionsVal2)));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nErase
  (KNativePointer ptr, KInt color) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KBoolean>(pixmap->erase(color));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nEraseSubset
  (KNativePointer ptr, KInt color, KInt l, KInt t, KInt w, KInt h) {
    SkPixmap* pixmap = interopToPtr<SkPixmap*>(ptr);
    return static_cast<KBoolean>(pixmap->erase(color, { l, t, w, h }));
}
