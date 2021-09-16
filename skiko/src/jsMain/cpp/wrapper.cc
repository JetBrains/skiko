#include "GrBackendSurface.h"
#include "GrDirectContext.h"
#include <emscripten/bind.h>
#include <emscripten.h>
#include <emscripten/html5.h>
#include "include/gpu/GrDirectContext.h"
#include "include/core/SkSurface.h"
#include "gpu/gl/GrGLInterface.h"
#include "gpu/gl/GrGLTypes.h"
#include "src/gpu/gl/GrGLDefines.h"
#include <GLES3/gl3.h>
#include "webgl/webgl1.h"

using namespace emscripten;

extern "C" void* init_surface() {
   return nullptr;
}

bool ping() {
    return true;
}


// Set the pixel format based on the colortype.
// These degrees of freedom are removed from canvaskit only to keep the interface simpler.
struct ColorSettings {
    ColorSettings(sk_sp<SkColorSpace> colorSpace) {
        if (colorSpace == nullptr || colorSpace->isSRGB()) {
            colorType = kRGBA_8888_SkColorType;
            pixFormat = GR_GL_RGBA8;
        } else {
            colorType = kRGBA_F16_SkColorType;
            pixFormat = GR_GL_RGBA16F;
        }
    };
    SkColorType colorType;
    GrGLenum pixFormat;
};

sk_sp<SkSurface> MakeOnScreenGLSurface(sk_sp<GrDirectContext> dContext, int width, int height,
                                       sk_sp<SkColorSpace> colorSpace) {
    // WebGL should already be clearing the color and stencil buffers, but do it again here to
    // ensure Skia receives them in the expected state.
    emscripten_glBindFramebuffer(GL_FRAMEBUFFER, 0);
    emscripten_glClearColor(0, 0, 0, 0);
    emscripten_glClearStencil(0);
    emscripten_glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    dContext->resetContext(kRenderTarget_GrGLBackendState | kMisc_GrGLBackendState);

    // The on-screen canvas is FBO 0. Wrap it in a Skia render target so Skia can render to it.
    GrGLFramebufferInfo info;
    info.fFBOID = 0;

    GrGLint sampleCnt;
    emscripten_glGetIntegerv(GL_SAMPLES, &sampleCnt);

    GrGLint stencil;
    emscripten_glGetIntegerv(GL_STENCIL_BITS, &stencil);

    const auto colorSettings = ColorSettings(colorSpace);
    info.fFormat = colorSettings.pixFormat;
    GrBackendRenderTarget target(width, height, sampleCnt, stencil, info);
    sk_sp<SkSurface> surface(SkSurface::MakeFromBackendRenderTarget(dContext.get(), target,
        kBottomLeft_GrSurfaceOrigin, colorSettings.colorType, colorSpace, nullptr));
    return surface;
}

EMSCRIPTEN_WEBGL_CONTEXT_HANDLE createContext(std::string id) {
    EmscriptenWebGLContextAttributes attr;
    emscripten_webgl_init_context_attributes(&attr);
    attr.majorVersion = 2;
    EMSCRIPTEN_WEBGL_CONTEXT_HANDLE ctx = emscripten_webgl_create_context(id.c_str(), &attr);
    assert(ctx && "Failed to create WebGL2 context");
    emscripten_webgl_make_context_current(ctx);
    return ctx;
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
    function("createContext", &createContext);
    function("MakeOnScreenGLSurface", &MakeOnScreenGLSurface);
};

