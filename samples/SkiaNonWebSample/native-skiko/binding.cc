#include <node_api.h>

#define GL_SILENCE_DEPRECATION
#include <OpenGL/OpenGL.h>
#include <OpenGL/gl3.h>
#include <OpenGL/gl3ext.h>

#include <cstdio>
#include <cstdint>
#include <cstring>
#include <string>
#include <unordered_map>
#include <vector>
#include <algorithm>
#include "include/core/SkCanvas.h"
#include "include/core/SkColor.h"
#include "include/core/SkPaint.h"
#include "include/core/SkRect.h"
#include "include/core/SkSurface.h"
#include "include/core/SkColorSpace.h"

#include "include/gpu/ganesh/GrBackendSurface.h"
#include "include/gpu/ganesh/GrDirectContext.h"
#include "include/gpu/ganesh/SkSurfaceGanesh.h"
#include "include/gpu/ganesh/gl/GrGLInterface.h"
#include "include/gpu/ganesh/gl/GrGLDirectContext.h"
#include "include/gpu/ganesh/gl/GrGLBackendSurface.h"
#include "include/gpu/ganesh/gl/mac/GrGLMakeMacInterface.h"

namespace {

napi_value Undefined(napi_env env);
uint32_t U32(napi_env env, napi_value value);
napi_value ToU32(napi_env env, uint32_t value);

struct RenderTarget {
    CGLContextObj context = nullptr;
    GLuint framebuffer = 0;
    GLuint color = 0;
    GLuint depthStencil = 0;
    GLsizei width = 0;
    GLsizei height = 0;
};

// CGLContextObj nativeContext = nullptr;
uint32_t nextContextId = 1;
uint32_t currentContextId = 0;
std::unordered_map<uint32_t, RenderTarget> renderTargets;

void Throw(napi_env env, const char* message) {
    napi_throw_error(env, nullptr, message);
}

CGLContextObj CreateNativeContext(napi_env env) {
//     if (nativeContext != nullptr) return true;

    CGLPixelFormatAttribute attributes[] = {
        kCGLPFAOpenGLProfile,
        static_cast<CGLPixelFormatAttribute>(kCGLOGLPVersion_3_2_Core),
        kCGLPFAAccelerated,
        kCGLPFAColorSize,
        static_cast<CGLPixelFormatAttribute>(24),
        kCGLPFAAlphaSize,
        static_cast<CGLPixelFormatAttribute>(8),
        kCGLPFADepthSize,
        static_cast<CGLPixelFormatAttribute>(24),
        kCGLPFAStencilSize,
        static_cast<CGLPixelFormatAttribute>(8),
        static_cast<CGLPixelFormatAttribute>(0),
    };

    CGLPixelFormatObj pixelFormat = nullptr;
    GLint count = 0;
    if (CGLChoosePixelFormat(attributes, &pixelFormat, &count) != kCGLNoError ||
        pixelFormat == nullptr) {
        Throw(env, "CGLChoosePixelFormat failed");
        return nullptr;
    }

    CGLContextObj context = nullptr;
    const CGLError result = CGLCreateContext(pixelFormat, nullptr, &context);
    CGLDestroyPixelFormat(pixelFormat);
    if (result != kCGLNoError || context == nullptr) {
        Throw(env, "CGLCreateContext failed");
        return nullptr;
    }

//     if (CGLSetCurrentContext(nativeContext) != kCGLNoError) {
//         Throw(env, "CGLSetCurrentContext failed");
//         return false;
//     }
//     std::fprintf(
//         stderr,
//         "[native-skiko] GL_VERSION=%s, GLSL_VERSION=%s\n",
//         reinterpret_cast<const char*>(glGetString(GL_VERSION)),
//         reinterpret_cast<const char*>(glGetString(GL_SHADING_LANGUAGE_VERSION))
//     );
    return context;
}

GLuint ResolveFramebuffer(GLuint framebuffer) {
    if (framebuffer != 0 || currentContextId == 0) {
        return framebuffer;
    }

    const auto found = renderTargets.find(currentContextId);
    if (found == renderTargets.end()) {
        return framebuffer;
    }

    return found->second.framebuffer;
}

int32_t ObjectI32(napi_env env, napi_value object, const char* name) {
    napi_value value;
    napi_get_named_property(env, object, name, &value);
    int32_t result = 0;
    napi_get_value_int32(env, value, &result);
    return result;
}

bool ActivateRenderTarget(napi_env env, uint32_t contextId) {
    const auto found = renderTargets.find(contextId);
    if (found == renderTargets.end()) {
        Throw(env, "Unknown native GL context id");
        return false;
    }

    const RenderTarget& target = found->second;

    if (CGLSetCurrentContext(target.context) != kCGLNoError) {
        Throw(env, "CGLSetCurrentContext failed");
        return false;
    }

    currentContextId = contextId;

    glBindFramebuffer(GL_FRAMEBUFFER, target.framebuffer);
    glViewport(0, 0, target.width, target.height);

    return true;
}

bool DrawNativeSkiaSmokeFrame(napi_env env, uint32_t contextId) {
fprintf(stderr, "[smoke] 1 activate\n");
    if (!ActivateRenderTarget(env, contextId)) {
        return false;
    }
    const auto found = renderTargets.find(contextId);
    if (found == renderTargets.end()) {
        Throw(env, "Unknown native GL context id");
        return false;
    }

    const RenderTarget& target = found->second;
   CGLContextObj currentContext = CGLGetCurrentContext();

   fprintf(
       stderr,
       "[smoke] target CGL=%p current CGL=%p\n",
       target.context,
       currentContext
   );

   if (currentContext == nullptr ||
       currentContext != target.context) {
       if (CGLSetCurrentContext(target.context) != kCGLNoError) {
           Throw(env, "Could not activate CGL context for native Skia");
           return false;
       }
   }

fprintf(stderr, "[smoke] 2 make interface\n");
//     sk_sp<const GrGLInterface> interface =
//         GrGLInterfaces::MakeMac();
    sk_sp<const GrGLInterface> interface = GrGLInterfaces::MakeMac();

fprintf(stderr, "[smoke] 3 made interface\n");
fprintf(stderr, "[smoke] 3 interface=%p\n", interface.get());
    if (!interface) {
        Throw(env, "GrGLInterfaces::MakeMac failed");
        return false;
    }

//     std::unique_ptr<GrDirectContext> directContext =
//         GrDirectContexts::MakeGL(interface);
    sk_sp<GrDirectContext> directContext = GrDirectContexts::MakeGL(interface);

    if (!directContext) {
        Throw(env, "Native GrDirectContext creation failed");
        return false;
    }

    GrGLFramebufferInfo framebufferInfo;
    framebufferInfo.fFBOID = target.framebuffer;
    framebufferInfo.fFormat = GL_RGBA8;

GrBackendRenderTarget backendTarget =
    GrBackendRenderTargets::MakeGL(
        target.width,
        target.height,
        0,  // sample count
        8,  // stencil bits
        framebufferInfo
    );

    sk_sp<SkSurface> surface =
        SkSurfaces::WrapBackendRenderTarget(
            directContext.get(),
            backendTarget,
            kBottomLeft_GrSurfaceOrigin,
            kRGBA_8888_SkColorType,
            nullptr,
            nullptr
        );

    if (!surface) {
        Throw(env, "Native SkSurface creation failed");
        return false;
    }

    SkCanvas* canvas = surface->getCanvas();

    canvas->clear(SkColorSetRGB(245, 245, 245));
fprintf(stderr, "[smoke] 11 draw\n");
    SkPaint blue;
    blue.setColor(SkColorSetRGB(35, 105, 220));
    blue.setAntiAlias(true);

    canvas->drawRoundRect(
        SkRect::MakeXYWH(
            40.0f,
            40.0f,
            static_cast<float>(target.width) - 80.0f,
            static_cast<float>(target.height) - 80.0f
        ),
        24.0f,
        24.0f,
        blue
    );

    SkPaint yellow;
    yellow.setColor(SkColorSetRGB(255, 210, 40));
    yellow.setAntiAlias(true);

    canvas->drawCircle(
        target.width * 0.5f,
        target.height * 0.5f,
        std::min(target.width, target.height) * 0.18f,
        yellow
    );

    directContext->flushAndSubmit(surface.get());

    return true;
}

napi_value CreateContext(napi_env env, napi_callback_info info) {
    napi_value args[2];
    size_t argc = 2;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    if (argc < 1) return nullptr;

    RenderTarget target;
    target.context = CreateNativeContext(env);
    if (target.context == nullptr) return nullptr;

    if (CGLSetCurrentContext(target.context) != kCGLNoError) {
        CGLDestroyContext(target.context);
        Throw(env, "CGLSetCurrentContext failed");
        return nullptr;
    }
    target.width = ObjectI32(env, args[0], "width");
    target.height = ObjectI32(env, args[0], "height");

    glGenFramebuffers(1, &target.framebuffer);
    glBindFramebuffer(GL_FRAMEBUFFER, target.framebuffer);

    glGenRenderbuffers(1, &target.color);
    glBindRenderbuffer(GL_RENDERBUFFER, target.color);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, target.width, target.height);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                              GL_RENDERBUFFER, target.color);

    glGenRenderbuffers(1, &target.depthStencil);
    glBindRenderbuffer(GL_RENDERBUFFER, target.depthStencil);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8,
                          target.width, target.height);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
                              GL_RENDERBUFFER, target.depthStencil);

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
        glDeleteRenderbuffers(1, &target.depthStencil);
        glDeleteRenderbuffers(1, &target.color);
        glDeleteFramebuffers(1, &target.framebuffer);
        CGLDestroyContext(target.context);
        Throw(env, "Native OpenGL framebuffer is incomplete");
        return nullptr;
    }

    const uint32_t contextId = nextContextId++;
    renderTargets.emplace(contextId, target);
    currentContextId = contextId;
    return ToU32(env, contextId);
}

