#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include <emscripten/bind.h>

using namespace emscripten;

extern "C" void* init_surface() {
   return nullptr;
}

bool ping() {
    return true;
}

EMSCRIPTEN_BINDINGS(Skiko) {
    function("ping", &ping);
};

