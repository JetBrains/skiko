#include "GrBackendSurface.h"
#include "common.h"

#include <emscripten.h>

static void deleteBackendRenderTarget(GrBackendRenderTarget* rt) {
    delete rt;
}

EMSCRIPTEN_KEEPALIVE
extern "C" KPointer BackendRenderTarget_nGetFinalizer() {
    return reinterpret_cast<KPointer>(reinterpret_cast<uintptr_t>(&deleteBackendRenderTarget));
}

EMSCRIPTEN_KEEPALIVE
extern "C" KPointer BackendRenderTarget_nMakeGL
  (KInt width, KInt height, KInt sampleCnt, KInt stencilBits, KInt fbId, KInt fbFormat) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, sampleCnt, stencilBits, glInfo);
    return reinterpret_cast<KPointer>(instance);
}

EMSCRIPTEN_KEEPALIVE
extern "C" KPointer BackendRenderTarget_nMakeMetal
  (KInt width, KInt height, KPointer texturePtr) {
#ifdef SK_METAL
    GrMTLHandle texture = reinterpret_cast<GrMTLHandle>(static_cast<uintptr_t>(texturePtr));
    GrMtlTextureInfo fbInfo;
    fbInfo.fTexture.retain(texture);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, fbInfo);
    return reinterpret_cast<KPointer>(instance);
#else
    return 0;
#endif
}

#ifdef SK_DIRECT3D
#include "d3d/GrD3DTypes.h"
#endif

EMSCRIPTEN_KEEPALIVE
extern "C" KPointer BackendRenderTarget_MakeDirect3D
  (KInt width, KInt height, KPointer texturePtr, KInt format, KInt sampleCnt, KInt levelCnt) {
#ifdef SK_DIRECT3D
    GrD3DTextureResourceInfo texResInfo = {};
    ID3D12Resource* resource = reinterpret_cast<ID3D12Resource*>(static_cast<uintptr_t>(texturePtr));
    texResInfo.fResource.retain(resource);
    texResInfo.fResourceState = D3D12_RESOURCE_STATE_COMMON;
    texResInfo.fFormat = static_cast<DXGI_FORMAT>(format);
    texResInfo.fSampleCount = static_cast<uint32_t>(sampleCnt);
    texResInfo.fLevelCount = static_cast<uint32_t>(levelCnt);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, texResInfo);
    return reinterpret_cast<KPointer>(instance);
#else
    return 0;
#endif
}