napi_value MakeContextCurrent(napi_env env, napi_callback_info info) {
    napi_value args[1];
    size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    const bool activated = argc == 1 && ActivateRenderTarget(env, U32(env, args[0]));
    napi_value result;
    napi_get_boolean(env, activated, &result);
    return result;
}

napi_value ReadContextPixels(napi_env env, napi_callback_info info) {
    napi_value args[1];
    size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    if (argc != 1) return nullptr;
    const uint32_t contextId = U32(env, args[0]);
    if (!ActivateRenderTarget(env, contextId)) return nullptr;
    const RenderTarget& target = renderTargets.at(contextId);

    napi_value buffer;
    void* bytes = nullptr;
    const size_t size = static_cast<size_t>(target.width) *
                        static_cast<size_t>(target.height) * 4;
    napi_create_buffer(env, size, &bytes, &buffer);
    glFinish();
    glPixelStorei(GL_PACK_ALIGNMENT, 1);
    if (!DrawNativeSkiaSmokeFrame(env, contextId)) {
        return nullptr;
    }
    glReadPixels(0, 0, target.width, target.height,
                 GL_RGBA, GL_UNSIGNED_BYTE, bytes);
    return buffer;
}

napi_value DestroyContext(napi_env env, napi_callback_info info) {
    napi_value args[1];
    size_t argc = 1;
    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 1) {
        return Undefined(env);
    }

    const uint32_t contextId = U32(env, args[0]);
    const auto found = renderTargets.find(contextId);

    if (found == renderTargets.end()) {
        return Undefined(env);
    }

    const RenderTarget target = found->second;

    CGLSetCurrentContext(target.context);

    glDeleteRenderbuffers(1, &target.depthStencil);
    glDeleteRenderbuffers(1, &target.color);
    glDeleteFramebuffers(1, &target.framebuffer);

    renderTargets.erase(found);

    if (currentContextId == contextId) {
        currentContextId = 0;
        CGLSetCurrentContext(nullptr);
    }

    CGLDestroyContext(target.context);

    return Undefined(env);
}

