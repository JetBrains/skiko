
// This file has been auto generated.

#include <string>
#include "SkString.h"
#include "common.h"

static void deleteU16String(std::vector<KChar>* instance) {
    delete instance;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_U16String__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteU16String);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_U16String__1nMake
  (KChar* chars, KInt len) {
    std::vector<KChar>* instance = new std::vector<KChar>(len);
    memcpy(instance->data(), chars, len);
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KInteropPointer org_jetbrains_skia_U16String__1nToString
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_U16String__1nToString");
}
 