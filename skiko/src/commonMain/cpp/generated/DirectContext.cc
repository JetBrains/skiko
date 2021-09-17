
// This file has been auto generated.

//#ifndef SKIKO_WASM

#include <iostream>
#include "GrDirectContext.h"
#include "include/gpu/gl/GrGLInterface.h"
//#include "mtl/GrMtlBackendContext.h"
#include "common.h"

using namespace std;

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeGL
  () {
    return reinterpret_cast<KNativePointer>(GrDirectContext::MakeGL().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeGL2()
{
    // We assume that any calls we make to GL for the remainder of this function will go to the
    // desired WebGL Context.
    // setup interface.
    auto interface = GrGLMakeNativeInterface();
    std::cout << "org_jetbrains_skia_DirectContext__1nMakeGL2 " << interface << endl;
    // setup context
    return reinterpret_cast<KNativePointer>(GrDirectContext::MakeGL(interface).release());
}

//SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeMetal
//  (long devicePtr, long queuePtr) {
//    GrMtlBackendContext backendContext = {};
//    GrMTLHandle device = reinterpret_cast<GrMTLHandle>((devicePtr));
//    GrMTLHandle queue = reinterpret_cast<GrMTLHandle>((queuePtr));
//    backendContext.fDevice.retain(device);
//    backendContext.fQueue.retain(queue);
//    sk_sp<GrDirectContext> instance = GrDirectContext::MakeMetal(backendContext);
//    return reinterpret_cast<KNativePointer>(instance.release());
//}


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

//#endif