napi_ref wasmMemoryRef = nullptr;
napi_ref wasmMallocRef = nullptr;

uint8_t* wasmMemoryBase = nullptr;
size_t wasmMemorySize = 0;

std::unordered_map<std::string, uint32_t> wasmStrings;

napi_value Undefined(napi_env env) {
    napi_value value;
    napi_get_undefined(env, &value);
    return value;
}

uint32_t U32(napi_env env, napi_value value) {
    uint32_t result;
    napi_get_value_uint32(env, value, &result);
    return result;
}

int32_t I32(napi_env env, napi_value value) {
    int32_t result;
    napi_get_value_int32(env, value, &result);
    return result;
}

float F32(napi_env env, napi_value value) {
    double result;
    napi_get_value_double(env, value, &result);
    return static_cast<float>(result);
}

napi_value ToU32(napi_env env, uint32_t value) {
    napi_value result;
    napi_create_uint32(env, value, &result);
    return result;
}

napi_value ToI32(napi_env env, int32_t value) {
    napi_value result;
    napi_create_int32(env, value, &result);
    return result;
}

void RefreshWasmMemory(napi_env env) {
    napi_value memory;
    napi_get_reference_value(env, wasmMemoryRef, &memory);

    napi_value buffer;
    napi_get_named_property(env, memory, "buffer", &buffer);

    void* data;
    size_t size;
    napi_get_arraybuffer_info(env, buffer, &data, &size);

    wasmMemoryBase = static_cast<uint8_t*>(data);
    wasmMemorySize = size;
}

template <typename T>
T* WasmPointer(uint32_t offset) {
    if (offset == 0) {
        return nullptr;
    }

    return reinterpret_cast<T*>(wasmMemoryBase + offset);
}

