
// This file has been auto generated.

#include <string>
#include "SkString.h"
#include "common.h"

static void deleteU16String(std::vector<jchar>* instance) {
    delete instance;
}

extern "C" jlong org_jetbrains_skia_U16String__1nGetFinalizer
  (kref __Kinstance) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteU16String));
}


extern "C" jlong org_jetbrains_skia_U16String__1nMake
  (kref __Kinstance, jstring str) {
    TODO("implement org_jetbrains_skia_U16String__1nMake");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_U16String__1nMake
  (kref __Kinstance, jstring str) {
    jsize len = env->GetStringLength(str);
    std::vector<jchar>* instance = new std::vector<jchar>(len);
    env->GetStringRegion(str, 0, len, instance->data());
    return reinterpret_cast<jlong>(instance);
}
#endif



extern "C" jobject org_jetbrains_skia_U16String__1nToString
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_U16String__1nToString");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_U16String__1nToString
  (kref __Kinstance, jlong ptr) {
    std::vector<jchar>* instance = reinterpret_cast<std::vector<jchar>*>(static_cast<uintptr_t>(ptr));
    return env->NewString(instance->data(), instance->size());
}
#endif

