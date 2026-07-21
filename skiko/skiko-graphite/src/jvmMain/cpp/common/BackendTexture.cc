#include <jni.h>

#include "include/gpu/graphite/BackendTexture.h"
#if defined(SK_METAL)
#include "include/gpu/graphite/mtl/MtlGraphiteTypes_cpp.h"
#endif

static void deleteBackendTexture(skgpu::graphite::BackendTexture* texture) {
    delete texture;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_BackendTextureKt__1nGetBackendTextureFinalizer(JNIEnv*, jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBackendTexture));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_BackendTextureKt__1nWrapMetalTexture(
        JNIEnv*, jclass, jlong texturePtr, jint width, jint height) {
#if defined(SK_METAL)
    auto texture = skgpu::graphite::BackendTextures::MakeMetal(
            SkISize::Make(width, height),
            reinterpret_cast<CFTypeRef>(static_cast<uintptr_t>(texturePtr)));
    return reinterpret_cast<jlong>(new skgpu::graphite::BackendTexture(texture));
#else
    return 0;
#endif
}