/*
 * These GL arguments are buffer offsets, not pointers into WebAssembly.Memory.
 */
const void* BufferOffset(uint32_t offset) {
    return reinterpret_cast<const void*>(
        static_cast<uintptr_t>(offset)
    );
}

/*
 * A pixel argument is client memory unless a pixel-unpack buffer is bound.
 */
const void* PixelUnpackPointer(uint32_t offset) {
    if (offset == 0) {
        return nullptr;
    }

    GLint unpackBuffer = 0;
    glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, &unpackBuffer);

    if (unpackBuffer != 0) {
        return BufferOffset(offset);
    }

    return WasmPointer<const void>(offset);
}

uint32_t WasmAllocate(napi_env env, uint32_t size) {
    napi_value mallocFunction;
    napi_get_reference_value(env, wasmMallocRef, &mallocFunction);

    napi_value global;
    napi_get_global(env, &global);

    napi_value sizeArgument;
    napi_create_uint32(env, size, &sizeArgument);

    napi_value result;
    napi_call_function(
        env,
        global,
        mallocFunction,
        1,
        &sizeArgument,
        &result
    );

    uint32_t offset;
    napi_get_value_uint32(env, result, &offset);

    /*
     * malloc may have caused WebAssembly.Memory to grow.
     */
    RefreshWasmMemory(env);

    return offset;
}

uint32_t CopyStringToWasm(napi_env env, const GLubyte* value) {
    if (value == nullptr) {
        return 0;
    }

    const char* stringValue =
        reinterpret_cast<const char*>(value);

    auto existing = wasmStrings.find(stringValue);
    if (existing != wasmStrings.end()) {
        return existing->second;
    }

    const size_t length = std::strlen(stringValue);
    const uint32_t offset =
        WasmAllocate(env, static_cast<uint32_t>(length + 1));

    std::memcpy(
        wasmMemoryBase + offset,
        stringValue,
        length + 1
    );

    wasmStrings.emplace(stringValue, offset);
    return offset;
}

napi_value AttachWasmRuntime(
    napi_env env,
    napi_callback_info info
) {
    napi_value args[2];
    size_t argc = 2;
    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (wasmMemoryRef != nullptr) {
        napi_delete_reference(env, wasmMemoryRef);
    }

    if (wasmMallocRef != nullptr) {
        napi_delete_reference(env, wasmMallocRef);
    }

    /*
     * args[0] = WebAssembly.Memory
     * args[1] = exported wasm malloc function
     */
    napi_create_reference(env, args[0], 1, &wasmMemoryRef);
    napi_create_reference(env, args[1], 1, &wasmMallocRef);

    RefreshWasmMemory(env);
    wasmStrings.clear();

    return Undefined(env);
}

#define GL_VOID_BINDING(name, argumentCount, body)               \
    napi_value Host_##name(                                      \
        napi_env env,                                            \
        napi_callback_info info                                  \
    ) {                                                          \
        napi_value args[10];                                     \
        size_t argc = argumentCount;                             \
        napi_get_cb_info(                                        \
            env, info, &argc, args, nullptr, nullptr             \
        );                                                       \
        body                                                     \
        return Undefined(env);                                   \
    }

/* Context and capability queries */

napi_value Host_glGetString(
    napi_env env,
    napi_callback_info info
) {
    napi_value args[1];
    size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);

    const GLubyte* result = glGetString(
        static_cast<GLenum>(U32(env, args[0]))
    );

    return ToU32(env, CopyStringToWasm(env, result));
}

napi_value Host_glGetStringi(
    napi_env env,
    napi_callback_info info
) {
    napi_value args[2];
    size_t argc = 2;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);

    const GLubyte* result = glGetStringi(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1]))
    );

    return ToU32(env, CopyStringToWasm(env, result));
}

GL_VOID_BINDING(glGetIntegerv, 2, {
    const GLenum pname =
        static_cast<GLenum>(U32(env, args[0]));

    GLint* params =
        WasmPointer<GLint>(U32(env, args[1]));

    glGetIntegerv(pname, params);

    const bool framebufferBinding =
        pname == GL_FRAMEBUFFER_BINDING ||
        pname == GL_DRAW_FRAMEBUFFER_BINDING ||
        pname == GL_READ_FRAMEBUFFER_BINDING;

    if (framebufferBinding &&
        params != nullptr &&
        currentContextId != 0) {
        const auto found =
            renderTargets.find(currentContextId);

        if (found != renderTargets.end() &&
            static_cast<GLuint>(*params) ==
                found->second.framebuffer) {
            *params = 0;
        }
    }
})

