#include <iostream>
#include <stdint.h>
#include "GrBackendSurface.h"
#include "common.h"

static void deleteBackendRenderTarget(GrBackendRenderTarget* rt) {
    delete rt;
}
SKIKO_EXPORT KNativePointer org_jetbrains_skiko_BackendRenderTarget__nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteBackendRenderTarget);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_BackendRenderTarget__nMakeGL
  (KInt width, KInt height, KInt sampleCnt, KInt stencilBits, KInt fbId, KInt fbFormat) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, sampleCnt, stencilBits, glInfo);
    return instance;
}

SKIKO_EXPORT KNativePointer BackendRenderTarget_nMakeMetal
  ( KInt width, KInt height, KNativePointer texturePtr) {
#ifdef SK_METAL
    GrMTLHandle texture = reinterpret_cast<GrMTLHandle>((texturePtr));
    GrMtlTextureInfo fbInfo;
    fbInfo.fTexture.retain(texture);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, fbInfo);
    return instance;
#else
    return 0;
#endif
}

#ifdef SK_DIRECT3D
#include "d3d/GrD3DTypes.h"
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
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, texResInfo);
    return instance;
#else
    return 0;
#endif
}
