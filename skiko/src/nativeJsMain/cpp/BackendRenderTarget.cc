#include <iostream>
#include <stdint.h>
#include "GrBackendSurface.h"
#include "common.h"
#include "ganesh/gl/GrGLDirectContext.h"
#include "ganesh/gl/GrGLBackendSurface.h"
#ifdef SK_METAL
#include "ganesh/mtl/GrMtlBackendSurface.h"
#include "ganesh/mtl/GrMtlTypes.h"
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
    auto obj = GrBackendRenderTargets::MakeGL(width, height, sampleCnt, stencilBits, glInfo);
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
    auto instance = new GrBackendRenderTarget(obj);
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
