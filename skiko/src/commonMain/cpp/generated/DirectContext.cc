
// This file has been auto generated.

#include "GrDirectContext.h"
#include "include/gpu/gl/GrGLInterface.h"
#include "common.h"

#ifdef SK_METAL
#include "mtl/GrMtlBackendContext.h"
#endif

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeGL
  () {
    return reinterpret_cast<KNativePointer>(GrDirectContext::MakeGL().release());
}

#ifdef SK_METAL
SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeMetal
  (long devicePtr, long queuePtr) {
    GrMtlBackendContext backendContext = {};
    GrMTLHandle device = reinterpret_cast<GrMTLHandle>((devicePtr));
    GrMTLHandle queue = reinterpret_cast<GrMTLHandle>((queuePtr));
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContext::MakeMetal(backendContext);
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeDirect3D
  (KNativePointer adapterPtr, KNativePointer devicePtr, KNativePointer queuePtr) {
    TODO("implement org_jetbrains_skia_DirectContext__1nMakeDirect3D");
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nFlush
  (KNativePointer ptr) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->flush(GrFlushInfo());
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nSubmit
  (KNativePointer ptr, KBoolean syncCpu) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->submit(syncCpu);
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nReset
  (KNativePointer ptr, KInt flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->resetContext((uint32_t) flags);
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nAbandon
  (KNativePointer ptr, KInt flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->abandonContext();
}

