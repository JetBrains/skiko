#include "common.h"
#include <emscripten.h>

typedef void (*finalizer_t)(void*);

EMSCRIPTEN_KEEPALIVE
extern "C" void org_jetbrains_skia_ColorSpace__nInvokeFinalizer(KPointer finalizer, KPointer obj) {
    finalizer_t finalizer_f = reinterpret_cast<finalizer_t>(finalizer);
    finalizer_f(obj);
}