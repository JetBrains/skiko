#include <string>
#include <jni.h>
#include "interop.hh"
#include "SkString.h"

static void deleteU16String(std::vector<jchar>* instance) {
    delete instance;
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_U16StringKt_U16String_1nGetFinalizer
  (JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteU16String));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_U16StringKt_U16String_1nMake
  (JNIEnv* env, jclass jclass, jcharArray chars, jint len) {
    std::vector<jchar>* instance = new std::vector<jchar>(len);
    env->GetCharArrayRegion(chars, 0, len, instance->data());
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jobject JNICALL Java_org_jetbrains_skia_U16StringKt_U16String_1nToString
  (JNIEnv* env, jclass jclass, jlong ptr) {
    std::vector<jchar>* instance = reinterpret_cast<std::vector<jchar>*>(static_cast<uintptr_t>(ptr));
    return env->NewString(instance->data(), instance->size());
}
