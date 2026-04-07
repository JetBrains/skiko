#include <iostream>
#include "SkSurface.h"
#include "../../common.h"

#include "include/gpu/graphite/Context.h"
#include "include/gpu/graphite/ContextOptions.h"
#include "include/gpu/graphite/GraphiteTypes.h"
#include "include/gpu/graphite/TextureInfo.h"
#include "gpu/graphite/Recorder.h"
#include "gpu/graphite/Surface.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_SurfaceFactory__1nMakeFromBackendTexture
(KNativePointer pRecorder,
KNativePointer pBackendTexture,
KInt colorType,
KNativePointer colorSpacePtr,
KInteropPointer surfacePropsInts) {
    skgpu::graphite::Recorder* graphiteRecorder = reinterpret_cast<skgpu::graphite::Recorder*>(pRecorder);
    skgpu::graphite::BackendTexture* backendTexture = reinterpret_cast<skgpu::graphite::BackendTexture*>(pBackendTexture);
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>((colorSpacePtr)));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(surfacePropsInts);

    auto surface = SkSurfaces::WrapBackendTexture(
            graphiteRecorder,
            *backendTexture,
            skColorType,
            colorSpace,
            surfaceProps.get());

    return reinterpret_cast<KNativePointer>(surface.release());
}
