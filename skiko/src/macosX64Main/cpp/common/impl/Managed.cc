#include <iostream>
#include "common.h"

typedef void (*FreeFunction)(void*);

extern "C" void org_jetbrains_skia_impl_Managed__1nInvokeFinalizer
  (jlong finalizerPtr, jlong ptr) {
    void* instance = reinterpret_cast<void*>(static_cast<uintptr_t>(ptr));
    FreeFunction finalizer = reinterpret_cast<FreeFunction>(static_cast<uintptr_t>(finalizerPtr));
    finalizer(instance);
}
