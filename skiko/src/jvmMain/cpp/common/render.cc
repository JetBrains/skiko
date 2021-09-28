#include <jni.h>

#if SK_BUILD_FOR_LINUX
#include <stdint.h>
#endif

#include "jni_helpers.h"
#include "GrBackendSurface.h"
#include "GrDirectContext.h"

extern "C" {

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeGLRenderTargetNative(
    JNIEnv * env, jclass jclass, 
    jint width, jint height, 
    jint sampleCnt, jint stencilBits, 
    jint fbId, jint fbFormat
) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget* obj = new GrBackendRenderTarget(width, height, sampleCnt, stencilBits, glInfo);
    return reinterpret_cast<jlong>(obj);
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeGLContextNative(JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(GrDirectContext::MakeGL().release());
}

extern void getMetalDeviceAndQueue(void** device, void** queue);

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeMetalRenderTargetNative(
    JNIEnv * env, jclass jclass, jint width, jint height, jint sampleCnt) {
#ifdef SK_METAL
    // TODO: create properly.
    GrMtlTextureInfo mtlInfo;
    GrBackendRenderTarget* obj = new GrBackendRenderTarget(width, height, sampleCnt, mtlInfo);
    return reinterpret_cast<jlong>(obj);
#else
    return 0;
#endif
}

JNIEXPORT jlong JNICALL Java_org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative(JNIEnv* env, jclass jclass) {
#ifdef SK_METAL
    void* device = nullptr;
    void* queue = nullptr;
    getMetalDeviceAndQueue(&device, &queue);
    return reinterpret_cast<jlong>(GrDirectContext::MakeMetal(device, queue).release());
#else
    return 0;
#endif
}

}  // extern "C"