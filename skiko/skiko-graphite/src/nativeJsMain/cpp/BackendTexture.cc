#include "common.h"

#include "include/gpu/graphite/BackendTexture.h"
#include "include/gpu/graphite/mtl/MtlGraphiteTypes_cpp.h"

static void deleteBackendTexture(skgpu::graphite::BackendTexture* texture) {
    delete texture;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_BackendTexture__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteBackendTexture);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_BackendTexture__1nWrapMetalTexture(
        KNativePointer texturePtr, KInt width, KInt height) {
    auto texture = skgpu::graphite::BackendTextures::MakeMetal(
            SkISize::Make(width, height),
            reinterpret_cast<CFTypeRef>(texturePtr));
    return reinterpret_cast<KNativePointer>(new skgpu::graphite::BackendTexture(texture));
}