GL_VOID_BINDING(glGetFloatv, 2, {
    glGetFloatv(
        static_cast<GLenum>(U32(env, args[0])),
        WasmPointer<GLfloat>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glGetShaderPrecisionFormat, 4, {
    glGetShaderPrecisionFormat(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        WasmPointer<GLint>(U32(env, args[2])),
        WasmPointer<GLint>(U32(env, args[3]))
    );
})

napi_value Host_glGetError(
    napi_env env,
    napi_callback_info info
) {
    size_t argc = 0;
    napi_get_cb_info(env, info, &argc, nullptr, nullptr, nullptr);
    return ToU32(env, glGetError());
}

/* Basic state */

GL_VOID_BINDING(glDisable, 1, {
    glDisable(static_cast<GLenum>(U32(env, args[0])));
})

GL_VOID_BINDING(glEnable, 1, {
    glEnable(static_cast<GLenum>(U32(env, args[0])));
})

GL_VOID_BINDING(glDepthMask, 1, {
    glDepthMask(static_cast<GLboolean>(U32(env, args[0])));
})

GL_VOID_BINDING(glFrontFace, 1, {
    glFrontFace(static_cast<GLenum>(U32(env, args[0])));
})

GL_VOID_BINDING(glLineWidth, 1, {
    glLineWidth(F32(env, args[0]));
})

GL_VOID_BINDING(glPixelStorei, 2, {
    glPixelStorei(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLint>(I32(env, args[1]))
    );
})

GL_VOID_BINDING(glViewport, 4, {
    glViewport(
        I32(env, args[0]),
        I32(env, args[1]),
        I32(env, args[2]),
        I32(env, args[3])
    );
})

GL_VOID_BINDING(glClearColor, 4, {
    glClearColor(
        F32(env, args[0]),
        F32(env, args[1]),
        F32(env, args[2]),
        F32(env, args[3])
    );
})

GL_VOID_BINDING(glColorMask, 4, {
    glColorMask(
        static_cast<GLboolean>(U32(env, args[0])),
        static_cast<GLboolean>(U32(env, args[1])),
        static_cast<GLboolean>(U32(env, args[2])),
        static_cast<GLboolean>(U32(env, args[3]))
    );
})

GL_VOID_BINDING(glClear, 1, {
    glClear(static_cast<GLbitfield>(U32(env, args[0])));
})

GL_VOID_BINDING(glBlendEquation, 1, {
    glBlendEquation(static_cast<GLenum>(U32(env, args[0])));
})

GL_VOID_BINDING(glBlendFunc, 2, {
    glBlendFunc(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1]))
    );
})

/* Buffers */

