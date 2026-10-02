#include <node_api.h>

#define GL_SILENCE_DEPRECATION
#include <OpenGL/OpenGL.h>
#include <OpenGL/gl3.h>
#include <OpenGL/gl3ext.h>

#include <SDL.h>

#include <cstdio>
#include <cstdint>
#include <cstring>
#include <string>
#include <unordered_map>
#include <vector>
#include <array>
#include <algorithm>
#include <memory>

#include "include/gpu/ganesh/gl/GrGLInterface.h"
#include "include/gpu/ganesh/gl/mac/GrGLMakeMacInterface.h"

namespace {

napi_value Undefined(napi_env env);
uint32_t U32(napi_env env, napi_value value);
napi_value ToU32(napi_env env, uint32_t value);
int32_t I32(napi_env env, napi_value value);
float F32(napi_env env, napi_value value);
napi_value ToBool(napi_env env, bool value);

napi_ref wasmMemoryRef = nullptr;
napi_ref wasmMallocRef = nullptr;

uint8_t* wasmMemoryBase = nullptr;
template <typename T>
T* WasmPointer(uint32_t offset) {
    if (offset == 0) {
        return nullptr;
    }

    return reinterpret_cast<T*>(wasmMemoryBase + offset);
}

struct RenderTarget {
    CGLContextObj context = nullptr;
    GLuint framebuffer = 0;
    GLuint color = 0;
    GLuint depthStencil = 0;
    int width = 0;
    int height = 0;

    sk_sp<const GrGLInterface> glInterface;
};

SDL_Window* presentationWindow = nullptr;
SDL_GLContext presentationGL = nullptr;
CGLContextObj presentationCGL = nullptr;
GLuint presentationReadFramebuffer = 0;
bool presentationOpen = false;

// CGLContextObj nativeContext = nullptr;
uint32_t nextContextId = 1;
uint32_t currentContextId = 0;
std::unordered_map<uint32_t, RenderTarget> renderTargets;

void Throw(napi_env env, const char* message) {
    napi_throw_error(env, nullptr, message);
}

void ThrowSDLError(napi_env env, const char* operation) {
    const std::string message = std::string(operation) + ": " + SDL_GetError();
    Throw(env, message.c_str());
}

std::string String(napi_env env, napi_value value) {
    size_t length = 0;
    napi_get_value_string_utf8(env, value, nullptr, 0, &length);
    std::string result(length + 1, '\0');
    napi_get_value_string_utf8(env, value, result.data(), result.size(), &length);
    result.resize(length);
    return result;
}

const GrGLInterface* CurrentSkiaGL(napi_env env) {
    const auto found = renderTargets.find(currentContextId);

    if (currentContextId == 0 ||
        found == renderTargets.end() ||
        !found->second.glInterface) {
        Throw(env, "No active Skia GL interface");
        return nullptr;
    }

    return found->second.glInterface.get();
}
#include "native-skia-gl-bindings.inc"

napi_value GetSkiaGLFunction(
        napi_env env,
        napi_callback_info info) {
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
        Throw(env, "getGLFunction expects a name");
        return nullptr;
    }

    size_t length = 0;

    napi_get_value_string_utf8(
        env,
        args[0],
        nullptr,
        0,
        &length
    );

    std::string name(length + 1, '\0');

    napi_get_value_string_utf8(
        env,
        args[0],
        name.data(),
        name.size(),
        &length
    );

    name.resize(length);

    napi_callback callback =
        FindGeneratedSkiaGLCallback(name);

    // Special functions use their explicit ABI adapters.
    if (callback == nullptr) {
        return Undefined(env);
    }

    napi_value function;

    napi_create_function(
        env,
        name.c_str(),
        NAPI_AUTO_LENGTH,
        callback,
        nullptr,
        &function
    );

    fprintf(
        stderr,
        "[skia-gl] resolved through GrGLInterface: %s\n",
        name.c_str()
    );

    return function;
}

