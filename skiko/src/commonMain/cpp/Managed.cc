#include "common.h"

typedef void (*finalizer_t)(void*);

SKIKO_EXPORT void org_jetbrains_skia_Managed__invokeFinalizer(KNativePointer finalizer, KNativePointer obj) {
    finalizer_t finalizer_f = reinterpret_cast<finalizer_t>(finalizer);
    finalizer_f(obj);
}