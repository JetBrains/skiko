#include <iostream>
#include <stdint.h>
#include "common.h"
#include "gpu/ganesh/gl/GrGLBackendSurface.h"
#include "include/gpu/ganesh/SkImageGanesh.h"
#include "include/gpu/ganesh/gl/GrGLTypes.h"
#include "gpu/ganesh/GrBackendSurface.h"

static void deleteBackendTexture(GrBackendTexture* rt) {
    delete rt;
}
SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendTexture__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteBackendTexture);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_BackendTexture__1nMakeGL
  (KInt textureId, KInt target, KInt width, KInt height, KInt format, KInt surfaceOrigin, KInt colorType, KBoolean isMipmapped) {
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