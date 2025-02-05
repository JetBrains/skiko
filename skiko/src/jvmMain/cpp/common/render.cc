#include <jni.h>
#include "ganesh/gl/GrGLDirectContext.h" // TODO: skia update: check if it's correct
#include "ganesh/gl/GrGLBackendSurface.h" // TODO: skia update: check if it's correct

#if SK_BUILD_FOR_LINUX
#include <stdint.h>
#endif

#include "jni_helpers.h"
#include "ganesh/GrBackendSurface.h"
#include "ganesh/GrDirectContext.h"

#ifdef SK_METAL
#include "ganesh/mtl/GrMtlDirectContext.h"
#include "ganesh/mtl/GrMtlBackendSurface.h"
#include "ganesh/mtl/GrMtlBackendContext.h"
#endif

extern "C" {

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeGLRenderTargetNative(
    JNIEnv * env, jclass jclass, 
    jint width, jint height, 
    jint sampleCnt, jint stencilBits, 
    jint fbId, jint fbFormat
) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget obj = GrBackendRenderTargets::MakeGL(width, height, sampleCnt, stencilBits, glInfo);
    GrBackendRenderTarget* target = new GrBackendRenderTarget(obj);
    return reinterpret_cast<jlong>(target);
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeGLContextNative(JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(GrDirectContexts::MakeGL().release());
}

extern void getMetalDeviceAndQueue(void** device, void** queue);

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeMetalRenderTargetNative(
    JNIEnv * env, jclass jclass, jint width, jint height, jint sampleCnt) {
#ifdef SK_METAL
    // TODO: create properly.
    GrMtlTextureInfo mtlInfo;
    GrBackendRenderTarget obj = GrBackendRenderTargets::MakeMtl(width, height, mtlInfo);
    GrBackendRenderTarget* instance = new GrBackendRenderTarget(obj);
    return reinterpret_cast<jlong>(instance);
#else
    return 0;
#endif
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative(JNIEnv* env, jclass jclass) {
#ifdef SK_METAL
    void* device = nullptr;
    void* queue = nullptr;
    getMetalDeviceAndQueue(&device, &queue);

    GrMtlBackendContext backendContext = {};
    GrMTLHandle deviceHandle = reinterpret_cast<GrMTLHandle>(device);
    GrMTLHandle queueHandle = reinterpret_cast<GrMTLHandle>(queue);
    backendContext.fDevice.retain(deviceHandle);
    backendContext.fQueue.retain(queueHandle);
    return reinterpret_cast<jlong>(GrDirectContexts::MakeMetal(backendContext).release());
#else
    return 0;
#endif
}

}  // extern "C"
