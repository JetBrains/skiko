#include <jni.h>
#include "SkBitmap.h"
#include "SkPixelRef.h"
#include "SkSamplingOptions.h"
#include "SkShader.h"
#include "interop.hh"

static void deleteBitmap(SkBitmap* instance) {
    // std::cout << "Deleting [SkBitmap " << instance << "]" << std::endl;
    delete instance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBitmap));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nMake
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(new SkBitmap());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nMakeClone
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(new SkBitmap(*instance));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nSwap
  (JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* other = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(otherPtr));
    instance->swap(*other);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetImageInfo
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray imageInfoResult, jlongArray colorSpaceResultPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkImageInfo imageInfo = instance->info();

    skija::ImageInfo::writeImageInfoForInterop(
        env, imageInfo, imageInfoResult, colorSpaceResultPtr
    );
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetRowBytesAsPixels
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->rowBytesAsPixels();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nIsNull
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->isNull();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetRowBytes
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->rowBytes();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nSetAlphaType
  (JNIEnv* env, jclass jclass, jlong ptr, jint alphaType) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->setAlphaType(static_cast<SkAlphaType>(alphaType));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nComputeByteSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->computeByteSize();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nIsImmutable
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->isImmutable();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nSetImmutable
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->setImmutable();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nReset
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->reset();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nComputeIsOpaque
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return SkBitmap::ComputeIsOpaque(*instance);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nSetImageInfo
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jint rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->setInfo(imageInfo, rowBytes);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nAllocPixelsFlags
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jint flags) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->tryAllocPixelsFlags(imageInfo, flags);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nAllocPixelsRowBytes
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jint rowBytes) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    return instance->tryAllocPixels(imageInfo, rowBytes);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nInstallPixels
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jbyteArray pixelsArr, jint rowBytes, jint pixelsLen) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));

    jbyte* pixels = new jbyte[pixelsLen];
    env->GetByteArrayRegion(pixelsArr, 0, pixelsLen, pixels);
    return instance->installPixels(imageInfo, pixels, rowBytes, deleteJBytes, nullptr);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nAllocPixels
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->tryAllocPixels();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetPixelRef
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixelRef* pixelRef = instance->pixelRef();
    pixelRef->ref();
    return reinterpret_cast<jlong>(pixelRef);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetPixelRefOriginX
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkIPoint origin = instance->pixelRefOrigin();
    return origin.x();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetPixelRefOriginY
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkIPoint origin = instance->pixelRefOrigin();
    return origin.y();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nSetPixelRef
  (JNIEnv* env, jclass jclass, jlong ptr, jlong pixelRefPtr, jint dx, jint dy) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixelRef* pixelRef = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(pixelRefPtr));
    instance->setPixelRef(sk_ref_sp<SkPixelRef>(pixelRef), dx, dy);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nIsReadyToDraw
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->readyToDraw();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetGenerationId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nNotifyPixelsChanged
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->notifyPixelsChanged();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nEraseColor
  (JNIEnv* env, jclass jclass, jlong ptr, jint color) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->eraseColor(color);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nErase
  (JNIEnv* env, jclass jclass, jlong ptr, jint color, jint left, jint top, jint right, jint bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->erase(color, {left, top, right, bottom});
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetColor
  (JNIEnv* env, jclass jclass, jlong ptr, jint x, jint y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getColor(x, y);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nGetAlphaf
  (JNIEnv* env, jclass jclass, jlong ptr, jint x, jint y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getAlphaf(x, y);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nExtractSubset
  (JNIEnv* env, jclass jclass, jlong ptr, jlong dstPtr, jint left, jint top, jint right, jint bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(dstPtr));
    return instance->extractSubset(dst, {left, top, right, bottom});
}

// returns true if readBytes array contains successfully read bytes. returns false otherwise
extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nReadPixels
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jint rowBytes, jint srcX, jint srcY, jbyteArray readBytes) {
    jbyte *result_bytes = env->GetByteArrayElements(readBytes, NULL);

    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    jboolean result = instance->readPixels(imageInfo, result_bytes, rowBytes, srcX, srcY);
    env->ReleaseByteArrayElements(readBytes, result_bytes, 0);
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nExtractAlpha
  (JNIEnv* env, jclass jclass, jlong ptr, jlong dstPtr, jlong paintPtr, jintArray resultPoint) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(dstPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkIPoint offset;

    jint *result_int = env->GetIntArrayElements(resultPoint, NULL);
    jboolean result = instance->extractAlpha(dst, paint, &offset);

    if (result) {
        result_int[0] = offset.fX;
        result_int[1] = offset.fY;
    }
    env->ReleaseIntArrayElements(resultPoint, result_int, 0);
    return result;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nPeekPixels
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = new SkPixmap();
    if (instance->peekPixels(pixmap))
        return ptrToJlong(pixmap);
    else {
        delete pixmap;
        return 0;
    }
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapExternalKt_Bitmap_1nMakeShader
  (JNIEnv* env, jclass jclass, jlong ptr, jint tmx, jint tmy, jint samplingModeVal1, jint samplingModeVal2, jfloatArray localMatrixArr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(static_cast<SkTileMode>(tmx), static_cast<SkTileMode>(tmy), skija::SamplingMode::unpackFrom2Ints(env, samplingModeVal1, samplingModeVal2), localMatrix.get());
    return reinterpret_cast<jlong>(shader.release());
}
