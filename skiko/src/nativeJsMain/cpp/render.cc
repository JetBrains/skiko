#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_RenderTargetsKt_makeGLRenderTargetNative
    (KInt width, KInt height, KInt sampleCnt, KInt stencilBits, KInt fbId, KInt fbFormat) {
    GrGLFramebufferInfo glInfo = { static_cast<unsigned int>(fbId), static_cast<unsigned int>(fbFormat) };
    GrBackendRenderTarget* obj = new GrBackendRenderTarget(width, height, sampleCnt, stencilBits, glInfo);
    return reinterpret_cast<KNativePointer>(obj);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_RenderTargetsKt_makeGLContextNative() {
    return reinterpret_cast<KNativePointer>(GrDirectContext::MakeGL().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skiko_RenderTargetsKt_makeMetalRenderTargetNative
    (KInt width, KInt height, KInt sampleCnt) {
#ifdef SK_METAL
    // TODO: create properly.
    GrMtlTextureInfo mtlInfo;
    GrBackendRenderTarget* obj = new GrBackendRenderTarget(width, height, sampleCnt, mtlInfo);
    return reinterpret_cast<KNativePointer>(obj);
#else
    return 0;
#endif
}


SKIKO_EXPORT KNativePointer org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative
    () {
    TODO("implement org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative");
}

#if 0
SKIKO_EXPORT KNativePointer org_jetbrains_skiko_RenderTargetsKt_makeMetalContextNative
    () {
#ifdef SK_METAL
    void* device = nullptr;
    void* queue = nullptr;
    getMetalDeviceAndQueue(&device, &queue);
    return reinterpret_cast<KNativePointer>(GrDirectContext::MakeMetal(device, queue).release());
#else
    return 0;
#endif
}
#endif

