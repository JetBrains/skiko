#include <jni.h>
#include "SkBitmap.h"
#include "SkImage.h"
#include "ganesh/GrBackendSurface.h"
#include "ganesh/GrDirectContext.h"
#include "include/gpu/ganesh/SkImageGanesh.h"

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_gpu_ganesh_ImageFactoryKt__1nReadPixelsBitmapWithContext
  (JNIEnv* env, jclass jclass, jlong imagePtr, jlong contextPtr, jlong bitmapPtr, jint srcX, jint srcY, jboolean cache) {
    SkImage* image = reinterpret_cast<SkImage*>(static_cast<uintptr_t>(imagePtr));
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return image->readPixels(context, bitmap->info(), bitmap->getPixels(), bitmap->pixmap().rowBytes(), srcX, srcY, cachingHint);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_ganesh_ImageFactoryKt__1nAdoptTextureFrom
  (JNIEnv* env, jclass jclass, jlong contextPtr, jlong backendTexturePtr, jint surfaceOrigin, jint colorType) {
    GrBackendTexture* backendTexture = reinterpret_cast<GrBackendTexture*>(static_cast<uintptr_t>(backendTexturePtr));
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    sk_sp<SkImage> image = SkImages::AdoptTextureFrom(
        static_cast<GrRecordingContext*>(context), *backendTexture,
        static_cast<GrSurfaceOrigin>(surfaceOrigin), static_cast<SkColorType>(colorType));
    return reinterpret_cast<jlong>(image.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_ganesh_ImageFactoryKt__1nAdoptTextureFromAlphaType
  (JNIEnv* env, jclass jclass, jlong contextPtr, jlong backendTexturePtr, jint surfaceOrigin, jint colorType, jint alphaType) {
    GrBackendTexture* backendTexture = reinterpret_cast<GrBackendTexture*>(static_cast<uintptr_t>(backendTexturePtr));
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    sk_sp<SkImage> image = SkImages::AdoptTextureFrom(
        static_cast<GrRecordingContext*>(context), *backendTexture,
        static_cast<GrSurfaceOrigin>(surfaceOrigin), static_cast<SkColorType>(colorType),
        static_cast<SkAlphaType>(alphaType));
    return reinterpret_cast<jlong>(image.release());
}