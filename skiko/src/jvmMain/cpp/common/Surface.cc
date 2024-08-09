#include <iostream>
#include <jni.h>
#include "GrDirectContext.h"
#include "SkSurface.h"
#include "interop.hh"

#include "include/gpu/ganesh/SkSurfaceGanesh.h"

#ifdef SK_METAL
#include "include/gpu/ganesh/mtl/SkSurfaceMetal.h"
#include "include/gpu/ganesh/mtl/GrMtlTypes.h"
#endif

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeRasterDirect
  (JNIEnv* env, jclass jclass,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jlong pixelsPtr, jint rowBytes,
    jintArray surfacePropsInts)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::WrapPixels(
      imageInfo,
      reinterpret_cast<void*>(static_cast<uintptr_t>(pixelsPtr)),
      rowBytes,
      surfaceProps.get());
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeRasterDirectWithPixmap
  (JNIEnv* env, jclass jclass,
    jlong pixmapPtr, jintArray surfacePropsInts)
{
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::WrapPixels(*pixmap, surfaceProps.get());
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeRaster
  (JNIEnv* env, jclass jclass,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jint rowBytes,
    jintArray surfacePropsInts)
{
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::Raster(
      imageInfo,
      rowBytes,
      surfaceProps.get());
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeRasterN32Premul
  (JNIEnv* env, jclass jclass, jint width, jint height) {
  // todo rename kotlin methods accordingly
    SkImageInfo imageInfo = SkImageInfo::MakeN32Premul(width, height);
    sk_sp<SkSurface> surface = SkSurfaces::Raster(imageInfo);
    return reinterpret_cast<jlong>(surface.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeFromBackendRenderTarget
  (JNIEnv* env, jclass jclass, jlong pContext, jlong pBackendRenderTarget, jint surfaceOrigin, jint colorType, jlong colorSpacePtr, jintArray surfacePropsInts) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(pContext));
    GrBackendRenderTarget* backendRenderTarget = reinterpret_cast<GrBackendRenderTarget*>(static_cast<uintptr_t>(pBackendRenderTarget));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);

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
    return reinterpret_cast<jlong>(surface.release());
}

#ifdef SK_METAL
extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeFromMTKView
  (JNIEnv* env, jclass jclass, jlong contextPtr, jlong mtkViewPtr, jint surfaceOrigin, jint sampleCount, jint colorType, jlong colorSpacePtr, jintArray surfacePropsInts) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    GrMTLHandle* mtkView = reinterpret_cast<GrMTLHandle*>(static_cast<uintptr_t>(mtkViewPtr));
    GrSurfaceOrigin grSurfaceOrigin = static_cast<GrSurfaceOrigin>(surfaceOrigin);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);

    sk_sp<SkSurface> surface = SkSurfaces::WrapMTKView(
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

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeRenderTarget
  (JNIEnv* env, jclass jclass, jlong contextPtr, jboolean budgeted,
    jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr,
    jint sampleCount, jint surfaceOrigin,
    jintArray surfacePropsInts,
    jboolean shouldCreateWithMips)
{
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(contextPtr));
    SkColorSpace* colorSpace = reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr));
    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              static_cast<SkColorType>(colorType),
                                              static_cast<SkAlphaType>(alphaType),
                                              sk_ref_sp<SkColorSpace>(colorSpace));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);

    sk_sp<SkSurface> instance = SkSurfaces::RenderTarget(
      context, budgeted ? skgpu::Budgeted::kYes : skgpu::Budgeted::kNo,
      imageInfo,
      sampleCount, static_cast<GrSurfaceOrigin>(surfaceOrigin),
      surfaceProps.get(),
      shouldCreateWithMips);
    return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeNull
  (JNIEnv* env, jclass jclass, jint width, jint height) {
  sk_sp<SkSurface> instance = SkSurfaces::Null(width, height);
  return reinterpret_cast<jlong>(instance.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nGetCanvas
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->getCanvas());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_SurfaceKt_Surface_1nGetWidth
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->width();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_SurfaceKt_Surface_1nGetHeight
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->height();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeImageSnapshot
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->makeImageSnapshot().release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeImageSnapshotR
  (JNIEnv* env, jclass jclass, jlong ptr, jint left, jint top, jint right, jint bottom) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->makeImageSnapshot({left, top, right, bottom}).release());
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_SurfaceKt__1nGenerationId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->generationID();
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_SurfaceKt__1nReadPixelsToPixmap
  (JNIEnv* env, jclass jclass, jlong ptr, jlong pixmapPtr, jint srcX, jint srcY) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    return surface->readPixels(*pixmap, srcX, srcY);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_SurfaceKt_Surface_1nReadPixels
  (JNIEnv* env, jclass jclass, jlong ptr, jlong bitmapPtr, jint srcX, jint srcY) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    return surface->readPixels(*bitmap, srcX, srcY);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_SurfaceKt__1nWritePixelsFromPixmap
  (JNIEnv* env, jclass jclass, jlong ptr, jlong pixmapPtr, jint x, jint y) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(pixmapPtr));
    surface->writePixels(*pixmap, x, y);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_SurfaceKt_Surface_1nWritePixels
  (JNIEnv* env, jclass jclass, jlong ptr, jlong bitmapPtr, jint x, jint y) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkBitmap* bitmap = reinterpret_cast<SkBitmap*>(static_cast<uintptr_t>(bitmapPtr));
    surface->writePixels(*bitmap, x, y);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_SurfaceKt__1nUnique
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return surface->unique();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_SurfaceKt_Surface_1nGetImageInfo
  (JNIEnv* env, jclass jclass, jlong ptr, jintArray imageInfoResult, jlongArray colorSpaceResultPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkImageInfo imageInfo = surface->imageInfo();

    skija::ImageInfo::writeImageInfoForInterop(
        env, imageInfo, imageInfoResult, colorSpaceResultPtr
    );
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeSurface
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    sk_sp<SkSurface> newSurface = surface->makeSurface(width, height);
    return reinterpret_cast<jlong>(newSurface.release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nMakeSurfaceI
  (JNIEnv* env, jclass jclass, jlong ptr, jint width, jint height, jint colorType, jint alphaType, jlong colorSpacePtr) {
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

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_SurfaceKt__1nDraw
  (JNIEnv* env, jclass jclass, jlong ptr, jlong canvasPtr, jfloat x, jfloat y, jint samplingModeVal1, jint samplingModeVal2, jlong paintPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    SkPaint* paint = reinterpret_cast<SkPaint*>(static_cast<uintptr_t>(paintPtr));
    surface->draw(canvas, x, y, skija::SamplingMode::unpackFrom2Ints(env, samplingModeVal1, samplingModeVal2), paint);
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_jetbrains_skia_SurfaceKt__1nPeekPixels
  (JNIEnv *env, jclass jclass, jlong ptr, jlong dstPixmapPtr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    SkPixmap* pixmap = reinterpret_cast<SkPixmap*>(static_cast<uintptr_t>(dstPixmapPtr));
    return static_cast<jboolean>(surface->peekPixels(pixmap));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_SurfaceKt__1nNotifyContentWillChange
  (JNIEnv* env, jclass jclass, jlong ptr, jint mode) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    surface->notifyContentWillChange(static_cast<SkSurface::ContentChangeMode>(mode));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_SurfaceKt__1nGetRecordingContext
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkSurface* surface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(ptr));
    return reinterpret_cast<jlong>(surface->recordingContext());
}
