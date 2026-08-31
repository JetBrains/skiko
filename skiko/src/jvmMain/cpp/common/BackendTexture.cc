#include <iostream>
#include <jni.h>
#include "ganesh/GrBackendSurface.h"
#include "SkData.h"
#include "SkImage.h"
#include <ganesh/gl/GrGLBackendSurface.h>
#include "ganesh/gl/GrGLBackendSurface.h"
#include "include/gpu/ganesh/SkImageGanesh.h"
#include "ganesh/GrDirectContext.h"
#include "ganesh/gl/GrGLDirectContext.h"

#ifdef SK_METAL
#include "ganesh/mtl/GrMtlBackendSurface.h"
#include "ganesh/mtl/GrMtlTypes.h"
#endif

#ifdef SK_DIRECT3D
#include "ganesh/d3d/GrD3DTypes.h"
#include "ganesh/d3d/GrD3DBackendSurface.h"
#endif


static void deleteBackendTexture(GrBackendTexture* rt) {
    // std::cout << "Deleting [GrBackendTexture " << rt << "]" << std::endl;
    delete rt;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BackendTextureKt_BackendTexture_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBackendTexture));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BackendTextureKt__1nMakeGL
  (JNIEnv* env, jclass jclass, jint width, jint height, jboolean isMipmapped, jint textureId, jint target, jint format) {
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
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_BackendTextureKt__1nGLTextureParametersModified
  (JNIEnv* env, jclass jclass, jlong backendTexturePtr) {
    GrBackendTexture* backendTexture = reinterpret_cast<GrBackendTexture*>(static_cast<uintptr_t>(backendTexturePtr));
    GrBackendTextures::GLTextureParametersModified(backendTexture);
}

// Always defined (the #ifdef is inside) so the JNI symbol exists even on builds without Metal: the Kotlin
// side then gets a NULL pointer and throws the documented UnsupportedOperationException, instead of an
// UnsatisfiedLinkError on a missing symbol. Mirrors the Kotlin/Native binding.
extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BackendTextureKt__1nMakeMetal
  (JNIEnv* env, jclass jclass, jint width, jint height, jlong texturePtr, jboolean isMipmapped) {
#ifdef SK_METAL
    GrMTLHandle texture = reinterpret_cast<GrMTLHandle>(static_cast<uintptr_t>(texturePtr));
    GrMtlTextureInfo textureInfo;
    textureInfo.fTexture.retain(texture);

    GrBackendTexture obj = GrBackendTextures::MakeMtl(
        width,
        height,
        isMipmapped ? skgpu::Mipmapped::kYes : skgpu::Mipmapped::kNo,
        textureInfo
    );

    GrBackendTexture* instance = new GrBackendTexture(obj);
    return reinterpret_cast<jlong>(instance);
#else
    return 0;
#endif
}

// Always defined for the same reason as _1nMakeMetal above (Direct3D-less builds return NULL and the Kotlin
// side throws the documented UnsupportedOperationException rather than an UnsatisfiedLinkError). resourceState
// is a client-supplied D3D12_RESOURCE_STATES value threaded into fResourceState.
extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BackendTextureKt__1nMakeDirect3DTexture
  (JNIEnv* env, jclass jclass, jint width, jint height, jlong texturePtr, jint format, jint resourceState, jint sampleCnt, jint levelCnt) {
#ifdef SK_DIRECT3D
    GrD3DTextureResourceInfo texResInfo = {};
    ID3D12Resource* resource = reinterpret_cast<ID3D12Resource*>(static_cast<uintptr_t>(texturePtr));
    texResInfo.fResource.retain(resource);
    texResInfo.fResourceState = static_cast<D3D12_RESOURCE_STATES>(resourceState);
    texResInfo.fFormat = static_cast<DXGI_FORMAT>(format);
    texResInfo.fSampleCount = static_cast<uint32_t>(sampleCnt);
    texResInfo.fLevelCount = static_cast<uint32_t>(levelCnt);
    GrBackendTexture* instance = new GrBackendTexture(
        GrBackendTextures::MakeD3D(width, height, texResInfo)
    );
    return reinterpret_cast<jlong>(instance);
#else
    return 0;
#endif
}
