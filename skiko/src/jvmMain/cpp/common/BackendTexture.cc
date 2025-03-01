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


static void deleteBackendTexture(GrBackendTexture* rt) {
    // std::cout << "Deleting [GrBackendTexture " << rt << "]" << std::endl;
    delete rt;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BackendTextureKt_BackendTexture_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBackendTexture));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BackendTextureKt__1nMakeGL
  (JNIEnv* env, jclass jclass, jint textureId, jint target, jint format, jint width, jint height, jboolean isMipmapped) {
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