CGLContextObj CreateNativeContext(napi_env env) {
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
    const CGLError result =
        CGLCreateContext(pixelFormat, presentationCGL, &context);
    CGLDestroyPixelFormat(pixelFormat);
    if (result != kCGLNoError || context == nullptr) {
        Throw(env, "CGLCreateContext failed");
        return nullptr;
    }

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

napi_value CreateContext(napi_env env, napi_callback_info info) {
    napi_value args[2];
    size_t argc = 2;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    if (argc < 1) return nullptr;

    if (presentationCGL == nullptr) {
        Throw(env, "createWindow must be called before createContext");
        return nullptr;
    }

    RenderTarget target;
    target.context = CreateNativeContext(env);
    if (target.context == nullptr) return nullptr;

    if (CGLSetCurrentContext(target.context) != kCGLNoError) {
        CGLDestroyContext(target.context);
        Throw(env, "CGLSetCurrentContext failed");
        return nullptr;
    }
    target.glInterface = GrGLInterfaces::MakeMac();

    if (!target.glInterface || !target.glInterface->validate()) {
        CGLSetCurrentContext(nullptr);
        CGLDestroyContext(target.context);
        Throw(env, "Skia could not create a valid native GrGLInterface");
        return nullptr;
    }
    target.width = ObjectI32(env, args[0], "width");
    target.height = ObjectI32(env, args[0], "height");

    glGenFramebuffers(1, &target.framebuffer);
    glBindFramebuffer(GL_FRAMEBUFFER, target.framebuffer);

    glGenTextures(1, &target.color);
    glBindTexture(GL_TEXTURE_2D, target.color);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8,
                 target.width, target.height, 0,
                 GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                           GL_TEXTURE_2D, target.color, 0);

    glGenRenderbuffers(1, &target.depthStencil);
    glBindRenderbuffer(GL_RENDERBUFFER, target.depthStencil);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8,
                          target.width, target.height);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
                              GL_RENDERBUFFER, target.depthStencil);

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
        glDeleteRenderbuffers(1, &target.depthStencil);
        glDeleteTextures(1, &target.color);
        glDeleteFramebuffers(1, &target.framebuffer);
        CGLSetCurrentContext(nullptr);
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

napi_value CreateWindow(napi_env env, napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    if (argc != 3) {
        Throw(env, "createWindow expects width, height, and title");
        return nullptr;
    }
    if (presentationWindow != nullptr) {
        Throw(env, "The native presentation window already exists");
        return nullptr;
    }

    SDL_SetMainReady();
    if (SDL_InitSubSystem(SDL_INIT_VIDEO) != 0) {
        ThrowSDLError(env, "SDL_InitSubSystem");
        return nullptr;
    }

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 2);
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE);
    SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1);
    SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, 24);
    SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE, 8);

    presentationWindow = SDL_CreateWindow(
        String(env, args[2]).c_str(),
        SDL_WINDOWPOS_CENTERED,
        SDL_WINDOWPOS_CENTERED,
        I32(env, args[0]),
        I32(env, args[1]),
        SDL_WINDOW_SHOWN | SDL_WINDOW_ALLOW_HIGHDPI | SDL_WINDOW_OPENGL);
    if (presentationWindow == nullptr) {
        ThrowSDLError(env, "SDL_CreateWindow");
        SDL_QuitSubSystem(SDL_INIT_VIDEO);
        return nullptr;
    }

    presentationGL = SDL_GL_CreateContext(presentationWindow);
    if (presentationGL == nullptr) {
        ThrowSDLError(env, "SDL_GL_CreateContext");
        SDL_DestroyWindow(presentationWindow);
        presentationWindow = nullptr;
        SDL_QuitSubSystem(SDL_INIT_VIDEO);
        return nullptr;
    }
    if (SDL_GL_MakeCurrent(presentationWindow, presentationGL) != 0) {
        ThrowSDLError(env, "SDL_GL_MakeCurrent");
        SDL_GL_DeleteContext(presentationGL);
        SDL_DestroyWindow(presentationWindow);
        presentationGL = nullptr;
        presentationWindow = nullptr;
        SDL_QuitSubSystem(SDL_INIT_VIDEO);
        return nullptr;
    }
    currentContextId = 0;

    presentationCGL = CGLGetCurrentContext();
    if (presentationCGL == nullptr) {
        Throw(env, "SDL did not expose a native CGL context");
        SDL_GL_DeleteContext(presentationGL);
        SDL_DestroyWindow(presentationWindow);
        presentationGL = nullptr;
        presentationWindow = nullptr;
        SDL_QuitSubSystem(SDL_INIT_VIDEO);
        return nullptr;
    }

    SDL_GL_SetSwapInterval(1);
    glGenFramebuffers(1, &presentationReadFramebuffer);
    presentationOpen = true;
    return Undefined(env);
}

napi_value PollEvents(napi_env env, napi_callback_info) {
    if (presentationWindow == nullptr) return ToBool(env, false);

    SDL_Event event;
    while (SDL_PollEvent(&event)) {
        if (event.type == SDL_QUIT ||
            (event.type == SDL_WINDOWEVENT &&
             event.window.windowID == SDL_GetWindowID(presentationWindow) &&
             event.window.event == SDL_WINDOWEVENT_CLOSE)) {
            presentationOpen = false;
        }
    }
    return ToBool(env, presentationOpen);
}

napi_value SetWindowTitle(napi_env env, napi_callback_info info) {
    napi_value args[1];
    size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    if (argc == 1 && presentationWindow != nullptr) {
        SDL_SetWindowTitle(presentationWindow, String(env, args[0]).c_str());
    }
    return Undefined(env);
}

struct CanvasLayout {
    int x;
    int y;
    int width;
    int height;
};

