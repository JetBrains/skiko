
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

  int width = instance->width();
  int height = instance->height();
  int colorType = static_cast<int>(imageInfo.colorType());
  int alphaType = static_cast<int>(imageInfo.alphaType());

  imageInfoResult[0] = instance->width();
  imageInfoResult[1] = instance->height();
  imageInfoResult[2] = static_cast<int>(imageInfo.colorType());
  imageInfoResult[3] = static_cast<int>(imageInfo.alphaType());

  // Для того, чтобы на стороне котлина создать ImageInfo нам нужен NativePointer на ColorSpace
  // и возник вопрос как вернуть KNativePointer отсюда в котлин?

  // вариант 1) return писать не хочется, так как функция называется GetImageInfo
  // и возвращать из неё KNativePointer на ColorSpace будет плохо

  // вариант 2) использовать NativePointerArray размером = 1.
  // тогда необходимо закастить его к актуальному типу int* для wasm и long* для native?
  // сделал это с использованием #ifdef SKIKO_WASM ниже. Норм ли так?

  // какие есть альтерантивы? Можно разбить этот метод на два:
  // метод 1 будет заполнять IntArray imageInfoResult
  // метод 2 будет возвращать KNativePointer на ColorSpace
  // кмк такой вариант тоже так себе, так как придётся делать два вызова вместо одного везде
  // где нужен GetImageInfo (правда с точки зрения Bitmap.kt такое место всего одно)

  pointerTypeAlias* arr = static_cast<pointerTypeAlias*>(colorSpacePtrs);
  arr[0] = reinterpret_cast<pointerTypeAlias>(imageInfo.colorSpace());

//  #ifdef SKIKO_WASM
//    int* arr = static_cast<int*>(colorSpacePtrs);
//    arr[0] = reinterpret_cast<int>(imageInfo.colorSpace());
//  #else
//    long* arr = static_cast<long*>(colorSpacePtrs);
//    arr[0] = reinterpret_cast<long>(imageInfo.colorSpace());
//  #endif
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


SKIKO_EXPORT KByte* org_jetbrains_skia_Bitmap__1nReadPixels
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KInt rowBytes, KInt srcX, KInt srcY) {
    TODO("implement org_jetbrains_skia_Bitmap__1nReadPixels");
}

#if 0
SKIKO_EXPORT KByte* org_jetbrains_skia_Bitmap__1nReadPixels
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr, KNativePointer rowBytes, KInt srcX, KInt srcY) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::vector<KByte> pixels(std::min(height, instance->height() - srcY) * rowBytes);
    if (instance->readPixels(imageInfo, pixels.data(), rowBytes, srcX, srcY))
        return javaByteArray(env, pixels);
    else
        return nullptr;
}
#endif

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nExtractAlpha
  (KNativePointer ptr, KNativePointer dstPtr, KNativePointer paintPtr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nExtractAlpha");
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Bitmap__1nPeekPixels
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nPeekPixels");
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Bitmap__1nMakeShader
  (KNativePointer ptr, KInt tmx, KInt tmy, KNativePointer samplingMode, KFloat* localMatrixArr) {
    TODO("implement org_jetbrains_skia_Bitmap__1nMakeShader");
}

