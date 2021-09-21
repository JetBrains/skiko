#include <emscripten.h>
#include <emscripten/html5.h>
#include "webgl/webgl1.h"

// We need to have any definition invoking GL-lib on a backend size - otherwise GL is not created on frontend (regardless of -l=GL flag)
EMSCRIPTEN_WEBGL_CONTEXT_HANDLE createContext(char* id) {
    EmscriptenWebGLContextAttributes attr;
    emscripten_webgl_init_context_attributes(&attr);
    attr.majorVersion = 2;
    EMSCRIPTEN_WEBGL_CONTEXT_HANDLE ctx = emscripten_webgl_create_context(id, &attr);
    emscripten_webgl_make_context_current(ctx);
    return ctx;
}