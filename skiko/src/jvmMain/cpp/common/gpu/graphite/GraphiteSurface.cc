#include <iostream>
#include <jni.h>
#include "../../interop.hh"
#include "SkSurface.h"

#include "include/gpu/graphite/Context.h"
#include "include/gpu/graphite/ContextOptions.h"
#include "include/gpu/graphite/GraphiteTypes.h"
#include "include/gpu/graphite/TextureInfo.h"
#include "gpu/graphite/Recorder.h"
#include "gpu/graphite/Surface.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_SurfaceFactoryKt__1nMakeFromBackendTexture
        (JNIEnv* env, jclass jclass, jlong pRecorder, jlong pBackendTexture, jint colorType, jlong colorSpacePtr, jintArray surfacePropsInts) {
    skgpu::graphite::Recorder* graphiteRecorder = reinterpret_cast<skgpu::graphite::Recorder*>(static_cast<uintptr_t>(pRecorder));
    skgpu::graphite::BackendTexture* backendTexture = reinterpret_cast<skgpu::graphite::BackendTexture*>(static_cast<uintptr_t>(pBackendTexture));
    SkColorType skColorType = static_cast<SkColorType>(colorType);
    sk_sp<SkColorSpace> colorSpace = sk_ref_sp<SkColorSpace>(reinterpret_cast<SkColorSpace*>(static_cast<uintptr_t>(colorSpacePtr)));
    std::unique_ptr<SkSurfaceProps> surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsInts);

    auto surface = SkSurfaces::WrapBackendTexture(
            graphiteRecorder,
            *backendTexture,
            skColorType,
            colorSpace,
            surfaceProps.get());

    return reinterpret_cast<jlong>(surface.release());
}