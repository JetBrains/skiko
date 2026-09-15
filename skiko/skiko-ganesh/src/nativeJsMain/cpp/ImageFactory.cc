#include "SkBitmap.h"
#include "SkImage.h"
#include "common.h"
#include "ganesh/GrBackendSurface.h"
#include "ganesh/GrDirectContext.h"
#include "include/gpu/ganesh/SkImageGanesh.h"

SKIKO_EXPORT KBoolean org_jetbrains_skia_gpu_ganesh_ImageFactory__1nReadPixelsBitmapWithContext
  (KNativePointer imagePtr, KNativePointer contextPtr, KNativePointer bitmapPtr, KInt srcX, KInt srcY, KBoolean cache) {
    SkImage* image = reinterpret_cast<SkImage*>(imagePtr);
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(contextPtr);
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(bitmapPtr);
    auto cachingHint = cache ? SkImage::CachingHint::kAllow_CachingHint : SkImage::CachingHint::kDisallow_CachingHint;
    return image->readPixels(context, bitmap->info(), bitmap->getPixels(), bitmap->pixmap().rowBytes(), srcX, srcY, cachingHint);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_ganesh_ImageFactory__1nAdoptTextureFrom
  (KNativePointer contextPtr, KNativePointer backendTexturePtr, KInt surfaceOrigin, KInt colorType) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(contextPtr);
    GrBackendTexture* backendTexture = reinterpret_cast<GrBackendTexture*>(backendTexturePtr);
    sk_sp<SkImage> image = SkImages::AdoptTextureFrom(
        static_cast<GrRecordingContext*>(context), *backendTexture,
        static_cast<GrSurfaceOrigin>(surfaceOrigin), static_cast<SkColorType>(colorType));
    return reinterpret_cast<KNativePointer>(image.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_ganesh_ImageFactory__1nAdoptTextureFromAlphaType
  (KNativePointer contextPtr, KNativePointer backendTexturePtr, KInt surfaceOrigin, KInt colorType, KInt alphaType) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(contextPtr);
    GrBackendTexture* backendTexture = reinterpret_cast<GrBackendTexture*>(backendTexturePtr);
    sk_sp<SkImage> image = SkImages::AdoptTextureFrom(
        static_cast<GrRecordingContext*>(context), *backendTexture,
        static_cast<GrSurfaceOrigin>(surfaceOrigin), static_cast<SkColorType>(colorType),
        static_cast<SkAlphaType>(alphaType));
    return reinterpret_cast<KNativePointer>(image.release());
}