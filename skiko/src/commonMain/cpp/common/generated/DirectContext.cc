
// This file has been auto generated.

#include <iostream>
#include "GrDirectContext.h"
#include "mtl/GrMtlBackendContext.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeGL
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>(GrDirectContext::MakeGL().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeMetal
  (KInteropPointer __Kinstance, long devicePtr, long queuePtr) {
    GrMtlBackendContext backendContext = {};
    GrMTLHandle device = reinterpret_cast<GrMTLHandle>((devicePtr));
    GrMTLHandle queue = reinterpret_cast<GrMTLHandle>((queuePtr));
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContext::MakeMetal(backendContext);
    return reinterpret_cast<KNativePointer>(instance.release());
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeDirect3D
  (KInteropPointer __Kinstance, KNativePointer adapterPtr, KNativePointer devicePtr, KNativePointer queuePtr) {
    TODO("implement org_jetbrains_skia_DirectContext__1nMakeDirect3D");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeDirect3D
  (KInteropPointer __Kinstance, KNativePointer adapterPtr, KNativePointer devicePtr, KNativePointer queuePtr) {
    GrD3DBackendContext backendContext = {};
    IDXGIAdapter1* adapter = reinterpret_cast<IDXGIAdapter1*>((adapterPtr));
    ID3D12Device* device = reinterpret_cast<ID3D12Device*>((devicePtr));
    ID3D12CommandQueue* queue = reinterpret_cast<ID3D12CommandQueue*>((queuePtr));
    backendContext.fAdapter.retain(adapter);
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContext::MakeDirect3D(backendContext);
    return reinterpret_cast<KNativePointer>(instance.release());
}
#endif


SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nFlush
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->flush(GrFlushInfo());
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nSubmit
  (KInteropPointer __Kinstance, KNativePointer ptr, KBoolean syncCpu) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->submit(syncCpu);
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nReset
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->resetContext((uint32_t) flags);
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nAbandon
  (KInteropPointer __Kinstance, KNativePointer ptr, KInt flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->abandonContext();
}
