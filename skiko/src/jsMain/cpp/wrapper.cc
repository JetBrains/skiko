#include <common.h>

#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include <emscripten/bind.h>

using namespace emscripten;

extern "C" void* init_surface() {
   return nullptr;
}

EMSCRIPTEN_BINDINGS(Skiko) {
    function("org_jetbrains_skia_Canvas__1nDrawPoint", &org_jetbrains_skia_Canvas__1nDrawPoint);
};

