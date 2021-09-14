#include <iostream>
#include <jni.h>
#include "jni_helpers.h"

typedef void (*FreeFunction)(void*);

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_impl_Managed__1nInvokeFinalizer
  (JNIEnv* env, jclass jclass, jlong finalizerPtr, jlong ptr) {
    try {
      void* instance = reinterpret_cast<void*>(static_cast<uintptr_t>(ptr));
      FreeFunction finalizer = reinterpret_cast<FreeFunction>(static_cast<uintptr_t>(finalizerPtr));
      finalizer(instance);
    } catch(...) {
      logJavaException(env, handleException(__FUNCTION__));
    }
}