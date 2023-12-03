#include <iostream>
#include "SkData.h"
#include "SkImage.h"
#include "SkBitmap.h"
#include "SkShader.h"
#include "SkEncodedImageFormat.h"
#include "encode/SkPngEncoder.h"
#include "encode/SkJpegEncoder.h"
#include "encode/SkWebpEncoder.h"
#include "common.h"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeRaster
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KByte* bytesArr, KInt rowBytes) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    sk_sp<SkImage> image = SkImages::RasterFromPixmapCopy(SkPixmap(imageInfo, bytesArr, rowBytes));
    return reinterpret_cast<KNativePointer>(image.release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeRasterData
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer dataPtr, KInt rowBytes) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    sk_sp<SkImage> image = SkImages::RasterFromData(imageInfo, sk_ref_sp(data), rowBytes);
    return reinterpret_cast<KNativePointer>(image.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeFromBitmap
  (KNativePointer bitmapPtr) {
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    sk_sp<SkImage> image = SkImages::RasterFromBitmap(*bitmap);
    return reinterpret_cast<KNativePointer>(image.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeFromPixmap
  (KNativePointer pixmapPtr) {
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    sk_sp<SkImage> image = SkImages::RasterFromPixmap(*pixmap, nullptr, nullptr);
    return reinterpret_cast<KNativePointer>(image.release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeFromEncoded
  (KByte* encodedArray, KInt encodedLen) {
    sk_sp<SkData> encodedData = SkData::MakeWithCopy(encodedArray, encodedLen);
    sk_sp<SkImage> image = SkImages::DeferredFromEncodedData(encodedData);
    return reinterpret_cast<KNativePointer>(image.release());
}

SKIKO_EXPORT void org_jetbrains_skia_Image__1nGetImageInfo
  (KNativePointer ptr, KInt* imageInfoResult, KNativePointer* colorSpacePtrsArray) {
  SkImage* instance = reinterpret_cast<SkImage*>((ptr));
  SkImageInfo imageInfo = instance->imageInfo();
  skija::ImageInfo::writeImageInfoForInterop(imageInfo, imageInfoResult, colorSpacePtrsArray);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nEncodeToData
  (KNativePointer ptr, KInt format, KInt quality) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    SkEncodedImageFormat skFormat = static_cast<SkEncodedImageFormat>(format);
    if (!instance->isTextureBacked()) {
       switch (skFormat) {
         case SkEncodedImageFormat::kPNG: {
           SkPngEncoder::Options options = SkPngEncoder::Options();
           options.fZLibLevel = std::max(0, std::min(quality / 10, 9));
           SkData* data = SkPngEncoder::Encode(nullptr, instance, options).release();
           return reinterpret_cast<KNativePointer>(data);
         }
         case SkEncodedImageFormat::kJPEG: {
           SkJpegEncoder::Options options = SkJpegEncoder::Options();
           options.fQuality = quality;
           SkData* data = SkJpegEncoder::Encode(nullptr, instance, options).release();
           return reinterpret_cast<KNativePointer>(data);
         }
         case SkEncodedImageFormat::kWEBP: {
           SkWebpEncoder::Options options = SkWebpEncoder::Options();
           options.fQuality = quality;
           SkData* data = SkWebpEncoder::Encode(nullptr, instance, options).release();
           return reinterpret_cast<KNativePointer>(data);
         }
       default:
         break;
       }
    }
    return reinterpret_cast<KNativePointer>(0);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Image__1nMakeShader
  (KNativePointer ptr, KInt tmx, KInt tmy, KInt samplingModeVal1, KInt samplingModeVal2, KFloat* localMatrixArr) {
    SkImage* instance = reinterpret_cast<SkImage*>(ptr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(
        static_cast<SkTileMode>(tmx),
        static_cast<SkTileMode>(tmy),
        skija::SamplingMode::unpackFrom2Ints(samplingModeVal1, samplingModeVal2),
        localMatrix.get()
    );
    return reinterpret_cast<KNativePointer>(shader.release());
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Image__1nPeekPixels
  (KNativePointer ptr) {
  SkImage* instance = reinterpret_cast<SkImage*>(ptr);
  SkPixmap* pixmap = new SkPixmap();
  if (instance->peekPixels(pixmap)) {
      return reinterpret_cast<KNativePointer>(pixmap);
  } else {
      delete pixmap;
      return nullptr;
  }
}

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
  (KNativePointer ptr, KNativePointer pixmapPtr, KInt samplingOptionsVal1, KInt samplingOptionsVal2, KBoolean cache) {
    SkImage* instance = reinterpret_cast<SkImage*>((ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return instance->scalePixels(*pixmap, skija::SamplingMode::unpackFrom2Ints(samplingOptionsVal1, samplingOptionsVal2), cachingHint);
}
