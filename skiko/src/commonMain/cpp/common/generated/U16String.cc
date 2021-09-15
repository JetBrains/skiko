
// This file has been auto generated.

#include <string>
#include "SkString.h"
#include "common.h"

static void deleteU16String(std::vector<jchar>* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_U16String__1nGetFinalizer
  (KInteropPointer __Kinstance) {
    return reinterpret_cast<KNativePointer>((&deleteU16String));
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_U16String__1nMake
  (KInteropPointer __Kinstance, KInteropPointer str) {
    TODO("implement org_jetbrains_skia_U16String__1nMake");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_U16String__1nMake
  (KInteropPointer __Kinstance, KInteropPointer str) {
    jsize len = env->GetStringLength(str);
    std::vector<jchar>* instance = new std::vector<jchar>(len);
    env->GetStringRegion(str, 0, len, instance->data());
    return reinterpret_cast<KNativePointer>(instance);
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_U16String__1nToString
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_U16String__1nToString");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_U16String__1nToString
  (KInteropPointer __Kinstance, KNativePointer ptr) {
    std::vector<jchar>* instance = reinterpret_cast<std::vector<jchar>*>((ptr));
    return env->NewString(instance->data(), instance->size());
}
#endif

