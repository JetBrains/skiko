
// This file has been auto generated.

#include "SkBitmap.h"
#include "SkPixelRef.h"
#include "SkSamplingOptions.h"
#include "SkShader.h"
#include "common.h"

static void deleteBitmap(SkBitmap* instance) {
    // std::cout << "Deleting [SkBitmap " << instance << "]" << std::endl;
    delete instance;
}

extern "C" jlong org_jetbrains_skia_Bitmap__1nGetFinalizer(kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBitmap));
}

extern "C" jlong org_jetbrains_skia_Bitmap__1nMake
  (kref __Kinstance) {
    return reinterpret_cast<jlong>(new SkBitmap());
}

extern "C" jlong org_jetbrains_skia_Bitmap__1nMakeClone
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(new SkBitmap(*instance));
}

extern "C" void org_jetbrains_skia_Bitmap__1nSwap
  (kref __Kinstance, jlong ptr, jlong otherPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* other = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(otherPtr));
    instance->swap(*other);
}


extern "C" jobject org_jetbrains_skia_Bitmap__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nGetImageInfo");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Bitmap__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return skija::ImageInfo::toJava(env, instance->info());
}
#endif


extern "C" jint org_jetbrains_skia_Bitmap__1nGetRowBytesAsPixels
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->rowBytesAsPixels();
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nIsNull
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->isNull();
}

extern "C" jlong org_jetbrains_skia_Bitmap__1nGetRowBytes
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->rowBytes();
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nSetAlphaType
  (kref __Kinstance, jlong ptr, jint alphaType) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->setAlphaType(static_cast<SkAlphaType>(alphaType));
}

extern "C" jlong org_jetbrains_skia_Bitmap__1nComputeByteSize
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->computeByteSize();
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nIsImmutable
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->isImmutable();
}

extern "C" void org_jetbrains_skia_Bitmap__1nSetImmutable
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->setImmutable();
}

extern "C" void org_jetbrains_skia_Bitmap__1nReset
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->reset();
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nComputeIsOpaque
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return SkBitmap::ComputeIsOpaque(*instance);
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nSetImageInfo
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->setInfo(imageInfo, rowBytes);
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nAllocPixelsFlags
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jint flags) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->tryAllocPixelsFlags(imageInfo, flags);
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nAllocPixelsRowBytes
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->tryAllocPixels(imageInfo, rowBytes);
}


extern "C" jboolean org_jetbrains_skia_Bitmap__1nInstallPixels
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jbyteArray pixelsArr, jlong rowBytes) {
    TODO("implement org_jetbrains_skia_Bitmap__1nInstallPixels");
}
     
#if 0 
extern "C" jboolean org_jetbrains_skia_Bitmap__1nInstallPixels
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jbyteArray pixelsArr, jlong rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));

    jsize len = env->GetArrayLength(pixelsArr);
    jbyte* pixels = new jbyte[len];
    env->GetByteArrayRegion(pixelsArr, 0, len, pixels);
    return instance->installPixels(imageInfo, pixels, rowBytes, deleteJBytes, nullptr);
}
#endif


extern "C" jboolean org_jetbrains_skia_Bitmap__1nAllocPixels
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->tryAllocPixels();
}

extern "C" jlong org_jetbrains_skia_Bitmap__1nGetPixelRef
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixelRef* pixelRef = instance->pixelRef();
    pixelRef->ref();
    return reinterpret_cast<jlong>(pixelRef);
}

extern "C" jlong org_jetbrains_skia_Bitmap__1nGetPixelRefOrigin
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkIPoint origin = instance->pixelRefOrigin();
    return packIPoint(origin);
}

extern "C" void org_jetbrains_skia_Bitmap__1nSetPixelRef
  (kref __Kinstance, jlong ptr, jlong pixelRefPtr, jint dx, jint dy) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixelRef* pixelRef = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(pixelRefPtr));
    instance->setPixelRef(sk_ref_sp<SkPixelRef>(pixelRef), dx, dy);
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nIsReadyToDraw
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->readyToDraw();
}

extern "C" jint org_jetbrains_skia_Bitmap__1nGetGenerationId
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" void org_jetbrains_skia_Bitmap__1nNotifyPixelsChanged
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->notifyPixelsChanged();
}

extern "C" void org_jetbrains_skia_Bitmap__1nEraseColor
  (kref __Kinstance, jlong ptr, jint color) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->eraseColor(color);
}

extern "C" void org_jetbrains_skia_Bitmap__1nErase
  (kref __Kinstance, jlong ptr, jint color, jint left, jint top, jint right, jint bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->erase(color, {left, top, right, bottom});
}

extern "C" jint org_jetbrains_skia_Bitmap__1nGetColor
  (kref __Kinstance, jlong ptr, jint x, jint y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getColor(x, y);
}

extern "C" jfloat org_jetbrains_skia_Bitmap__1nGetAlphaf
  (kref __Kinstance, jlong ptr, jint x, jint y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getAlphaf(x, y);
}

extern "C" jboolean org_jetbrains_skia_Bitmap__1nExtractSubset
  (kref __Kinstance, jlong ptr, jlong dstPtr, jint left, jint top, jint right, jint bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(dstPtr));
    return instance->extractSubset(dst, {left, top, right, bottom});
}


extern "C" jbyteArray org_jetbrains_skia_Bitmap__1nReadPixels
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong rowBytes, jint srcX, jint srcY) {
    TODO("implement org_jetbrains_skia_Bitmap__1nReadPixels");
}
     
#if 0 
extern "C" jbyteArray org_jetbrains_skia_Bitmap__1nReadPixels
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jlong rowBytes, jint srcX, jint srcY) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::vector<jbyte> pixels(std::min(height, instance->height() - srcY) * rowBytes);
    if (instance->readPixels(imageInfo, pixels.data(), rowBytes, srcX, srcY))
        return javaByteArray(env, pixels);
    else
        return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_Bitmap__1nExtractAlpha
  (kref __Kinstance, jlong ptr, jlong dstPtr, jlong paintPtr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nExtractAlpha");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Bitmap__1nExtractAlpha
  (kref __Kinstance, jlong ptr, jlong dstPtr, jlong paintPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(dstPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkIPoint offset;
    if (instance->extractAlpha(dst, paint, &offset))
        return skija::IPoint::fromSkIPoint(env, offset);
    else
        return nullptr;
}
#endif



extern "C" jobject org_jetbrains_skia_Bitmap__1nPeekPixels
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nPeekPixels");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Bitmap__1nPeekPixels
  (kref __Kinstance, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixmap pixmap;
    if (instance->peekPixels(&pixmap))
        return env->NewDirectByteBuffer(pixmap.writable_addr(), pixmap.rowBytes() * pixmap.height());
    else
        return nullptr;
}
#endif



extern "C" jlong org_jetbrains_skia_Bitmap__1nMakeShader
  (kref __Kinstance, jlong ptr, jint tmx, jint tmy, jlong samplingMode, jfloatArray localMatrixArr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nMakeShader");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Bitmap__1nMakeShader
  (kref __Kinstance, jlong ptr, jint tmx, jint tmy, jlong samplingMode, jfloatArray localMatrixArr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(static_cast<SkTileMode>(tmx), static_cast<SkTileMode>(tmy), skija::SamplingMode::unpack(samplingMode), localMatrix.get());
    return reinterpret_cast<jlong>(shader.release());
}
#endif

