#include <iostream>
#include <stdint.h>
#include "GrBackendSurface.h"
#include "common.h"

static void deleteBackendRenderTarget(GrBackendRenderTarget* rt) {
    delete rt;
}

extern "C" jlong BackendRenderTarget_nGetFinalizer() {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBackendRenderTarget));
}

extern "C" jlong BackendRenderTarget_nMakeGL
  (kref __Kinstance, jint width, jint height, jint sampleCnt, jint stencilBits, jint fbId, jint fbFormat) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, sampleCnt, stencilBits, glInfo);
    return reinterpret_cast<jlong>(instance);
}

extern "C" jlong BackendRenderTarget_nMakeMetal
  (kref __Kinstance, jint width, jint height, jlong texturePtr) {
#ifdef SK_METAL
    GrMTLHandle texture = reinterpret_cast<GrMTLHandle>(static_cast<uintptr_t>(texturePtr));
    GrMtlTextureInfo fbInfo;
    fbInfo.fTexture.retain(texture);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, fbInfo);
    return reinterpret_cast<jlong>(instance);
#else
    return 0;
#endif
}

#ifdef SK_DIRECT3D
#include "d3d/GrD3DTypes.h"
#endif

extern "C" jlong BackendRenderTarget_MakeDirect3D
  (kref __Kinstance, jint width, jint height, jlong texturePtr, jint format, jint sampleCnt, jint levelCnt) {
#ifdef SK_DIRECT3D
    GrD3DTextureResourceInfo texResInfo = {};
    ID3D12Resource* resource = reinterpret_cast<ID3D12Resource*>(static_cast<uintptr_t>(texturePtr));
    texResInfo.fResource.retain(resource);
    texResInfo.fResourceState = D3D12_RESOURCE_STATE_COMMON;
    texResInfo.fFormat = static_cast<DXGI_FORMAT>(format);
    texResInfo.fSampleCount = static_cast<uint32_t>(sampleCnt);
    texResInfo.fLevelCount = static_cast<uint32_t>(levelCnt);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(width, height, texResInfo);
    return reinterpret_cast<jlong>(instance);
#else
    return 0;
#endif
}