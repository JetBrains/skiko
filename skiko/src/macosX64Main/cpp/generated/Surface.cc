
// This file has been auto generated.

#include <iostream>
#include "GrDirectContext.h"
#include "SkSurface.h"
#include "common.h"


extern "C" jlong org_jetbrains_skia_Surface__1nMakeRasterDirect
  (kref __Kinstance,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jlong pixelsPtr, jlong rowBytes,
    jobject surfacePropsObj)
{
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Surface__1nMakeRasterDirect");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Surface__1nMakeRasterDirect
  (kref __Kinstance,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jlong pixelsPtr, jlong rowBytes,
    jobject surfacePropsObj)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> instance = SkSurface::MakeRasterDirect(
      imageInfo,
      reinterpret_cast<void*>(static_cast<uintptr_t>(pixelsPtr)),
      rowBytes,
      surfaceProps.get());
    return reinterpret_cast<jlong>(instance.release());
}
#endif



extern "C" jlong org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap
  (kref __Kinstance,
    jlong pixmapPtr, jobject surfacePropsObj)
{
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap
  (kref __Kinstance,
    jlong pixmapPtr, jobject surfacePropsObj)
{
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> instance = SkSurface::MakeRasterDirect(*pixmap, surfaceProps.get());
    return reinterpret_cast<jlong>(instance.release());
}
#endif



extern "C" jlong org_jetbrains_skia_Surface__1nMakeRaster
  (kref __Kinstance,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jlong rowBytes,
    jobject surfacePropsObj)
{
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Surface__1nMakeRaster");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Surface__1nMakeRaster
  (kref __Kinstance,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jlong rowBytes,
    jobject surfacePropsObj)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
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
    return reinterpret_cast<jlong>(instance.release());
}
#endif


extern "C" jlong org_jetbrains_skia_Surface__1nMakeRasterN32Premul
  (kref __Kinstance, jint width, jint height) {
    sk_sp<SkSurface> surface = SkSurface::MakeRasterN32Premul(
        width, height,
        /* const SkSurfaceProps* */ nullptr
    );
    return reinterpret_cast<jlong>(surface.release());
}


extern "C" jlong org_jetbrains_skia_Surface__1nMakeFromBackendRenderTarget
  (kref __Kinstance, jlong pContext, jlong pBackendRenderTarget, jint surfaceOrigin, jint colorType, jlong colorSpacePtr, jobject surfacePropsObj) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(pContext));
    GrBackendRenderTarget* backendRenderTarget = reinterpret_cast<GrBackendRenderTarget*>(static_cast<uintptr_t>(pBackendRenderTarget));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));

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
    return reinterpret_cast<jlong>(surface.release());
}



extern "C" jlong org_jetbrains_skia_Surface__1nMakeFromMTKView
  (kref __Kinstance, jlong contextPtr, jlong mtkViewPtr, jint surfaceOrigin, jint sampleCount, jint colorType, jlong colorSpacePtr, jobject surfacePropsObj) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Surface__1nMakeFromMTKView");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Surface__1nMakeFromMTKView
  (kref __Kinstance, jlong contextPtr, jlong mtkViewPtr, jint surfaceOrigin, jint sampleCount, jint colorType, jlong colorSpacePtr, jobject surfacePropsObj) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    GrMTLHandle* mtkView = reinterpret_cast<GrMTLHandle*>(static_cast<uintptr_t>(mtkViewPtr));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsObj);

    sk_sp<SkSurface> surface = SkSurface::MakeFromMTKView(
        static_cast<GrRecordingContext*>(context),
        mtkView,
        grSurfaceOrigin,
        sampleCount,
        skColorType,
        colorSpace,
        surfaceProps.get());
    return reinterpret_cast<jlong>(surface.release());
}
#endif



extern "C" jlong org_jetbrains_skia_Surface__1nMakeRenderTarget
  (kref __Kinstance, jlong contextPtr, jboolean budgeted,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jint sampleCount, jint surfaceOrigin,
    jobject surfacePropsObj,
    jboolean shouldCreateWithMips)
{
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Surface__1nMakeRenderTarget");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Surface__1nMakeRenderTarget
  (kref __Kinstance, jlong contextPtr, jboolean budgeted,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jint sampleCount, jint surfaceOrigin,
    jobject surfacePropsObj,
    jboolean shouldCreateWithMips)
{
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
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
    return reinterpret_cast<jlong>(instance.release());
}
#endif


extern "C" jlong org_jetbrains_skia_Surface__1nMakeNull
  (kref __Kinstance, jint width, jint height) {
  sk_sp<SkSurface> instance = SkSurface::MakeNull(width, height);
  return reinterpret_cast<jlong>(instance.release());
}

extern "C" jlong org_jetbrains_skia_Surface__1nGetCanvas
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->getCanvas());
}

