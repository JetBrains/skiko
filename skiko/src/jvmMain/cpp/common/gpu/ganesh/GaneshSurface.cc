#include <iostream>
#include <jni.h>
#include "../../interop.hh"
#include "include/gpu/ganesh/SkSurfaceGanesh.h"
#include "ganesh/GrDirectContext.h"

#ifdef SK_METAL
#include "include/gpu/ganesh/mtl/SkSurfaceMetal.h"
#include "include/gpu/ganesh/mtl/GrMtlTypes.h"
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