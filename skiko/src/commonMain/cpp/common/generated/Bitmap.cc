
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nGetFinalizer(KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteBitmap));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMake
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>(new SkBitmap());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMakeClone
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return reinterpret_cast<KNativePointer>(new SkBitmap(*instance));
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nSwap
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer otherPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkBitmap* other = reinterpret_cast<SkBitmap*>((otherPtr));
    instance->swap(*other);
}


SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nGetImageInfo
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nGetImageInfo");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nGetImageInfo
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return skija::ImageInfo::toJava(env, instance->info());
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nGetRowBytesAsPixels
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->rowBytesAsPixels();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nIsNull
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->isNull();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nGetRowBytes
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->rowBytes();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nSetAlphaType
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt alphaType) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->setAlphaType(static_cast<SkAlphaType>(alphaType));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nComputeByteSize
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->computeByteSize();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nIsImmutable
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->isImmutable();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nSetImmutable
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->setImmutable();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nReset
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->reset();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nComputeIsOpaque
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return SkBitmap::ComputeIsOpaque(*instance);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nSetImageInfo
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->setInfo(imageInfo, rowBytes);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nAllocPixelsFlags
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KInt flags) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->tryAllocPixelsFlags(imageInfo, flags);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nAllocPixelsRowBytes
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->tryAllocPixels(imageInfo, rowBytes);
}


SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nInstallPixels
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, jbyteArray pixelsArr, KNativePointer rowBytes) {
    TODO("implement org_jetbrains_skia_Bitmap__1nInstallPixels");
}
     
#if 0 
SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nInstallPixels
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, jbyteArray pixelsArr, KNativePointer rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
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


SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nAllocPixels
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->tryAllocPixels();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nGetPixelRef
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkPixelRef* pixelRef = instance->pixelRef();
    pixelRef->ref();
    return reinterpret_cast<KNativePointer>(pixelRef);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nGetPixelRefOrigin
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkIPoint origin = instance->pixelRefOrigin();
    return packIPoint(origin);
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nSetPixelRef
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer pixelRefPtr, KInt dx, KInt dy) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkPixelRef* pixelRef = reinterpret_cast<SkPixelRef*>((pixelRefPtr));
    instance->setPixelRef(sk_ref_sp<SkPixelRef>(pixelRef), dx, dy);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nIsReadyToDraw
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->readyToDraw();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nGetGenerationId
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->getGenerationID();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nNotifyPixelsChanged
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->notifyPixelsChanged();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nEraseColor
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt color) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->eraseColor(color);
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nErase
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt color, KInt left, KInt top, KInt right, KInt bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->erase(color, {left, top, right, bottom});
}

SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nGetColor
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt x, KInt y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->getColor(x, y);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Bitmap__1nGetAlphaf
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt x, KInt y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->getAlphaf(x, y);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nExtractSubset
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dstPtr, KInt left, KInt top, KInt right, KInt bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>((dstPtr));
    return instance->extractSubset(dst, {left, top, right, bottom});
}


SKIKO_EXPORT jbyteArray org_jetbrains_skia_Bitmap__1nReadPixels
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer rowBytes, KInt srcX, KInt srcY) {
    TODO("implement org_jetbrains_skia_Bitmap__1nReadPixels");
}
     
#if 0 
SKIKO_EXPORT jbyteArray org_jetbrains_skia_Bitmap__1nReadPixels
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer rowBytes, KInt srcX, KInt srcY) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
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



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nExtractAlpha
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dstPtr, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nExtractAlpha");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nExtractAlpha
  (KInteropPointer __Kinstance, KNativePointer ptr, KNativePointer dstPtr, KNativePointer paintPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>((dstPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    SkIPoint offset;
    if (instance->extractAlpha(dst, paint, &offset))
        return skija::IPoint::fromSkIPoint(env, offset);
    else
        return nullptr;
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nPeekPixels
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nPeekPixels");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nPeekPixels
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkPixmap pixmap;
    if (instance->peekPixels(&pixmap))
        return env->NewDirectByteBuffer(pixmap.writable_addr(), pixmap.rowBytes() * pixmap.height());
    else
        return nullptr;
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMakeShader
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt tmx, KInt tmy, KNativePointer samplingMode, KFloat* localMatrixArr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nMakeShader");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMakeShader
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt tmx, KInt tmy, KNativePointer samplingMode, KFloat* localMatrixArr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(static_cast<SkTileMode>(tmx), static_cast<SkTileMode>(tmy), skija::SamplingMode::unpack(samplingMode), localMatrix.get());
    return reinterpret_cast<KNativePointer>(shader.release());
}
#endif

