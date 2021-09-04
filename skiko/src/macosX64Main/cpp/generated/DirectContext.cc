
// This file has been auto generated.

#include <iostream>
#include "GrDirectContext.h"
#include "mtl/GrMtlBackendContext.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_DirectContext__1nMakeGL
  (kref __Kinstance) {
    return reinterpret_cast<jlong>(GrDirectContext::MakeGL().release());
}

extern "C" jlong org_jetbrains_skia_DirectContext__1nMakeMetal
  (kref __Kinstance, long devicePtr, long queuePtr) {
    GrMtlBackendContext backendContext = {};
    GrMTLHandle device = reinterpret_cast<GrMTLHandle>(static_cast<uintptr_t>(devicePtr));
    GrMTLHandle queue = reinterpret_cast<GrMTLHandle>(static_cast<uintptr_t>(queuePtr));
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContext::MakeMetal(backendContext);
    return reinterpret_cast<jlong>(instance.release());
}


extern "C" jlong org_jetbrains_skia_DirectContext__1nMakeDirect3D
  (kref __Kinstance, jlong adapterPtr, jlong devicePtr, jlong queuePtr) {
    throw std::runtime_error("TODO: implement org_jetbrains_skia_DirectContext__1nMakeDirect3D");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_DirectContext__1nMakeDirect3D
  (kref __Kinstance, jlong adapterPtr, jlong devicePtr, jlong queuePtr) {
    GrD3DBackendContext backendContext = {};
    IDXGIAdapter1* adapter = reinterpret_cast<IDXGIAdapter1*>(static_cast<uintptr_t>(adapterPtr));
    ID3D12Device* device = reinterpret_cast<ID3D12Device*>(static_cast<uintptr_t>(devicePtr));
    ID3D12CommandQueue* queue = reinterpret_cast<ID3D12CommandQueue*>(static_cast<uintptr_t>(queuePtr));
    backendContext.fAdapter.retain(adapter);
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContext::MakeDirect3D(backendContext);
    return reinterpret_cast<jlong>(instance.release());
}
#endif


extern "C" void org_jetbrains_skia_DirectContext__1nFlush
  (kref __Kinstance, jlong ptr) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->flush(GrFlushInfo());
}

extern "C" void org_jetbrains_skia_DirectContext__1nSubmit
  (kref __Kinstance, jlong ptr, jboolean syncCpu) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->submit(syncCpu);
}

extern "C" void org_jetbrains_skia_DirectContext__1nReset
  (kref __Kinstance, jlong ptr, jint flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->resetContext((uint32_t) flags);
}

extern "C" void org_jetbrains_skia_DirectContext__1nAbandon
  (kref __Kinstance, jlong ptr, jint flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->abandonContext();
}
