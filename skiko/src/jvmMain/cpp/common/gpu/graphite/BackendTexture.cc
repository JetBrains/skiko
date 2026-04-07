#include <jni.h>
#include "../../interop.hh"
#include "include/gpu/graphite/BackendTexture.h"

#ifdef SK_METAL
#include "gpu/graphite/mtl/MtlGraphiteTypes_cpp.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_BackendTextureKt_BackendTexture_1nWrapMetalTexture
        (JNIEnv* env, jclass jclass, jlong texturePtr, jint width, jint height) {
    skgpu::graphite::BackendTexture backendTexture = skgpu::graphite::BackendTextures::MakeMetal(
            SkISize::Make(width, height),
            reinterpret_cast<void *>(texturePtr)
    );

    return reinterpret_cast<jlong>(new skgpu::graphite::BackendTexture(backendTexture));
}
#endif

static void deleteBackendTexture(skgpu::graphite::BackendTexture *rt) {
    delete rt;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_gpu_graphite_BackendTextureKt_BackendTexture_1nGetFinalizer
        (JNIEnv *env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBackendTexture));
}