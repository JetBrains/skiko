#include <jni.h>

#include "interop.hh"
#include "include/core/SkColorSpace.h"
#include "include/gpu/graphite/BackendTexture.h"
#include "include/gpu/graphite/Recorder.h"
#include "include/gpu/graphite/Surface.h"

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_SurfaceFactoryKt__1nMakeFromBackendTexture(
        JNIEnv* env,
        jclass,
        jlong recorderPtr,
        jlong backendTexturePtr,
        jint colorType,
        jlong colorSpacePtr,
        jintArray surfacePropsValues) {
    auto recorder = reinterpret_cast<skgpu::graphite::Recorder*>(
            static_cast<uintptr_t>(recorderPtr));
    auto backendTexture = reinterpret_cast<skgpu::graphite::BackendTexture*>(
            static_cast<uintptr_t>(backendTexturePtr));
    auto colorSpace = sk_ref_sp(reinterpret_cast<SkColorSpace*>(
            static_cast<uintptr_t>(colorSpacePtr)));
    auto surfaceProps = skija::SurfaceProps::toSkSurfaceProps(env, surfacePropsValues);
    return reinterpret_cast<jlong>(SkSurfaces::WrapBackendTexture(
            recorder,
            *backendTexture,
            static_cast<SkColorType>(colorType),
            std::move(colorSpace),
            surfaceProps.get()).release());
}
