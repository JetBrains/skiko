
// This file has been auto generated.

#include "SkPixmap.h"
#include "common.h"

static void deletePixmap(SkPixmap *pixmap) {
    delete pixmap;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return ptrToKNativePointer(&deletePixmap);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nMakeNull
  (KInteropPointer __Kinstance) {
    return ptrToKNativePointer(new SkPixmap());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nMake
  (KInteropPointer __Kinstance,
  KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes) {
    SkColorSpace* colorSpace = KNativePointerToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return ptrToKNativePointer(new SkPixmap(
        imageInfo, KNativePointerToPtr<void*>(pixelsPtr), rowBytes));
}

SKIKO_EXPORT void org_jetbrains_skia_Pixmap__1nReset
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    pixmap->reset();
}

SKIKO_EXPORT void org_jetbrains_skia_Pixmap__1nResetWithInfo
  (KInteropPointer __Kinstance, KNativePointer ptr,
  KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = KNativePointerToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    pixmap->reset(imageInfo, KNativePointerToPtr<void*>(pixelsPtr), rowBytes);
}

SKIKO_EXPORT void org_jetbrains_skia_Pixmap__1nSetColorSpace
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer colorSpacePtr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = KNativePointerToPtr<SkColorSpace*>(colorSpacePtr);
    pixmap->setColorSpace(sk_ref_sp<SkColorSpace>(colorSpace));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nExtractSubset
  (KInteropPointer __Kinstance, KNativePointer ptr,
  KNativePointer subsetPtr, KInt l, KInt t, KInt w, KInt h) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkPixmap* dst = KNativePointerToPtr<SkPixmap*>(subsetPtr);
    return pixmap->extractSubset(dst, { l, t, w, h });
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Pixmap__1nGetInfo
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Pixmap__1nGetInfo");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Pixmap__1nGetInfo
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    const SkImageInfo& imageInfo = pixmap->info();
    return skija::ImageInfo::toJava(env, imageInfo);
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nGetRowBytes
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->rowBytes());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nGetAddr
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return ptrToKNativePointer(pixmap->addr());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nGetRowBytesAsPixels
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->rowBytesAsPixels());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nComputeByteSize
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->computeByteSize());
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nComputeIsOpaque
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KBoolean>(pixmap->computeIsOpaque());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Pixmap__1nGetColor
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt x, KInt y) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KInt>(pixmap->getColor(x, y));
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Pixmap__1nGetAlphaF
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt x, KInt y) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KFloat>(pixmap->getAlphaf(x, y));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Pixmap__1nGetAddrAt
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt x, KInt y) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return reinterpret_cast<KNativePointer>(pixmap->addr(x, y));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixels
  (KInteropPointer __Kinstance, KNativePointer ptr,
  KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = KNativePointerToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return static_cast<KBoolean>(pixmap->readPixels(imageInfo, KNativePointerToPtr<void*>(pixelsPtr), rowBytes));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixelsFromPoint
  (KInteropPointer __Kinstance, KNativePointer ptr,
  KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer pixelsPtr, KInt rowBytes,
  KInt srcX, KInt srcY) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = KNativePointerToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return static_cast<KBoolean>(pixmap->readPixels(imageInfo, KNativePointerToPtr<void*>(pixelsPtr), rowBytes, srcX, srcY));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixelsToPixmap
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dstPixmapPtr) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = KNativePointerToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<KBoolean>(pixmap->readPixels(*dstPixmap));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nReadPixelsToPixmapFromPoint
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dstPixmapPtr, KInt srcX, KInt srcY) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = KNativePointerToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<KBoolean>(pixmap->readPixels(*dstPixmap, srcX, srcY));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nScalePixels
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dstPixmapPtr, KNativePointer samplingOptions) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = KNativePointerToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<KBoolean>(pixmap->scalePixels(*dstPixmap, skija::SamplingMode::unpack(samplingOptions)));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nErase
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt color) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KBoolean>(pixmap->erase(color));
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Pixmap__1nEraseSubset
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt color, KInt l, KInt t, KInt w, KInt h) {
    SkPixmap* pixmap = KNativePointerToPtr<SkPixmap*>(ptr);
    return static_cast<KBoolean>(pixmap->erase(color, { l, t, w, h }));
}
