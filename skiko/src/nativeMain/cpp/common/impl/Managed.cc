#include <iostream>
#include "common.h"

typedef void (*FreeFunction)(void*);

extern "C" void org_jetbrains_skia_impl_Managed__1nInvokeFinalizer
  (KNativePointer finalizerPtr, KNativePointer ptr) {
    FreeFunction finalizer = reinterpret_cast<FreeFunction>(finalizerPtr);
    finalizer(ptr);
}
