
// This file has been auto generated.

#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "common.h"

extern "C" jlong org_jetbrains_skiko_RenderTargetsKt_makeGLRenderTargetNative
    (jint width, jint height, jint sampleCnt, jint stencilBits, jint fbId, jint fbFormat) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget* obj = new GrBackendRenderTarget(width, height, sampleCnt, stencilBits, glInfo);
    return reinterpret_cast<jlong>(obj);
}

extern "C" jlong org_jetbrains_skiko_RenderTargetsKt_makeGLContextNative() {
    return reinterpret_cast<jlong>(GrDirectContext::MakeGL().release());
}

extern "C" jlong org_jetbrains_skiko_RenderTargetsKt_makeMetalRenderTargetNative
    (jint width, jint height, jint sampleCnt) {
#ifdef SK_METAL
    // TODO: create properly.
    GrMtlTextureInfo mtlInfo;
    GrBackendRenderTarget* obj = new GrBackendRenderTarget(width, height, sampleCnt, mtlInfo);
    return reinterpret_cast<jlong>(obj);
#else
    return 0;
#endif
}


extern "C" jlong org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative
    () {
    TODO("implement org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative");
}
     
#if 0 
extern "C" jlong org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative
    () {
#ifdef SK_METAL
    void* device = nullptr;
    void* queue = nullptr;
    getMetalDeviceAndQueue(&device, &queue);
    return reinterpret_cast<jlong>(GrDirectContext::MakeMetal(device, queue).release());
#else
    return 0;
#endif
}
#endif

