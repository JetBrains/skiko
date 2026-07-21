#include "common.h"

#include "include/core/SkColorSpace.h"
#include "include/gpu/graphite/BackendTexture.h"
#include "include/gpu/graphite/Recorder.h"
#include "include/gpu/graphite/Surface.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_SurfaceFactory__1nMakeFromBackendTexture(
        KNativePointer recorderPtr,
        KNativePointer backendTexturePtr,
        KInt colorType,
        KNativePointer colorSpacePtr,
        KInteropPointer surfacePropsValues) {
    auto recorder = reinterpret_cast<skgpu::graphite::Recorder*>(recorderPtr);
    auto backendTexture = reinterpret_cast<skgpu::graphite::BackendTexture*>(backendTexturePtr);
    auto colorSpace = sk_ref_sp(reinterpret_cast<SkColorSpace*>(colorSpacePtr));
    auto surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsValues);
    return reinterpret_cast<KNativePointer>(SkSurfaces::WrapBackendTexture(
            recorder,
            *backendTexture,
            static_cast<SkColorType>(colorType),
            std::move(colorSpace),
            surfaceProps.get()).release());
}
