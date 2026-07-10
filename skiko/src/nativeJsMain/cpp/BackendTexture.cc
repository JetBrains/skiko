#include <iostream>
#include <stdint.h>
#include "common.h"
#include "gpu/ganesh/gl/GrGLBackendSurface.h"
#include "include/gpu/ganesh/SkImageGanesh.h"
#include "include/gpu/ganesh/gl/GrGLTypes.h"
#include "gpu/ganesh/GrBackendSurface.h"

#ifdef SK_METAL
#include "ganesh/mtl/GrMtlBackendSurface.h"
#include "ganesh/mtl/GrMtlTypes.h"
#endif

#ifdef SK_DIRECT3D
#include "ganesh/d3d/GrD3DTypes.h"
#include "ganesh/d3d/GrD3DBackendSurface.h"
#endif

static void deleteBackendTexture(GrBackendTexture* rt) {
    delete rt;
}
SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendTexture__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteBackendTexture);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendTexture__1nMakeGL
  (KInt width, KInt height, KBoolean isMipmapped, KInt textureId, KInt target, KInt format) {
    GrGLTextureInfo textureInfo;
    textureInfo.fID = static_cast<GrGLuint>(textureId);
    textureInfo.fTarget = static_cast<GrGLenum>(target);
    textureInfo.fFormat = static_cast<GrGLenum>(format);

    GrBackendTexture obj = GrBackendTextures::MakeGL(
        width,
        height,
        isMipmapped ? skgpu::Mipmapped::kYes : skgpu::Mipmapped::kNo,
        textureInfo
    );

    GrBackendTexture* instance = new GrBackendTexture(obj);
    return instance;
}

SKIKO_EXPORT void org_jetbrains_skia_BackendTexture__1nGLTextureParametersModified
  (KNativePointer backendTexturePtr) {
    GrBackendTexture* backendTexture = reinterpret_cast<GrBackendTexture*>(backendTexturePtr);
    GrBackendTextures::GLTextureParametersModified(backendTexture);
}

// Always defined (the #ifdef is inside) so the symbol exists even on builds without Metal: the Kotlin side
// then gets a NULL pointer and throws the documented UnsupportedOperationException. Mirrors the JVM binding.
SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendTexture__1nMakeMetal
  (KInt width, KInt height, KNativePointer texturePtr, KBoolean isMipmapped) {
#ifdef SK_METAL
    GrMtlTextureInfo textureInfo;
    textureInfo.fTexture.retain(reinterpret_cast<GrMTLHandle>(texturePtr));

    GrBackendTexture obj = GrBackendTextures::MakeMtl(
        width,
        height,
        isMipmapped ? skgpu::Mipmapped::kYes : skgpu::Mipmapped::kNo,
        textureInfo
    );

    GrBackendTexture* instance = new GrBackendTexture(obj);
    return instance;
#else
    return nullptr;
#endif
}

// Always defined for the same reason as _1nMakeMetal above. resourceState is a client-supplied
// D3D12_RESOURCE_STATES value threaded into fResourceState.
SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendTexture__1nMakeDirect3DTexture
  (KInt width, KInt height, KNativePointer texturePtr, KInt format, KInt resourceState, KInt sampleCnt, KInt levelCnt) {
#ifdef SK_DIRECT3D
    GrD3DTextureResourceInfo texResInfo = {};
    ID3D12Resource* resource = reinterpret_cast<ID3D12Resource*>((texturePtr));
    texResInfo.fResource.retain(resource);
    texResInfo.fResourceState = static_cast<D3D12_RESOURCE_STATES>(resourceState);
    texResInfo.fFormat = static_cast<DXGI_FORMAT>(format);
    texResInfo.fSampleCount = static_cast<uint32_t>(sampleCnt);
    texResInfo.fLevelCount = static_cast<uint32_t>(levelCnt);
    GrBackendTexture* instance = new GrBackendTexture(
        GrBackendTextures::MakeD3D(width, height, texResInfo)
    );
    return instance;
#else
    return nullptr;
#endif
}