#include "common.h"
#include "ganesh/gl/GrGLAssembleInterface.h"
#include "ganesh/gl/GrGLInterface.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_GLAssembledInterface__1nCreateFromNativePointers
  (KNativePointer ctxPtr, KNativePointer functionPtr) {
    void* context = reinterpret_cast<void*>(ctxPtr);
    GrGLGetProc getProc = reinterpret_cast<GrGLGetProc>(functionPtr);
    sk_sp<const GrGLInterface> interface = GrGLMakeAssembledInterface(context, getProc);
    return reinterpret_cast<KNativePointer>(const_cast<GrGLInterface*>(interface.release()));
}
