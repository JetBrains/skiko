#include <string>
#include <jni.h>
#include "interop.hh"
#include "SkString.h"

static void deleteU16String(std::vector<jchar>* instance) {
    delete instance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_U16StringExternalKt_U16String_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteU16String));
}