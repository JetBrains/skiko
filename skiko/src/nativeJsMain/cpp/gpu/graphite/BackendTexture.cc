#include "../../common.h"
#include "include/gpu/graphite/BackendTexture.h"

#ifdef SK_METAL
#include "gpu/graphite/mtl/MtlGraphiteTypes_cpp.h"
#endif

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_BackendTexture__1nWrapMetalTexture(KNativePointer texturePtr, KInt width, KInt height){
#ifdef SK_METAL
    skgpu::graphite::BackendTexture backendTexture = skgpu::graphite::BackendTextures::MakeMetal(
            SkISize::Make(width, height),
            reinterpret_cast<CFTypeRef>(texturePtr)
    );

    return reinterpret_cast<KNativePointer>(new skgpu::graphite::BackendTexture(backendTexture));
#else
    return static_cast<KNativePointer>(nullptr);
#endif
}


static void deleteBackendTexture(skgpu::graphite::BackendTexture* backendTexture) {
    delete backendTexture;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_BackendTexture__1nGetFinalizer
        () {
    return reinterpret_cast<KNativePointer>(&deleteBackendTexture);
}