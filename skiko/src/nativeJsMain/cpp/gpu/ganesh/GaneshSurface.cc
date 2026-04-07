#include <iostream>
#include "ganesh/GrDirectContext.h"
#include "SkSurface.h"
#include "include/gpu/ganesh/SkSurfaceGanesh.h"
#include "../../common.h"

#ifdef SK_METAL
#include "include/gpu/ganesh/mtl/SkSurfaceMetal.h"
#include "include/gpu/ganesh/mtl/GrMtlTypes.h"
#endif

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