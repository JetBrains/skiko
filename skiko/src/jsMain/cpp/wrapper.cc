#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include <emscripten/bind.h>
#include <emscripten.h>
#include <emscripten/html5.h>
#include "include/gpu/GrDirectContext.h"
#include "include/gpu/gl/GrGLInterface.h"

using namespace emscripten;

extern "C" void* init_surface() {
   return nullptr;
}

bool ping() {
    return true;
}

/**
 * Sets the given WebGL context to be "current" and then creates a GrDirectContext from that
 * context.
 */
static sk_sp<GrDirectContext> MakeGrContext(EMSCRIPTEN_WEBGL_CONTEXT_HANDLE context)
{
    EMSCRIPTEN_RESULT r = emscripten_webgl_make_context_current(context);
    if (r < 0) {
        printf("failed to make webgl context current %d\n", r);
        return nullptr;
    }
    // setup GrDirectContext
    auto interface = GrGLMakeNativeInterface();
    // setup contexts
    sk_sp<GrDirectContext> dContext(GrDirectContext::MakeGL(interface));
    return dContext;
}


EMSCRIPTEN_BINDINGS(Skiko) {
    function("ping", &ping);
    function("MakeGrContext", &MakeGrContext);
};

