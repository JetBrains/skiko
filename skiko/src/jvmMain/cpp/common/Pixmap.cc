#include <jni.h>
#include "interop.hh"
#include "SkPixmap.h"

static void deletePixmap(SkPixmap *pixmap) {
    delete pixmap;
}

extern "C" {
    JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nGetFinalizer
      (JNIEnv *env, jclass klass) {
        return ptrToJlong(&deletePixmap);
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PixmapKt__1nMakeNull
      (JNIEnv *env, jclass klass) {
        return ptrToJlong(new SkPixmap());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nMake
      (JNIEnv *env, jclass klass,
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

    JNIEXPORT void JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nReset
      (JNIEnv *env, jclass klass, jlong ptr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        pixmap->reset();
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skia_PixmapKt__1nResetWithInfo
      (JNIEnv *env, jclass klass, jlong ptr,
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

    JNIEXPORT void JNICALL Java_org_jetbrains_skia_PixmapKt__1nSetColorSpace
      (JNIEnv *env, jclass klass, jlong ptr, jlong colorSpacePtr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        SkColorSpace* colorSpace = jlongToPtr<SkColorSpace*>(colorSpacePtr);
        pixmap->setColorSpace(sk_ref_sp<SkColorSpace>(colorSpace));
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nExtractSubset
      (JNIEnv *env, jclass klass, jlong ptr,
      jlong subsetPtr, jint l, jint t, jint w, jint h) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        SkPixmap* dst = jlongToPtr<SkPixmap*>(subsetPtr);
        return pixmap->extractSubset(dst, { l, t, w, h });
    }

    JNIEXPORT void JNICALL Java_org_jetbrains_skia_PixmapKt__1nGetInfo
      (JNIEnv* env, jclass jclass, jlong ptr, jintArray imageInfoResult, jlongArray colorSpaceResultPtr) {
        SkPixmap* instance = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(ptr));
        SkImageInfo imageInfo = instance->info();

        skija::ImageInfo::writeImageInfoForInterop(
                env, imageInfo, imageInfoResult, colorSpaceResultPtr
        );
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nGetRowBytes
      (JNIEnv *env, jclass klass, jlong ptr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jint>(pixmap->rowBytes());
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PixmapKt__1nGetAddr
      (JNIEnv *env, jclass klass, jlong ptr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return ptrToJlong(pixmap->addr());
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nGetRowBytesAsPixels
      (JNIEnv *env, jclass klass, jlong ptr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jint>(pixmap->rowBytesAsPixels());
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nComputeByteSize
      (JNIEnv *env, jclass klass, jlong ptr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jint>(pixmap->computeByteSize());
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nComputeIsOpaque
      (JNIEnv *env, jclass klass, jlong ptr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jboolean>(pixmap->computeIsOpaque());
    }

    JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PixmapKt_Pixmap_1nGetColor
      (JNIEnv *env, jclass klass, jlong ptr, jint x, jint y) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jint>(pixmap->getColor(x, y));
    }

    JNIEXPORT jfloat JNICALL Java_org_jetbrains_skia_PixmapKt__1nGetAlphaF
      (JNIEnv *env, jclass klass, jlong ptr, jint x, jint y) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jfloat>(pixmap->getAlphaf(x, y));
    }

    JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PixmapKt__1nGetAddrAt
      (JNIEnv *env, jclass klass, jlong ptr, jint x, jint y) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return reinterpret_cast<jlong>(pixmap->addr(x, y));
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt__1nReadPixels
      (JNIEnv *env, jclass klass, jlong ptr,
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

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt__1nReadPixelsFromPoint
      (JNIEnv *env, jclass klass, jlong ptr,
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

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt__1nReadPixelsToPixmap
      (JNIEnv *env, jclass klass, jlong ptr, jlong dstPixmapPtr) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        SkPixmap* dstPixmap = jlongToPtr<SkPixmap*>(dstPixmapPtr);
        return static_cast<jboolean>(pixmap->readPixels(*dstPixmap));
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt__1nReadPixelsToPixmapFromPoint
      (JNIEnv *env, jclass klass, jlong ptr, jlong dstPixmapPtr, jint srcX, jint srcY) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        SkPixmap* dstPixmap = jlongToPtr<SkPixmap*>(dstPixmapPtr);
        return static_cast<jboolean>(pixmap->readPixels(*dstPixmap, srcX, srcY));
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt__1nScalePixels
      (JNIEnv *env, jclass klass, jlong ptr, jlong dstPixmapPtr, jint samplingOptionsVal1, jint samplingOptionsVal2) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        SkPixmap* dstPixmap = jlongToPtr<SkPixmap*>(dstPixmapPtr);
        return static_cast<jboolean>(pixmap->scalePixels(*dstPixmap, skija::SamplingMode::unpackFrom2Ints(env, samplingOptionsVal1, samplingOptionsVal2)));
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt__1nErase
      (JNIEnv *env, jclass klass, jlong ptr, jint color) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jboolean>(pixmap->erase(color));
    }

    JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_PixmapKt__1nEraseSubset
      (JNIEnv *env, jclass klass, jlong ptr, jint color, jint l, jint t, jint w, jint h) {
        SkPixmap* pixmap = jlongToPtr<SkPixmap*>(ptr);
        return static_cast<jboolean>(pixmap->erase(color, { l, t, w, h }));
    }
}
