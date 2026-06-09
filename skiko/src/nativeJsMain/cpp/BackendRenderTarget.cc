#include <iostream>
#include <stdint.h>
#include "ganesh/GrBackendSurface.h"
#include "common.h"
#include "ganesh/gl/GrGLDirectContext.h"
#include "ganesh/gl/GrGLBackendSurface.h"
#ifdef SK_METAL
#include "ganesh/mtl/GrMtlBackendSurface.h"
#include "ganesh/mtl/GrMtlTypes.h"
#endif

#ifdef SK_VULKAN
#include "include/gpu/ganesh/vk/GrVkBackendSurface.h"
#include "include/gpu/ganesh/vk/GrVkTypes.h"
#endif

static void deleteBackendRenderTarget(GrBackendRenderTarget* rt) {
    delete rt;
}
SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendRenderTarget__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteBackendRenderTarget);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendRenderTarget__1nMakeGL
  (KInt width, KInt height, KInt sampleCnt, KInt stencilBits, KInt fbId, KInt fbFormat) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget obj = GrBackendRenderTargets::MakeGL(width, height, sampleCnt, stencilBits, glInfo);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(obj);
    return instance;
}

SKIKO_EXPORT KNativePointer BackendRenderTarget_nMakeMetal
  ( KInt width, KInt height, KNativePointer texturePtr) {
#ifdef SK_METAL
    GrMTLHandle texture = reinterpret_cast<GrMTLHandle>((texturePtr));
    GrMtlTextureInfo fbInfo;
    fbInfo.fTexture.retain(texture);
    GrBackendRenderTarget obj = GrBackendRenderTargets::MakeMtl(width, height, fbInfo);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(obj);
    return instance;
#else
    return 0;
#endif
}

#ifdef SK_DIRECT3D
#include "ganesh/d3d/GrD3DTypes.h"
#include "ganesh/d3d/GrD3DBackendSurface.h"
#endif

SKIKO_EXPORT KNativePointer BackendRenderTarget_MakeDirect3D
  (KInt width, KInt height, KNativePointer texturePtr, KInt format, KInt sampleCnt, KInt levelCnt) {
#ifdef SK_DIRECT3D
    GrD3DTextureResourceInfo texResInfo = {};
    ID3D12Resource* resource = reinterpret_cast<ID3D12Resource*>((texturePtr));
    texResInfo.fResource.retain(resource);
    texResInfo.fResourceState = D3D12_RESOURCE_STATE_COMMON;
    texResInfo.fFormat = static_cast<DXGI_FORMAT>(format);
    texResInfo.fSampleCount = static_cast<uint32_t>(sampleCnt);
    texResInfo.fLevelCount = static_cast<uint32_t>(levelCnt);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(
        GrBackendRenderTargets::MakeD3D(width, height, texResInfo)
    );
    return instance;
#else
    return 0;
#endif
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendRenderTarget__1nMakeVulkan
  (KInt width, KInt height, KNativePointer imagePtr, KInt imageTiling, KInt imageLayout, KInt format, KInt imageUsageFlags, KInt sampleCnt, KInt levelCnt) {
#ifdef SK_VULKAN
    GrVkImageInfo vkInfo = {};
    vkInfo.fImage = reinterpret_cast<VkImage>(imagePtr);
    vkInfo.fImageTiling = static_cast<VkImageTiling>(imageTiling);
    vkInfo.fImageLayout = static_cast<VkImageLayout>(imageLayout);
    vkInfo.fFormat = static_cast<VkFormat>(format);
    vkInfo.fImageUsageFlags = static_cast<VkImageUsageFlags>(imageUsageFlags);
    vkInfo.fSampleCount = static_cast<uint32_t>(sampleCnt);
    vkInfo.fLevelCount = static_cast<uint32_t>(levelCnt);

    GrBackendRenderTarget backendRenderTarget = GrBackendRenderTargets::MakeVk(width, height, vkInfo);
    if (!backendRenderTarget.isValid()) {
        return nullptr;
    }

    GrBackendRenderTarget* instance = new GrBackendRenderTarget(backendRenderTarget);
    return instance;
#else
    return nullptr;
#endif // SK_VULKAN
}
