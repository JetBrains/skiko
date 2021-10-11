
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

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteBitmap));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMake
  () {
    return reinterpret_cast<KNativePointer>(new SkBitmap());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMakeClone
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return reinterpret_cast<KNativePointer>(new SkBitmap(*instance));
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nSwap
  (KNativePointer ptr, KNativePointer otherPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkBitmap* other = reinterpret_cast<SkBitmap*>((otherPtr));
    instance->swap(*other);
}


SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nGetImageInfo
  (KNativePointer ptr, KInt* imageInfoResult, KNativePointerArray colorSpacePtrs) {

  SkBitmap* instance = reinterpret_cast<SkBitmap*>(ptr);
  SkImageInfo imageInfo = instance->info();

  imageInfoResult[0] = instance->width();
  imageInfoResult[1] = instance->height();
  imageInfoResult[2] = static_cast<int>(imageInfo.colorType());
  imageInfoResult[3] = static_cast<int>(imageInfo.alphaType());

  KNativePointer* arr = static_cast<KNativePointer*>(colorSpacePtrs);
  arr[0] = reinterpret_cast<KNativePointer>(imageInfo.colorSpace());
}

#if 0
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nGetImageInfo
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return skija::ImageInfo::toJava(env, instance->info());
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nGetRowBytesAsPixels
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->rowBytesAsPixels();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nIsNull
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->isNull();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nGetRowBytes
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->rowBytes();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nSetAlphaType
  (KNativePointer ptr, KInt alphaType) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->setAlphaType(static_cast<SkAlphaType>(alphaType));
}

SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nComputeByteSize
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->computeByteSize();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nIsImmutable
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->isImmutable();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nSetImmutable
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->setImmutable();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nReset
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->reset();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nComputeIsOpaque
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return SkBitmap::ComputeIsOpaque(*instance);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nSetImageInfo
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KInt rowBytes) {
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
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KInt flags) {
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
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KInt rowBytes) {
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
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KByte* pixelsArr, KInt rowBytes) {
    TODO("implement org_jetbrains_skia_Bitmap__1nInstallPixels");
}

#if 0
SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nInstallPixels
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KByte* pixelsArr, KNativePointer rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));

    jsize len = env->GetArrayLength(pixelsArr);
    KByte* pixels = new KByte[len];
    env->GetByteArrayRegion(pixelsArr, 0, len, pixels);
    return instance->installPixels(imageInfo, pixels, rowBytes, deleteJBytes, nullptr);
}
#endif


SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nAllocPixels
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->tryAllocPixels();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nGetPixelRef
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkPixelRef* pixelRef = instance->pixelRef();
    pixelRef->ref();
    return reinterpret_cast<KNativePointer>(pixelRef);
}

SKIKO_EXPORT KLong org_jetbrains_skia_Bitmap__1nGetPixelRefOrigin
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkIPoint origin = instance->pixelRefOrigin();
    return packIPoint(origin);
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nSetPixelRef
  (KNativePointer ptr, KNativePointer pixelRefPtr, KInt dx, KInt dy) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkPixelRef* pixelRef = reinterpret_cast<SkPixelRef*>((pixelRefPtr));
    instance->setPixelRef(sk_ref_sp<SkPixelRef>(pixelRef), dx, dy);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nIsReadyToDraw
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->readyToDraw();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nGetGenerationId
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->getGenerationID();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nNotifyPixelsChanged
  (KNativePointer ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->notifyPixelsChanged();
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nEraseColor
  (KNativePointer ptr, KInt color) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->eraseColor(color);
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nErase
  (KNativePointer ptr, KInt color, KInt left, KInt top, KInt right, KInt bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    instance->erase(color, {left, top, right, bottom});
}

SKIKO_EXPORT KInt org_jetbrains_skia_Bitmap__1nGetColor
  (KNativePointer ptr, KInt x, KInt y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->getColor(x, y);
}

SKIKO_EXPORT KFloat org_jetbrains_skia_Bitmap__1nGetAlphaf
  (KNativePointer ptr, KInt x, KInt y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    return instance->getAlphaf(x, y);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Bitmap__1nExtractSubset
  (KNativePointer ptr, KNativePointer dstPtr, KInt left, KInt top, KInt right, KInt bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>((dstPtr));
    return instance->extractSubset(dst, {left, top, right, bottom});
}


// returns 1 if readBytes contain array contains successfully read bytes. returns 0 otherwise
SKIKO_EXPORT KByte org_jetbrains_skia_Bitmap__1nReadPixels
  (KNativePointer ptr, KInt width, KInt height, KInt colorType,
  KInt alphaType, KNativePointer colorSpacePtr, KInt rowBytes, KInt srcX, KInt srcY, KByte* resultBytes
  ) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(ptr);
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));

    if (instance->readPixels(imageInfo, resultBytes, rowBytes, srcX, srcY)) {
        return 1;
    } else {
        return 0;
    }
}

SKIKO_EXPORT void org_jetbrains_skia_Bitmap__1nExtractAlpha
  (KNativePointer ptr, KNativePointer dstPtr, KNativePointer paintPtr, KInt* result) {

  SkBitmap* instance = reinterpret_cast<SkBitmap*>(ptr);
  SkBitmap* dst = reinterpret_cast<SkBitmap*>(dstPtr);
  SkPaint* paint = reinterpret_cast<SkPaint*>(paintPtr);
  SkIPoint offset;

  if (instance->extractAlpha(dst, paint, &offset)) {
      result[0] = 1;
      result[1] = offset.fX;
      result[2] = offset.fY;
  } else {
     result[0] = 0;
  }
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nPeekPixels
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nPeekPixels");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMakeShader
  (KNativePointer ptr, KInt tmx, KInt tmy, KLong samplingMode, KFloat* localMatrixArr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(ptr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(
        static_cast<SkTileMode>(tmx),
        static_cast<SkTileMode>(tmy),
        skija::SamplingMode::unpack(samplingMode),
        localMatrix.get()
    );
    return reinterpret_cast<KNativePointer>(shader.release());
}

