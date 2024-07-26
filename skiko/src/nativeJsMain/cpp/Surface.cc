#include <iostream>
#include "GrDirectContext.h"
#include "SkSurface.h"
#include "include/gpu/ganesh/SkSurfaceGanesh.h"
#include "common.h"

#ifdef SK_METAL
#include "include/gpu/ganesh/mtl/SkSurfaceMetal.h"
#include "include/gpu/mtl/GrMtlTypes.h"
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterDirect
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KNativePointer pixelsPtr, KInt rowBytes,
    KInt* surfacePropsInts)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::WrapPixels(
      imageInfo,
      reinterpret_cast<void*>(pixelsPtr),
      rowBytes,
      surfaceProps.get());
    return reinterpret_cast<KNativePointer>(instance.release());
}



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap
  (KNativePointer pixmapPtr, KInt* surfacePropsInts)
{
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(pixmapPtr);
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::WrapPixels(*pixmap, surfaceProps.get());
    return reinterpret_cast<KNativePointer>(instance.release());
}



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRaster
  (KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KInt rowBytes,
    KInt* surfacePropsInts)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(colorSpacePtr);
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::Raster(
      imageInfo,
      rowBytes,
      surfaceProps.get());
    return reinterpret_cast<KNativePointer>(instance.release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterN32Premul
  (KInt width, KInt height) {
    SkImageInfo imageInfo = SkImageInfo::MakeN32Premul(width, height);
    sk_sp<SkSurface> surface = SkSurfaces::Raster(imageInfo);
    return reinterpret_cast<KNativePointer>(surface.release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeFromBackendRenderTarget
  (KNativePointer pContext, KNativePointer pBackendRenderTarget, KInt surfaceOrigin, KInt colorType, KNativePointer colorSpacePtr, KInteropPointer surfacePropsInts) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((pContext));
    GrBackendRenderTarget* backendRenderTarget = reinterpret_cast<GrBackendRenderTarget*>((pBackendRenderTarget));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));

    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsInts);

    sk_sp<SkSurface> surface = SkSurfaces::WrapBackendRenderTarget(
        static_cast<GrRecordingContext*>(context),
        *backendRenderTarget,
        grSurfaceOrigin,
        skColorType,
        colorSpace,
        surfaceProps.get(),
        /* RenderTargetReleaseProc */ nullptr,
        /* ReleaseContext */ nullptr
    );
    return reinterpret_cast<KNativePointer>(surface.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeFromMTKView
  (KNativePointer contextPtr, KNativePointer mtkViewPtr, KInt surfaceOrigin, KInt sampleCount, KInt colorType, KNativePointer colorSpacePtr, KInteropPointer surfacePropsInts) {
#ifdef SK_METAL
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((contextPtr));
    GrMTLHandle* mtkView = reinterpret_cast<GrMTLHandle*>((mtkViewPtr));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsInts);

    sk_sp<SkSurface> surface = SkSurfaces::WrapMTKView(
        static_cast<GrRecordingContext*>(context),
        mtkView,
        grSurfaceOrigin,
        sampleCount,
        skColorType,
        colorSpace,
        surfaceProps.get());
    return reinterpret_cast<KNativePointer>(surface.release());
#else // SK_METAL
    return static_cast<KNativePointer>(nullptr);
#endif // SK_METAL
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRenderTarget
  (KNativePointer contextPtr, KBoolean budgeted,
    KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KInt sampleCount, KInt surfaceOrigin,
    KInteropPointer surfacePropsInts,
    KBoolean shouldCreateWithMips)
{
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((contextPtr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::RenderTarget(
      context, budgeted ? skgpu::Budgeted::kYes : skgpu::Budgeted::kNo,
      imageInfo,
      sampleCount, static_cast<GrSurfaceOrigin>(surfaceOrigin),
      surfaceProps.get(),
      shouldCreateWithMips);
    return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeNull
  (KInt width, KInt height) {
  sk_sp<SkSurface> instance = SkSurfaces::Null(width, height);
  return reinterpret_cast<KNativePointer>(instance.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nGetCanvas
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return reinterpret_cast<KNativePointer>(surface->getCanvas());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Surface__1nGetWidth
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return surface->width();
}

SKIKO_EXPORT KInt org_jetbrains_skia_Surface__1nGetHeight
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return surface->height();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeImageSnapshot
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return reinterpret_cast<KNativePointer>(surface->makeImageSnapshot().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeImageSnapshotR
  (KNativePointer ptr, KInt left, KInt top, KInt right, KInt bottom) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return reinterpret_cast<KNativePointer>(surface->makeImageSnapshot({left, top, right, bottom}).release());
}

SKIKO_EXPORT KInt org_jetbrains_skia_Surface__1nGenerationId
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return surface->generationID();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Surface__1nReadPixelsToPixmap
  (KNativePointer ptr, KNativePointer pixmapPtr, KInt srcX, KInt srcY) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    return surface->readPixels(*pixmap, srcX, srcY);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Surface__1nReadPixels
  (KNativePointer ptr, KNativePointer bitmapPtr, KInt srcX, KInt srcY) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    return surface->readPixels(*bitmap, srcX, srcY);
}

SKIKO_EXPORT void org_jetbrains_skia_Surface__1nWritePixelsFromPixmap
  (KNativePointer ptr, KNativePointer pixmapPtr, KInt x, KInt y) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    surface->writePixels(*pixmap, x, y);
}

SKIKO_EXPORT void org_jetbrains_skia_Surface__1nWritePixels
  (KNativePointer ptr, KNativePointer bitmapPtr, KInt x, KInt y) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>((bitmapPtr));
    surface->writePixels(*bitmap, x, y);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Surface__1nUnique
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return surface->unique();
}


SKIKO_EXPORT void org_jetbrains_skia_Surface__1nGetImageInfo
  (KNativePointer ptr, KInt* imageInfoResult, KNativePointer* colorSpacePtrsArray) {
  SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
  SkImageInfo imageInfo = surface->imageInfo();
  skija::ImageInfo::writeImageInfoForInterop(imageInfo, imageInfoResult, colorSpacePtrsArray);
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeSurface
  (KNativePointer ptr, KInt width, KInt height) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    sk_sp<SkSurface> newSurface = surface->makeSurface(width, height);
    return reinterpret_cast<KNativePointer>(newSurface.release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeSurfaceI
  (KNativePointer ptr, KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    sk_sp<SkSurface> newSurface = surface->makeSurface(imageInfo);
    return reinterpret_cast<KNativePointer>(newSurface.release());
}

SKIKO_EXPORT void org_jetbrains_skia_Surface__1nDraw
  (KNativePointer ptr, KNativePointer canvasPtr, KFloat x, KFloat y, KInt samplingModeValue1, KInt samplingModeValue2, KNativePointer paintPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    surface->draw(canvas, x, y, skija::SamplingMode::unpackFrom2Ints(samplingModeValue1, samplingModeValue2), paint);
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_Surface__1nPeekPixels
  (KNativePointer ptr, KNativePointer dstPixmapPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((dstPixmapPtr));
    return static_cast<KBoolean>(surface->peekPixels(pixmap));
}

SKIKO_EXPORT void org_jetbrains_skia_Surface__1nNotifyContentWillChange
  (KNativePointer ptr, KInt mode) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    surface->notifyContentWillChange(static_cast<SkSurface::ContentChangeMode>(mode));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nGetRecordingContext
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    return reinterpret_cast<KNativePointer>(surface->recordingContext());
}
