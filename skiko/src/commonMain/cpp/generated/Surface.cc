
// This file has been auto generated.

#include <iostream>
#include "GrDirectContext.h"
#include "SkSurface.h"
#include "common.h"


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterDirect
  (KInteropPointer __Kinstance,
    KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KNativePointer pixelsPtr, KNativePointer rowBytes,
    KInteropPointer surfacePropsObj)
{
    TODO("implement org_jetbrains_skia_Surface__1nMakeRasterDirect");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterDirect
  (KInteropPointer __Kinstance,
    KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KNativePointer pixelsPtr, KNativePointer rowBytes,
    KInteropPointer surfacePropsObj)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> instance = SkSurface::MakeRasterDirect(
      imageInfo,
      reinterpret_cast<void*>((pixelsPtr)),
      rowBytes,
      surfaceProps.get());
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap
  (KInteropPointer __Kinstance,
    KNativePointer pixmapPtr, KInteropPointer surfacePropsObj)
{
    TODO("implement org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap
  (KInteropPointer __Kinstance,
    KNativePointer pixmapPtr, KInteropPointer surfacePropsObj)
{
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>((pixmapPtr));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> instance = SkSurface::MakeRasterDirect(*pixmap, surfaceProps.get());
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRaster
  (KInteropPointer __Kinstance,
    KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KNativePointer rowBytes,
    KInteropPointer surfacePropsObj)
{
    TODO("implement org_jetbrains_skia_Surface__1nMakeRaster");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRaster
  (KInteropPointer __Kinstance,
    KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KNativePointer rowBytes,
    KInteropPointer surfacePropsObj)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> instance = SkSurface::MakeRaster(
      imageInfo,
      rowBytes,
      surfaceProps.get());
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRasterN32Premul
  (KInt width, KInt height) {
    sk_sp<SkSurface> surface = SkSurface::MakeRasterN32Premul(
        width, height,
        /* const SkSurfaceProps* */ nullptr
    );
    return reinterpret_cast<KNativePointer>(surface.release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeFromBackendRenderTarget
  (KNativePointer pContext, KNativePointer pBackendRenderTarget, KInt surfaceOrigin, KInt colorType, KNativePointer colorSpacePtr, KInteropPointer surfacePropsObj) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((pContext));
    GrBackendRenderTarget* backendRenderTarget = reinterpret_cast<GrBackendRenderTarget*>((pBackendRenderTarget));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));

    // std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> surface = SkSurface::MakeFromBackendRenderTarget(
        static_cast<GrRecordingContext*>(context),
        *backendRenderTarget,
        grSurfaceOrigin,
        skColorType,
        colorSpace,
        // "TODO: we silently ignore the surfacePropsObj arg for now.
        // surfaceProps,
        nullptr,
        /* RenderTargetReleaseProc */ nullptr,
        /* ReleaseContext */ nullptr
    );
    return reinterpret_cast<KNativePointer>(surface.release());
}



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeFromMTKView
  (KNativePointer contextPtr, KNativePointer mtkViewPtr, KInt surfaceOrigin, KInt sampleCount, KInt colorType, KNativePointer colorSpacePtr, KInteropPointer surfacePropsObj) {
    TODO("implement org_jetbrains_skia_Surface__1nMakeFromMTKView");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeFromMTKView
  (KNativePointer contextPtr, KNativePointer mtkViewPtr, KInt surfaceOrigin, KInt sampleCount, KInt colorType, KNativePointer colorSpacePtr, KInteropPointer surfacePropsObj) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((contextPtr));
    GrMTLHandle* mtkView = reinterpret_cast<GrMTLHandle*>((mtkViewPtr));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> surface = SkSurface::MakeFromMTKView(
        static_cast<GrRecordingContext*>(context),
        mtkView,
        grSurfaceOrigin,
        sampleCount,
        skColorType,
        colorSpace,
        surfaceProps.get());
    return reinterpret_cast<KNativePointer>(surface.release());
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRenderTarget
  (KNativePointer contextPtr, KBoolean budgeted,
    KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KInt sampleCount, KInt surfaceOrigin,
    KInteropPointer surfacePropsObj,
    KBoolean shouldCreateWithMips)
{
    TODO("implement org_jetbrains_skia_Surface__1nMakeRenderTarget");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeRenderTarget
  (KNativePointer contextPtr, KBoolean budgeted,
    KInt width, KInt height, KInt colorType, KInt alphaType, KNativePointer colorSpacePtr,
    KInt sampleCount, KInt surfaceOrigin,
    KInteropPointer surfacePropsObj,
    KBoolean shouldCreateWithMips)
{
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((contextPtr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>((colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> instance = SkSurface::MakeRenderTarget(
      context, budgeted ? SkBudgeted::kYes : SkBudgeted::kNo,
      imageInfo,
      sampleCount, static_cast<GrSurfaceOrigin>(surfaceOrigin),
      surfaceProps.get(),
      shouldCreateWithMips);
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Surface__1nMakeNull
  (KInt width, KInt height) {
  sk_sp<SkSurface> instance = SkSurface::MakeNull(width, height);
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

SKIKO_EXPORT void org_jetbrains_skia_Surface__1nFlushAndSubmit
  (KNativePointer ptr, KBoolean syncCpu) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    surface->flushAndSubmit(syncCpu);
}

SKIKO_EXPORT void org_jetbrains_skia_Surface__1nFlush
  (KNativePointer ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    surface->flush();
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
  (KNativePointer ptr, KNativePointer canvasPtr, KFloat x, KFloat y, KNativePointer paintPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>((paintPtr));
    surface->draw(canvas, x, y, paint);
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