extern "C" jint org_jetbrains_skia_Surface__1nGetWidth
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->width();
}

extern "C" jint org_jetbrains_skia_Surface__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->height();
}

extern "C" jlong org_jetbrains_skia_Surface__1nMakeImageSnapshot
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->makeImageSnapshot().release());
}

extern "C" jlong org_jetbrains_skia_Surface__1nMakeImageSnapshotR
  (kref __Kinstance, jlong ptr, jint left, jint top, jint right, jint bottom) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->makeImageSnapshot({left, top, right, bottom}).release());
}

extern "C" jint org_jetbrains_skia_Surface__1nGenerationId
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->generationID();
}

extern "C" jboolean org_jetbrains_skia_Surface__1nReadPixelsToPixmap
  (kref __Kinstance, jlong ptr, jlong pixmapPtr, jint srcX, jint srcY) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    return surface->readPixels(*pixmap, srcX, srcY);
}

extern "C" jboolean org_jetbrains_skia_Surface__1nReadPixels
  (kref __Kinstance, jlong ptr, jlong bitmapPtr, jint srcX, jint srcY) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    return surface->readPixels(*bitmap, srcX, srcY);
}

extern "C" void org_jetbrains_skia_Surface__1nWritePixelsFromPixmap
  (kref __Kinstance, jlong ptr, jlong pixmapPtr, jint x, jint y) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    surface->writePixels(*pixmap, x, y);
}

extern "C" void org_jetbrains_skia_Surface__1nWritePixels
  (kref __Kinstance, jlong ptr, jlong bitmapPtr, jint x, jint y) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    surface->writePixels(*bitmap, x, y);
}

extern "C" void org_jetbrains_skia_Surface__1nFlushAndSubmit
  (kref __Kinstance, jlong ptr, jboolean syncCpu) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    surface->flushAndSubmit(syncCpu);
}

extern "C" void org_jetbrains_skia_Surface__1nFlush
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    surface->flush();
}

extern "C" jboolean org_jetbrains_skia_Surface__1nUnique
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->unique();
}


extern "C" jobject org_jetbrains_skia_Surface__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_Surface__1nGetImageInfo");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Surface__1nGetImageInfo
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    const SkImageInfo& info = surface->imageInfo();
    return env->NewObject(skija::ImageInfo::cls, skija::ImageInfo::ctor,
        info.width(),
        info.height(),
        static_cast<jint>(info.colorType()),
        static_cast<jint>(info.alphaType()),
        reinterpret_cast<jlong>(info.refColorSpace().release()));
}
#endif


extern "C" jlong org_jetbrains_skia_Surface__1nMakeSurface
  (kref __Kinstance, jlong ptr, jint width, jint height) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    sk_sp<SkSurface> newSurface = surface->makeSurface(width, height);
    return reinterpret_cast<jlong>(newSurface.release());
}

extern "C" jlong org_jetbrains_skia_Surface__1nMakeSurfaceI
  (kref __Kinstance, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    sk_sp<SkSurface> newSurface = surface->makeSurface(imageInfo);
    return reinterpret_cast<jlong>(newSurface.release());
}

extern "C" void org_jetbrains_skia_Surface__1nDraw
  (kref __Kinstance, jlong ptr, jlong canvasPtr, jfloat x, jfloat y, jlong paintPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    surface->draw(canvas, x, y, paint);
}

extern "C" jboolean org_jetbrains_skia_Surface__1nPeekPixels
  (kref __Kinstance, jlong ptr, jlong dstPixmapPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(dstPixmapPtr));
    return static_cast<jboolean>(surface->peekPixels(pixmap));
}

extern "C" void org_jetbrains_skia_Surface__1nNotifyContentWillChange
  (kref __Kinstance, jlong ptr, jint mode) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    surface->notifyContentWillChange(static_cast<SkSurface::ContentChangeMode>(mode));
}

extern "C" jlong org_jetbrains_skia_Surface__1nGetRecordingContext
  (kref __Kinstance, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->recordingContext());
}
