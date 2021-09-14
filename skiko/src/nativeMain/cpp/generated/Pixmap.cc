
// This file has been auto generated.

#include "SkPixmap.h"
#include "common.h"

static void deletePixmap(SkPixmap *pixmap) {
    delete pixmap;
}

extern "C" jlong org_jetbrains_skia_Pixmap__1nGetFinalizer
  () {
    return ptrToJlong(&deletePixmap);
}

extern "C" jlong org_jetbrains_skia_Pixmap__1nMakeNull
  () {
    return ptrToJlong(new SkPixmap());
}

extern "C" jlong org_jetbrains_skia_Pixmap__1nMake
  (
  jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong pixelsPtr, jint rowBytes) {
    SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return ptrToJlong(new SkPixmap(
        imageInfo, jlongToPtr<void*>(pixelsPtr), rowBytes));
}

extern "C" void org_jetbrains_skia_Pixmap__1nReset
  (jlong ptr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    pixmap->reset();
}

extern "C" void org_jetbrains_skia_Pixmap__1nResetWithInfo
  (jlong ptr,
  jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong pixelsPtr, jint rowBytes) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    pixmap->reset(imageInfo, jlongToPtr<void*>(pixelsPtr), rowBytes);
}

extern "C" void org_jetbrains_skia_Pixmap__1nSetColorSpace
  (jlong ptr, jlong colorSpacePtr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
    pixmap->setColorSpace(sk_ref_sp<SkColorSpace>(colorSpace));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nExtractSubset
  (jlong ptr,
  jlong subsetPtr, jint l, jint t, jint w, jint h) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkPixmap* dst = jlongToPtr<SkPixmap*>(subsetPtr);
    return pixmap->extractSubset(dst, { l, t, w, h });
}


extern "C" jobject org_jetbrains_skia_Pixmap__1nGetInfo
  (jlong ptr) {
    TODO("implement org_jetbrains_skia_Pixmap__1nGetInfo");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Pixmap__1nGetInfo
  (jlong ptr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    const SkImageInfo& imageInfo = pixmap->info();
    return skija::ImageInfo::toJava(env, imageInfo);
}
#endif


extern "C" jint org_jetbrains_skia_Pixmap__1nGetRowBytes
  (jlong ptr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jint>(pixmap->rowBytes());
}

extern "C" jlong org_jetbrains_skia_Pixmap__1nGetAddr
  (jlong ptr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return ptrToJlong(pixmap->addr());
}

extern "C" jint org_jetbrains_skia_Pixmap__1nGetRowBytesAsPixels
  (jlong ptr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jint>(pixmap->rowBytesAsPixels());
}

extern "C" jint org_jetbrains_skia_Pixmap__1nComputeByteSize
  (jlong ptr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jint>(pixmap->computeByteSize());
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nComputeIsOpaque
  (jlong ptr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jboolean>(pixmap->computeIsOpaque());
}

extern "C" jint org_jetbrains_skia_Pixmap__1nGetColor
  (jlong ptr, jint x, jint y) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jint>(pixmap->getColor(x, y));
}

extern "C" jfloat org_jetbrains_skia_Pixmap__1nGetAlphaF
  (jlong ptr, jint x, jint y) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jfloat>(pixmap->getAlphaf(x, y));
}

extern "C" jlong org_jetbrains_skia_Pixmap__1nGetAddrAt
  (jlong ptr, jint x, jint y) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return reinterpret_cast<jlong>(pixmap->addr(x, y));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nReadPixels
  (jlong ptr,
  jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong pixelsPtr, jint rowBytes) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return static_cast<jboolean>(pixmap->readPixels(imageInfo, jlongToPtr<void*>(pixelsPtr), rowBytes));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nReadPixelsFromPoint
  (jlong ptr,
  jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong pixelsPtr, jint rowBytes,
  jint srcX, jint srcY) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                          height,
                                          static_cast<SkColorType>(colorType),
                                          static_cast<SkAlphaType>(alphaType),
                                          sk_ref_sp<SkColorSpace>(colorSpace));
    return static_cast<jboolean>(pixmap->readPixels(imageInfo, jlongToPtr<void*>(pixelsPtr), rowBytes, srcX, srcY));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nReadPixelsToPixmap
  (jlong ptr, jlong dstPixmapPtr) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = jlongToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<jboolean>(pixmap->readPixels(*dstPixmap));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nReadPixelsToPixmapFromPoint
  (jlong ptr, jlong dstPixmapPtr, jint srcX, jint srcY) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = jlongToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<jboolean>(pixmap->readPixels(*dstPixmap, srcX, srcY));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nScalePixels
  (jlong ptr, jlong dstPixmapPtr, jlong samplingOptions) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    SkPixmap* dstPixmap = jlongToPtr<SkPixmap*>(dstPixmapPtr);
    return static_cast<jboolean>(pixmap->scalePixels(*dstPixmap, skija::SamplingMode::unpack(samplingOptions)));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nErase
  (jlong ptr, jint color) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jboolean>(pixmap->erase(color));
}

extern "C" jboolean org_jetbrains_skia_Pixmap__1nEraseSubset
  (jlong ptr, jint color, jint l, jint t, jint w, jint h) {
    SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
    return static_cast<jboolean>(pixmap->erase(color, { l, t, w, h }));
}