napi_value Present(napi_env env, napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    if (argc != 3 || presentationWindow == nullptr || !presentationOpen) {
        return Undefined(env);
    }

    const std::array<uint32_t, 3> contextIds = {
        U32(env, args[0]), U32(env, args[1]), U32(env, args[2])};
    constexpr std::array<CanvasLayout, 3> layouts = {{
        {0, 0, 300, 300},
        {306, 0, 300, 300},
        {0, 306, 606, 400},
    }};

    // Submit every producer context before consuming its shared texture.
    for (uint32_t contextId : contextIds) {
        if (contextId == 0) continue;
        const auto found = renderTargets.find(contextId);
        if (found == renderTargets.end()) continue;
        CGLSetCurrentContext(found->second.context);
        glFlush();
    }

    if (CGLSetCurrentContext(presentationCGL) != kCGLNoError) {
        Throw(env, "CGLSetCurrentContext failed for presentation context");
        return nullptr;
    }
    currentContextId = 0;

    int pixelWidth = 0;
    int pixelHeight = 0;
    SDL_GL_GetDrawableSize(presentationWindow, &pixelWidth, &pixelHeight);
    const double scaleX = static_cast<double>(pixelWidth) / 606.0;
    const double scaleY = static_cast<double>(pixelHeight) / 706.0;

    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    glViewport(0, 0, pixelWidth, pixelHeight);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_BLEND);
    glEnable(GL_SCISSOR_TEST);
    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
    glScissor(0, 0, pixelWidth, pixelHeight);
    glClearColor(1, 1, 1, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    for (size_t index = 0; index < contextIds.size(); ++index) {
        const auto found = renderTargets.find(contextIds[index]);
        if (found == renderTargets.end()) continue;
        const RenderTarget& target = found->second;
        const CanvasLayout& layout = layouts[index];
        const int x = static_cast<int>(layout.x * scaleX);
        const int top = static_cast<int>(layout.y * scaleY);
        const int width = static_cast<int>(layout.width * scaleX);
        const int height = static_cast<int>(layout.height * scaleY);
        const int bottom = pixelHeight - top - height;
        const int borderX = std::max(1, static_cast<int>(scaleX));
        const int borderY = std::max(1, static_cast<int>(scaleY));

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glScissor(x, bottom, width, height);
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, presentationReadFramebuffer);
        glFramebufferTexture2D(
            GL_READ_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            target.color,
            0);
        if (glCheckFramebufferStatus(GL_READ_FRAMEBUFFER) !=
            GL_FRAMEBUFFER_COMPLETE) {
            Throw(env, "Shared canvas framebuffer is incomplete");
            return nullptr;
        }

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glScissor(
            x + borderX,
            bottom + borderY,
            width - 2 * borderX,
            height - 2 * borderY);
        glBlitFramebuffer(
            0,
            0,
            target.width,
            target.height,
            x + borderX,
            bottom + borderY,
            x + width - borderX,
            bottom + height - borderY,
            GL_COLOR_BUFFER_BIT,
            GL_LINEAR);
    }

    glDisable(GL_SCISSOR_TEST);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    SDL_GL_SwapWindow(presentationWindow);
    return Undefined(env);
}

napi_value DestroyWindow(napi_env env, napi_callback_info) {
    if (presentationWindow == nullptr) return Undefined(env);
    CGLSetCurrentContext(presentationCGL);
    if (presentationReadFramebuffer != 0) {
        glDeleteFramebuffers(1, &presentationReadFramebuffer);
    }
    SDL_GL_DeleteContext(presentationGL);
    SDL_DestroyWindow(presentationWindow);
    SDL_QuitSubSystem(SDL_INIT_VIDEO);
    presentationReadFramebuffer = 0;
    presentationCGL = nullptr;
    presentationGL = nullptr;
    presentationWindow = nullptr;
    presentationOpen = false;
    return Undefined(env);
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
    glDeleteTextures(1, &target.color);
    glDeleteFramebuffers(1, &target.framebuffer);

    renderTargets.erase(found);

    if (currentContextId == contextId) currentContextId = 0;
    CGLSetCurrentContext(nullptr);
    CGLDestroyContext(target.context);

    return Undefined(env);
}

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

napi_value ToBool(napi_env env, bool value) {
    napi_value result;
    napi_get_boolean(env, value, &result);
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
        {"createWindow", nullptr, CreateWindow, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"pollEvents", nullptr, PollEvents, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"setWindowTitle", nullptr, SetWindowTitle, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"present", nullptr, Present, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"destroyWindow", nullptr, DestroyWindow, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"createContext", nullptr, CreateContext, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"makeContextCurrent", nullptr, MakeContextCurrent, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"destroyContext", nullptr, DestroyContext, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"attachWasmRuntime", nullptr, AttachWasmRuntime, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"getGLFunction", nullptr, GetSkiaGLFunction, nullptr, nullptr, nullptr, napi_default, nullptr},
        #include "native-gl-special-exports.inc"
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
