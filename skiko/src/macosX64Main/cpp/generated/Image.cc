
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkImage.h"
#include "common.h"


extern "C" jlong org_jetbrains_skia_Image__1nMakeRaster
  (kref __Kinstance, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jbyteArray bytesArr, jlong rowBytes) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Image__1nMakeRaster");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Image__1nMakeRaster
  (kref __Kinstance, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jbyteArray bytesArr, jlong rowBytes) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    void* bytes = env->GetPrimitiveArrayCritical(bytesArr, 0);
    sk_sp<SkImage> image = SkImage::MakeRasterCopy(SkPixmap(imageInfo, bytes, rowBytes));
    env->ReleasePrimitiveArrayCritical(bytesArr, bytes, 0);
    return reinterpret_cast<jlong>(image.release());
}
#endif


extern "C" jlong org_jetbrains_skia_Image__1nMakeRasterData
  (kref __Kinstance, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong dataPtr, jlong rowBytes) {
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    sk_sp<SkImage> image = SkImage::MakeRasterData(imageInfo, sk_ref_sp(data), rowBytes);
    return reinterpret_cast<jlong>(image.release());
}

extern "C" jlong org_jetbrains_skia_Image__1nMakeFromBitmap
  (kref __Kinstance, jlong bitmapPtr) {
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    sk_sp<SkImage> image = SkImage::MakeFromBitmap(*bitmap);
    return reinterpret_cast<jlong>(image.release());
}

extern "C" jlong org_jetbrains_skia_Image__1nMakeFromPixmap
  (kref __Kinstance, jlong pixmapPtr) {
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    sk_sp<SkImage> image = SkImage::MakeFromRaster(*pixmap, nullptr, nullptr);
    return reinterpret_cast<jlong>(image.release());
}


extern "C" jlong org_jetbrains_skia_Image__1nMakeFromEncoded
  (kref __Kinstance, jbyteArray encodedArray) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Image__1nMakeFromEncoded");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Image__1nMakeFromEncoded
  (kref __Kinstance, jbyteArray encodedArray) {
    jsize encodedLen = env->GetArrayLength(encodedArray);
    jbyte* encoded = env->GetByteArrayElements(encodedArray, 0);
    sk_sp<SkData> encodedData = SkData::MakeWithCopy(encoded, encodedLen);
    env->ReleaseByteArrayElements(encodedArray, encoded, 0);

    sk_sp<SkImage> image = SkImage::MakeFromEncoded(encodedData);

    return reinterpret_cast<jlong>(image.release());
}
#endif



extern "C" jobject org_jetbrains_skia_Image__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Image__1nGetImageInfo");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Image__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    return skija::ImageInfo::toJava(env, instance->imageInfo());
}
#endif


extern "C" jlong org_jetbrains_skia_Image__1nEncodeToData
  (kref __Kinstance, jlong ptr, jint format, jint quality) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    SkData* data = instance->encodeToData(static_cast<SkEncodedImageFormat>(format), quality).release();
    return reinterpret_cast<jlong>(data);
}


extern "C" jlong org_jetbrains_skia_Image__1nMakeShader
  (kref __Kinstance, jlong ptr, jint tmx, jint tmy, jlong sampling, jfloatArray localMatrixArr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Image__1nMakeShader");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Image__1nMakeShader
  (kref __Kinstance, jlong ptr, jint tmx, jint tmy, jlong sampling, jfloatArray localMatrixArr) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(static_cast<SkTileMode>(tmx), static_cast<SkTileMode>(tmy), skija::SamplingMode::unpack(sampling), localMatrix.get());
    return reinterpret_cast<jlong>(shader.release());
}
#endif



extern "C" jobject org_jetbrains_skia_Image__1nPeekPixels
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Image__1nPeekPixels");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Image__1nPeekPixels
  (kref __Kinstance, jlong ptr) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    SkPixmap pixmap;
    if (instance->peekPixels(&pixmap))
        return env->NewDirectByteBuffer(pixmap.writable_addr(), pixmap.rowBytes() * pixmap.height());
    else
        return nullptr;
}
#endif


extern "C" jboolean org_jetbrains_skia_Image__1nPeekPixelsToPixmap
  (kref __Kinstance, jlong ptr, jlong pixmapPtr) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    return instance->peekPixels(pixmap);
}

extern "C" jboolean org_jetbrains_skia_Image__1nReadPixelsBitmap
  (kref __Kinstance, jlong ptr, jlong contextPtr, jlong bitmapPtr, jint srcX, jint srcY, jboolean cache) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return instance->readPixels(context, bitmap->info(), bitmap->getPixels(), bitmap->pixmap().rowBytes(), srcX, srcY, cachingHint);
}

extern "C" jboolean org_jetbrains_skia_Image__1nReadPixelsPixmap
  (kref __Kinstance, jlong ptr, jlong pixmapPtr, jint srcX, jint srcY, jboolean cache) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return instance->readPixels(*pixmap, srcX, srcY, cachingHint);
}

extern "C" jboolean org_jetbrains_skia_Image__1nScalePixels
  (kref __Kinstance, jlong ptr, jlong pixmapPtr, jlong samplingOptions, jboolean cache) {
    SkImage* instance = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return instance->scalePixels(*pixmap, skija::SamplingMode::unpack(samplingOptions), cachingHint);
}
