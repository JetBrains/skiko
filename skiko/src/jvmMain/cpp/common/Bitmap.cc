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

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapKt_Bitmap_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBitmap));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapKt__1nMake
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(new SkBitmap());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapKt__1nMakeClone
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(new SkBitmap(*instance));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nSwap
  (JNIEnv* env, jclass jclass, jlong ptr, jlong otherPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* other = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(otherPtr));
    instance->swap(*other);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nGetImageInfo
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray imageInfoResult, jlongArray colorSpaceResultPtr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkImageInfo imageInfo = instance->info();

    jint *result_int = env->GetIntArrayElements(imageInfoResult, NULL);
    result_int[0] = instance->width();
    result_int[1] = instance->height();
    result_int[2] = static_cast<int>(imageInfo.colorType());
    result_int[3] = static_cast<int>(imageInfo.alphaType());
    env->ReleaseIntArrayElements(imageInfoResult, result_int, 0);

    jlong *result_long = env->GetLongArrayElements(colorSpaceResultPtr, NULL);
    result_long[0] = reinterpret_cast<jlong>(imageInfo.refColorSpace().release());
    env->ReleaseLongArrayElements(colorSpaceResultPtr, result_long, 0);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapKt__1nGetRowBytesAsPixels
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->rowBytesAsPixels();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nIsNull
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->isNull();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapKt__1nGetRowBytes
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->rowBytes();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nSetAlphaType
  (JNIEnv* env, jclass jclass, jlong ptr, jint alphaType) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->setAlphaType(static_cast<SkAlphaType>(alphaType));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapKt__1nComputeByteSize
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->computeByteSize();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nIsImmutable
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->isImmutable();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nSetImmutable
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->setImmutable();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nReset
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->reset();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nComputeIsOpaque
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return SkBitmap::ComputeIsOpaque(*instance);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nSetImageInfo
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

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nAllocPixelsFlags
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

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nAllocPixelsRowBytes
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

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nInstallPixels
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jbyteArray pixelsArr, jint rowBytes) {
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

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nAllocPixels
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->tryAllocPixels();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapKt__1nGetPixelRef
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixelRef* pixelRef = instance->pixelRef();
    pixelRef->ref();
    return reinterpret_cast<jlong>(pixelRef);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapKt__1nGetPixelRefOrigin
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkIPoint origin = instance->pixelRefOrigin();
    return packIPoint(origin);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nSetPixelRef
  (JNIEnv* env, jclass jclass, jlong ptr, jlong pixelRefPtr, jint dx, jint dy) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixelRef* pixelRef = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(pixelRefPtr));
    instance->setPixelRef(sk_ref_sp<SkPixelRef>(pixelRef), dx, dy);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nIsReadyToDraw
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->readyToDraw();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapKt__1nGetGenerationId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nNotifyPixelsChanged
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->notifyPixelsChanged();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nEraseColor
  (JNIEnv* env, jclass jclass, jlong ptr, jint color) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->eraseColor(color);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BitmapKt__1nErase
  (JNIEnv* env, jclass jclass, jlong ptr, jint color, jint left, jint top, jint right, jint bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    instance->erase(color, {left, top, right, bottom});
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_BitmapKt_Bitmap_1nGetColor
  (JNIEnv* env, jclass jclass, jlong ptr, jint x, jint y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getColor(x, y);
}

extern "C" JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_BitmapKt__1nGetAlphaf
  (JNIEnv* env, jclass jclass, jlong ptr, jint x, jint y) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    return instance->getAlphaf(x, y);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_BitmapKt__1nExtractSubset
  (JNIEnv* env, jclass jclass, jlong ptr, jlong dstPtr, jint left, jint top, jint right, jint bottom) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(dstPtr));
    return instance->extractSubset(dst, {left, top, right, bottom});
}

// returns 1 if readBytes contain array contains successfully read bytes. returns 0 otherwise
extern "C" JNIEXPORT jbyte JNICALL Java_org_jetbrains_skia_BitmapKt__1nReadPixels
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr, jint rowBytes, jint srcX, jint srcY, jbyteArray readBytes) {
    jbyte *result_bytes = env->GetByteArrayElements(readBytes, NULL);

    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    if (instance->readPixels(imageInfo, result_bytes, rowBytes, srcX, srcY)) {
        env->ReleaseByteArrayElements(readBytes, result_bytes, 0);
        return 1;
    } else {
        return 0;
    }
}

extern "C" JNIEXPORT jbyte JNICALL Java_org_jetbrains_skia_BitmapKt__1nExtractAlpha
  (JNIEnv* env, jclass jclass, jlong ptr, jlong dstPtr, jlong paintPtr, jintArray resultPoint) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkBitmap* dst = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(dstPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    SkIPoint offset;

    jint *result_int = env->GetIntArrayElements(resultPoint, NULL);
    if (instance->extractAlpha(dst, paint, &offset)) {
        result_int[0] = offset.fX;
        result_int[1] = offset.fY;
        env->ReleaseIntArrayElements(resultPoint, result_int, 0);
        return 1;
    } else {
        return 0;
    }
}

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_BitmapKt__1nPeekPixels
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    SkPixmap pixmap;
    if (instance->peekPixels(&pixmap))
        return env->NewDirectByteBuffer(pixmap.writable_addr(), pixmap.rowBytes() * pixmap.height());
    else
        return nullptr;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BitmapKt__1nMakeShader
  (JNIEnv* env, jclass jclass, jlong ptr, jint tmx, jint tmy, jlong samplingMode, jfloatArray localMatrixArr) {
    SkBitmap* instance = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(ptr));
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    sk_sp<SkShader> shader = instance->makeShader(static_cast<SkTileMode>(tmx), static_cast<SkTileMode>(tmy), skija::SamplingMode::unpack(samplingMode), localMatrix.get());
    return reinterpret_cast<jlong>(shader.release());
}
