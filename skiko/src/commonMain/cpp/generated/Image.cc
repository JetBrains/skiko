
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkImage.h"
#include "common.h"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeRaster
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KByte* bytesArr, KNativePointer rowBytes) {
    TODO("implement org_jetbrains_skia_Image__1nMakeRaster");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeRaster
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KByte* bytesArr, KNativePointer rowBytes) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    void* bytes = env->GetPrimitiveArrayCritical(bytesArr, 0);
    sk_sp<SkImage> image = SkImage::MakeRasterCopy(SkPixmap(imageInfo, bytes, rowBytes));
    env->ReleasePrimitiveArrayCritical(bytesArr, bytes, 0);
    return reinterpret_cast<KNativePointer>(image.release());
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeRasterData
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer dataPtr, KInt rowBytes) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    sk_sp<SkImage> image = SkImage::MakeRasterData(imageInfo, sk_ref_sp(data), rowBytes);
    return reinterpret_cast<KNativePointer>(image.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeFromBitmap
  (KNativePointer bitmapPtr) {
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    sk_sp<SkImage> image = SkImage::MakeFromBitmap(*bitmap);
    return reinterpret_cast<KNativePointer>(image.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeFromPixmap
  (KNativePointer pixmapPtr) {
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    sk_sp<SkImage> image = SkImage::MakeFromRaster(*pixmap, nullptr, nullptr);
    return reinterpret_cast<KNativePointer>(image.release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeFromEncoded
  (KByte* encodedArray) {
    TODO("implement org_jetbrains_skia_Image__1nMakeFromEncoded");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeFromEncoded
  (KByte* encodedArray) {
    jsize encodedLen = env->GetArrayLength(encodedArray);
    KByte* encoded = env->GetByteArrayElements(encodedArray, 0);
    sk_sp<SkData> encodedData = SkData::MakeWithCopy(encoded, encodedLen);
    env->ReleaseByteArrayElements(encodedArray, encoded, 0);

    sk_sp<SkImage> image = SkImage::MakeFromEncoded(encodedData);

    return reinterpret_cast<KNativePointer>(image.release());
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Image__1nGetImageInfo
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Image__1nGetImageInfo");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Image__1nGetImageInfo
  (KNativePointer ptr) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    return skija::ImageInfo::toJava(env, instance->imageInfo());
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nEncodeToData
  (KNativePointer ptr, KInt format, KInt quality) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    SkData* data = instance->encodeToData(static_cast<SkEncodedImageFormat>(format), quality).release();
    return reinterpret_cast<KNativePointer>(data);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeShader
  (KNativePointer ptr, KInt tmx, KInt tmy, KNativePointer sampling, KFloat* localMatrixArr) {
    TODO("implement org_jetbrains_skia_Image__1nMakeShader");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeShader
  (KNativePointer ptr, KInt tmx, KInt tmy, KNativePointer sampling, KFloat* localMatrixArr) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(static_cast<SkTileMode>(tmx), static_cast<SkTileMode>(tmy), skija::SamplingMode::unpack(sampling), localMatrix.get());
    return reinterpret_cast<KNativePointer>(shader.release());
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Image__1nPeekPixels
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Image__1nPeekPixels");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Image__1nPeekPixels
  (KNativePointer ptr) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    SkPixmap pixmap;
    if (instance->peekPixels(&pixmap))
        return env->NewDirectByteBuffer(pixmap.writable_addr(), pixmap.rowBytes() * pixmap.height());
    else
        return nullptr;
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_Image__1nPeekPixelsToPixmap
  (KNativePointer ptr, KNativePointer pixmapPtr) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    return instance->peekPixels(pixmap);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Image__1nReadPixelsBitmap
  (KNativePointer ptr, KNativePointer contextPtr, KNativePointer bitmapPtr, KInt srcX, KInt srcY, KBoolean cache) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((contextPtr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return instance->readPixels(context, bitmap->info(), bitmap->getPixels(), bitmap->pixmap().rowBytes(), srcX, srcY, cachingHint);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Image__1nReadPixelsPixmap
  (KNativePointer ptr, KNativePointer pixmapPtr, KInt srcX, KInt srcY, KBoolean cache) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return instance->readPixels(*pixmap, srcX, srcY, cachingHint);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Image__1nScalePixels
  (KNativePointer ptr, KNativePointer pixmapPtr, KLong samplingOptions, KBoolean cache) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return instance->scalePixels(*pixmap, skija::SamplingMode::unpack(samplingOptions), cachingHint);
}