GL_VOID_BINDING(glGenBuffers, 2, {
    glGenBuffers(
        I32(env, args[0]),
        WasmPointer<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glBindBuffer, 2, {
    glBindBuffer(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glBufferData, 4, {
    glBufferData(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLsizeiptr>(I32(env, args[1])),
        WasmPointer<const void>(U32(env, args[2])),
        static_cast<GLenum>(U32(env, args[3]))
    );
})

GL_VOID_BINDING(glBufferSubData, 4, {
    glBufferSubData(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLintptr>(I32(env, args[1])),
        static_cast<GLsizeiptr>(I32(env, args[2])),
        WasmPointer<const void>(U32(env, args[3]))
    );
})

GL_VOID_BINDING(glBindVertexArray, 1, {
    glBindVertexArray(
        static_cast<GLuint>(U32(env, args[0]))
    );
})

/* Framebuffers */

GL_VOID_BINDING(glBindFramebuffer, 2, {
    glBindFramebuffer(
        static_cast<GLenum>(U32(env, args[0])),
        ResolveFramebuffer(
            static_cast<GLuint>(U32(env, args[1]))
        )
    );
})

GL_VOID_BINDING(glGenFramebuffers, 2, {
    glGenFramebuffers(
        I32(env, args[0]),
        WasmPointer<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glFramebufferTexture2D, 5, {
    glFramebufferTexture2D(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        static_cast<GLenum>(U32(env, args[2])),
        static_cast<GLuint>(U32(env, args[3])),
        I32(env, args[4])
    );
})

/* Programs and shaders */

napi_value Host_glCreateProgram(
    napi_env env,
    napi_callback_info info
) {
    size_t argc = 0;
    napi_get_cb_info(env, info, &argc, nullptr, nullptr, nullptr);
    return ToU32(env, glCreateProgram());
}

napi_value Host_glCreateShader(
    napi_env env,
    napi_callback_info info
) {
    napi_value args[1];
    size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);

    return ToU32(
        env,
        glCreateShader(
            static_cast<GLenum>(U32(env, args[0]))
        )
    );
}

GL_VOID_BINDING(glShaderSource, 4, {
    const GLuint shader =
        static_cast<GLuint>(U32(env, args[0]));

    const GLsizei count =
        static_cast<GLsizei>(I32(env, args[1]));

    const uint32_t stringsOffset =
        U32(env, args[2]);

    const uint32_t lengthsOffset =
        U32(env, args[3]);

    const uint32_t* wasmStringOffsets =
        WasmPointer<const uint32_t>(stringsOffset);

    std::vector<const GLchar*> strings(
        static_cast<size_t>(count)
    );

    for (GLsizei index = 0; index < count; ++index) {
        strings[static_cast<size_t>(index)] =
            WasmPointer<const GLchar>(
                wasmStringOffsets[index]
            );
    }

    const GLint* lengths =
        WasmPointer<const GLint>(lengthsOffset);

    glShaderSource(
        shader,
        count,
        strings.data(),
        lengths
    );
})

GL_VOID_BINDING(glCompileShader, 1, {
    glCompileShader(
        static_cast<GLuint>(U32(env, args[0]))
    );
})

GL_VOID_BINDING(glGetShaderiv, 3, {
    glGetShaderiv(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        WasmPointer<GLint>(U32(env, args[2]))
    );
})

GL_VOID_BINDING(glAttachShader, 2, {
    glAttachShader(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glBindAttribLocation, 3, {
    glBindAttribLocation(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1])),
        WasmPointer<const GLchar>(U32(env, args[2]))
    );
})

GL_VOID_BINDING(glLinkProgram, 1, {
    glLinkProgram(
        static_cast<GLuint>(U32(env, args[0]))
    );
})

GL_VOID_BINDING(glGetProgramiv, 3, {
    glGetProgramiv(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        WasmPointer<GLint>(U32(env, args[2]))
    );
})

napi_value Host_glGetUniformLocation(
    napi_env env,
    napi_callback_info info
) {
    napi_value args[2];
    size_t argc = 2;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);

    const GLint result = glGetUniformLocation(
        static_cast<GLuint>(U32(env, args[0])),
        WasmPointer<const GLchar>(U32(env, args[1]))
    );

    return ToI32(env, result);
}

GL_VOID_BINDING(glDeleteShader, 1, {
    glDeleteShader(
        static_cast<GLuint>(U32(env, args[0]))
    );
})

GL_VOID_BINDING(glUseProgram, 1, {
    glUseProgram(
        static_cast<GLuint>(U32(env, args[0]))
    );
})

/* Uniforms */

GL_VOID_BINDING(glUniform4fv, 3, {
    glUniform4fv(
        I32(env, args[0]),
        I32(env, args[1]),
        WasmPointer<const GLfloat>(U32(env, args[2]))
    );
})

GL_VOID_BINDING(glUniform1i, 2, {
    glUniform1i(
        I32(env, args[0]),
        I32(env, args[1])
    );
})

GL_VOID_BINDING(glUniform2f, 3, {
    glUniform2f(
        I32(env, args[0]),
        F32(env, args[1]),
        F32(env, args[2])
    );
})

/* Vertex attributes and drawing */

GL_VOID_BINDING(glEnableVertexAttribArray, 1, {
    glEnableVertexAttribArray(
        static_cast<GLuint>(U32(env, args[0]))
    );
})

GL_VOID_BINDING(glDisableVertexAttribArray, 1, {
    glDisableVertexAttribArray(
        static_cast<GLuint>(U32(env, args[0]))
    );
})

GL_VOID_BINDING(glVertexAttribPointer, 6, {
    glVertexAttribPointer(
        static_cast<GLuint>(U32(env, args[0])),
        I32(env, args[1]),
        static_cast<GLenum>(U32(env, args[2])),
        static_cast<GLboolean>(U32(env, args[3])),
        I32(env, args[4]),
        BufferOffset(U32(env, args[5]))
    );
})

GL_VOID_BINDING(glVertexAttribIPointer, 5, {
    glVertexAttribIPointer(
        static_cast<GLuint>(U32(env, args[0])),
        I32(env, args[1]),
        static_cast<GLenum>(U32(env, args[2])),
        I32(env, args[3]),
        BufferOffset(U32(env, args[4]))
    );
})

GL_VOID_BINDING(glVertexAttribDivisor, 2, {
    glVertexAttribDivisor(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glDrawElementsInstanced, 5, {
    glDrawElementsInstanced(
        static_cast<GLenum>(U32(env, args[0])),
        I32(env, args[1]),
        static_cast<GLenum>(U32(env, args[2])),
        BufferOffset(U32(env, args[3])),
        I32(env, args[4])
    );
})

GL_VOID_BINDING(glDrawRangeElements, 6, {
    glDrawRangeElements(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1])),
        static_cast<GLuint>(U32(env, args[2])),
        I32(env, args[3]),
        static_cast<GLenum>(U32(env, args[4])),
        BufferOffset(U32(env, args[5]))
    );
})

GL_VOID_BINDING(glDrawArrays, 3, {
    glDrawArrays(
        static_cast<GLenum>(U32(env, args[0])),
        I32(env, args[1]),
        I32(env, args[2])
    );
})

/* Textures */

GL_VOID_BINDING(glGenTextures, 2, {
    glGenTextures(
        I32(env, args[0]),
        WasmPointer<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glActiveTexture, 1, {
    glActiveTexture(
        static_cast<GLenum>(U32(env, args[0]))
    );
})

GL_VOID_BINDING(glBindTexture, 2, {
    glBindTexture(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glTexParameteri, 3, {
    glTexParameteri(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        I32(env, args[2])
    );
})

GL_VOID_BINDING(glTexStorage2D, 5, {
    glTexStorage2D(
        static_cast<GLenum>(U32(env, args[0])),
        I32(env, args[1]),
        static_cast<GLenum>(U32(env, args[2])),
        I32(env, args[3]),
        I32(env, args[4])
    );
})

GL_VOID_BINDING(glTexSubImage2D, 9, {
    glTexSubImage2D(
        static_cast<GLenum>(U32(env, args[0])),
        I32(env, args[1]),
        I32(env, args[2]),
        I32(env, args[3]),
        I32(env, args[4]),
        I32(env, args[5]),
        static_cast<GLenum>(U32(env, args[6])),
        static_cast<GLenum>(U32(env, args[7])),
        PixelUnpackPointer(U32(env, args[8]))
    );
})

/* Samplers */

GL_VOID_BINDING(glGenSamplers, 2, {
    glGenSamplers(
        I32(env, args[0]),
        WasmPointer<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glSamplerParameteri, 3, {
    glSamplerParameteri(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        I32(env, args[2])
    );
})

GL_VOID_BINDING(glSamplerParameterf, 3, {
    glSamplerParameterf(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        F32(env, args[2])
    );
})

GL_VOID_BINDING(glBindSampler, 2, {
    glBindSampler(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1]))
    );
})

/* macOS OpenGL extensions */

GL_VOID_BINDING(glTextureBarrierNV, 0, {
    glTextureBarrierNV();
})

GL_VOID_BINDING(glInsertEventMarkerEXT, 2, {
    glInsertEventMarkerEXT(
        I32(env, args[0]),
        WasmPointer<const GLchar>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glPopGroupMarkerEXT, 0, {
    glPopGroupMarkerEXT();
})

GL_VOID_BINDING(glPushGroupMarkerEXT, 2, {
    glPushGroupMarkerEXT(
        I32(env, args[0]),
        WasmPointer<const GLchar>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glGetInternalformativ, 5, {
    glGetInternalformativ(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])),
        static_cast<GLenum>(U32(env, args[2])),
        static_cast<GLsizei>(I32(env, args[3])),
        WasmPointer<GLint>(U32(env, args[4]))
    );
})

GL_VOID_BINDING(glBindFragDataLocation, 3, {
    glBindFragDataLocation(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1])),
        WasmPointer<const GLchar>(U32(env, args[2]))
    );
})

GL_VOID_BINDING(glBindFragDataLocationIndexed, 4, {
    glBindFragDataLocationIndexed(
        static_cast<GLuint>(U32(env, args[0])),
        static_cast<GLuint>(U32(env, args[1])),
        static_cast<GLuint>(U32(env, args[2])),
        WasmPointer<const GLchar>(U32(env, args[3]))
    );
})

GL_VOID_BINDING(glPolygonMode, 2, {
    glPolygonMode(
        static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glGenVertexArrays, 2, {
    glGenVertexArrays(
        static_cast<GLsizei>(I32(env, args[0])),
        WasmPointer<GLuint>(U32(env, args[1]))
    );
})

GL_VOID_BINDING(glDeleteVertexArrays, 2, {
    glDeleteVertexArrays(
        static_cast<GLsizei>(I32(env, args[0])),
        WasmPointer<const GLuint>(U32(env, args[1]))
    );
})

#include "native-gl-missing-bindings.inc"

#undef GL_VOID_BINDING

#define EXPORT_GL(name)                                         \
    {                                                           \
        #name,                                                  \
        nullptr,                                                \
        Host_##name,                                            \
        nullptr,                                                \
        nullptr,                                                \
        nullptr,                                                \
        napi_default,                                           \
        nullptr                                                 \
    }

napi_value Initialize(
    napi_env env,
    napi_value exports
) {
    napi_property_descriptor functions[] = {
        {"createContext", nullptr, CreateContext, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"makeContextCurrent", nullptr, MakeContextCurrent, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"readContextPixels", nullptr, ReadContextPixels, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"destroyContext", nullptr, DestroyContext, nullptr, nullptr, nullptr, napi_default, nullptr},
        {
            "attachWasmRuntime",
            nullptr,
            AttachWasmRuntime,
            nullptr,
            nullptr,
            nullptr,
            napi_default,
            nullptr
        },

        EXPORT_GL(glGetString),
        EXPORT_GL(glGetIntegerv),
        EXPORT_GL(glGetStringi),
        EXPORT_GL(glGetShaderPrecisionFormat),
        EXPORT_GL(glGetFloatv),
        EXPORT_GL(glGetError),

        EXPORT_GL(glDisable),
        EXPORT_GL(glDepthMask),
        EXPORT_GL(glFrontFace),
        EXPORT_GL(glLineWidth),
        EXPORT_GL(glPixelStorei),

        EXPORT_GL(glGenBuffers),
        EXPORT_GL(glBindBuffer),
        EXPORT_GL(glBufferData),
        EXPORT_GL(glBindVertexArray),
        EXPORT_GL(glBufferSubData),
        EXPORT_GL(glBindFramebuffer),

        EXPORT_GL(glViewport),
        EXPORT_GL(glClearColor),
        EXPORT_GL(glColorMask),
        EXPORT_GL(glClear),

        EXPORT_GL(glCreateProgram),
        EXPORT_GL(glCreateShader),
        EXPORT_GL(glShaderSource),
        EXPORT_GL(glCompileShader),
        EXPORT_GL(glGetShaderiv),
        EXPORT_GL(glAttachShader),
        EXPORT_GL(glBindAttribLocation),
        EXPORT_GL(glLinkProgram),
        EXPORT_GL(glGetProgramiv),
        EXPORT_GL(glGetUniformLocation),
        EXPORT_GL(glDeleteShader),
        EXPORT_GL(glUseProgram),

        EXPORT_GL(glEnable),
        EXPORT_GL(glBlendEquation),
        EXPORT_GL(glBlendFunc),
        EXPORT_GL(glUniform4fv),

        EXPORT_GL(glEnableVertexAttribArray),
        EXPORT_GL(glDisableVertexAttribArray),
        EXPORT_GL(glVertexAttribPointer),
        EXPORT_GL(glVertexAttribDivisor),
        EXPORT_GL(glDrawElementsInstanced),

        EXPORT_GL(glGenTextures),
        EXPORT_GL(glActiveTexture),
        EXPORT_GL(glBindTexture),
        EXPORT_GL(glTexParameteri),
        EXPORT_GL(glTexStorage2D),

        EXPORT_GL(glGenFramebuffers),
        EXPORT_GL(glFramebufferTexture2D),
        EXPORT_GL(glTexSubImage2D),

        EXPORT_GL(glDrawRangeElements),
        EXPORT_GL(glDrawArrays),
        EXPORT_GL(glUniform1i),
        EXPORT_GL(glUniform2f),

        EXPORT_GL(glGenSamplers),
        EXPORT_GL(glSamplerParameteri),
        EXPORT_GL(glSamplerParameterf),
        EXPORT_GL(glBindSampler),
        EXPORT_GL(glVertexAttribIPointer),

        EXPORT_GL(glTextureBarrierNV),
        EXPORT_GL(glInsertEventMarkerEXT),
        EXPORT_GL(glPopGroupMarkerEXT),
        EXPORT_GL(glPushGroupMarkerEXT),
        EXPORT_GL(glGetInternalformativ),
        EXPORT_GL(glBindFragDataLocation),
        EXPORT_GL(glBindFragDataLocationIndexed),
        EXPORT_GL(glPolygonMode),
        EXPORT_GL(glGenVertexArrays),
        EXPORT_GL(glDeleteVertexArrays),
        #include "native-gl-missing-exports.inc"
    };

    napi_define_properties(
        env,
        exports,
        sizeof(functions) / sizeof(functions[0]),
        functions
    );

    return exports;
}

#undef EXPORT_GL

} // namespace

NAPI_MODULE(NODE_GYP_MODULE_NAME, Initialize)
