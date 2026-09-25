#include <node_api.h>

#define GL_SILENCE_DEPRECATION
#include <OpenGL/OpenGL.h>
#include <OpenGL/gl3.h>
#include <OpenGL/gl3ext.h>

#include <ffi/ffi.h>
#include <cstdio>
#include <cstdint>
#include <cstring>
#include <string>
#include <unordered_map>
#include <vector>
#include <array>
#include <algorithm>
#include <dlfcn.h>
#include <memory>
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
int32_t I32(napi_env env, napi_value value);
float F32(napi_env env, napi_value value);

napi_ref wasmMemoryRef = nullptr;
napi_ref wasmMallocRef = nullptr;

uint8_t* wasmMemoryBase = nullptr;
size_t wasmMemorySize = 0;
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
    GLsizei width = 0;
    GLsizei height = 0;
};

enum class FFIKind {
    Void,
    U8,
    I32,
    U32,
    F32,
    I64,
    U64,
    WasmPtr,
};

struct GeneratedGLDescriptor {
    const char* name;
    FFIKind returnKind;
    std::vector<FFIKind> argumentKinds;
};

const std::vector<GeneratedGLDescriptor>
        generatedGLDescriptors = {
#include "native-gl-descriptors.inc"
};

struct FFIDynamicGLDescriptor {
    std::string name;
    void* address = nullptr;

    FFIKind returnKind = FFIKind::Void;
    std::vector<FFIKind> argumentKinds;

    ffi_cif callInterface{};
    ffi_type* returnType = nullptr;
    std::vector<ffi_type*> argumentTypes;
};

std::unordered_map<
    std::string,
    std::unique_ptr<FFIDynamicGLDescriptor>
> ffiGLFunctions;

// CGLContextObj nativeContext = nullptr;
uint32_t nextContextId = 1;
uint32_t currentContextId = 0;
std::unordered_map<uint32_t, RenderTarget> renderTargets;

void Throw(napi_env env, const char* message) {
    napi_throw_error(env, nullptr, message);
}

ffi_type* GetFFIType(FFIKind kind) {
    switch (kind) {
        case FFIKind::Void:
            return &ffi_type_void;

        case FFIKind::U8:
            return &ffi_type_uint8;

        case FFIKind::I32:
            return &ffi_type_sint32;

        case FFIKind::U32:
            return &ffi_type_uint32;

        case FFIKind::F32:
            return &ffi_type_float;

        case FFIKind::I64:
            return &ffi_type_sint64;

        case FFIKind::U64:
            return &ffi_type_uint64;

        case FFIKind::WasmPtr:
            return &ffi_type_pointer;
    }

    return nullptr;
}

union FFIValue {
    uint8_t u8;
    int32_t i32;
    uint32_t u32;
    float f32;
    int64_t i64;
    uint64_t u64;
    void* pointer;
};

napi_value CallFFIGL(
        napi_env env,
        napi_callback_info info) {
    napi_value args[16];
    size_t argc = std::size(args);
    void* callbackData = nullptr;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        &callbackData
    );

    auto* descriptor =
        static_cast<FFIDynamicGLDescriptor*>(
            callbackData
        );

    if (descriptor == nullptr ||
        descriptor->address == nullptr) {
        Throw(env, "Invalid libffi OpenGL descriptor");
        return nullptr;
    }

    if (argc != descriptor->argumentKinds.size()) {
        Throw(env, "Incorrect OpenGL argument count");
        return nullptr;
    }

    std::vector<FFIValue> values(argc);
    std::vector<void*> ffiArguments(argc);

    for (size_t i = 0; i < argc; ++i) {
        switch (descriptor->argumentKinds[i]) {
            case FFIKind::U8:
                values[i].u8 =
                    static_cast<uint8_t>(
                        U32(env, args[i])
                    );
                ffiArguments[i] = &values[i].u8;
                break;

            case FFIKind::I32:
                values[i].i32 = I32(env, args[i]);
                ffiArguments[i] = &values[i].i32;
                break;

            case FFIKind::U32:
                values[i].u32 = U32(env, args[i]);
                ffiArguments[i] = &values[i].u32;
                break;

            case FFIKind::F32:
                values[i].f32 = F32(env, args[i]);
                ffiArguments[i] = &values[i].f32;
                break;

            case FFIKind::I64:
                napi_get_value_int64(
                    env,
                    args[i],
                    &values[i].i64
                );
                ffiArguments[i] = &values[i].i64;
                break;

            case FFIKind::U64: {
                double value = 0;

                napi_get_value_double(
                    env,
                    args[i],
                    &value
                );

                values[i].u64 =
                    static_cast<uint64_t>(value);

                ffiArguments[i] = &values[i].u64;
                break;
            }

            case FFIKind::WasmPtr:
                values[i].pointer =
                    WasmPointer<uint8_t>(
                        U32(env, args[i])
                    );

                ffiArguments[i] =
                    &values[i].pointer;
                break;

            case FFIKind::Void:
                Throw(
                    env,
                    "Void cannot be an argument type"
                );
                return nullptr;
        }
    }

    FFIValue result{};

    ffi_call(
        &descriptor->callInterface,
        FFI_FN(descriptor->address),
        descriptor->returnKind == FFIKind::Void
            ? nullptr
            : &result,
        ffiArguments.data()
    );

    napi_value returnValue;

    switch (descriptor->returnKind) {
        case FFIKind::Void:
            return Undefined(env);

        case FFIKind::U8:
            napi_create_uint32(
                env,
                result.u8,
                &returnValue
            );
            return returnValue;

        case FFIKind::I32:
            napi_create_int32(
                env,
                result.i32,
                &returnValue
            );
            return returnValue;

        case FFIKind::U32:
            napi_create_uint32(
                env,
                result.u32,
                &returnValue
            );
            return returnValue;

        case FFIKind::F32:
            napi_create_double(
                env,
                result.f32,
                &returnValue
            );
            return returnValue;

        case FFIKind::I64:
            napi_create_bigint_int64(
                env,
                result.i64,
                &returnValue
            );
            return returnValue;

        case FFIKind::U64:
            napi_create_bigint_uint64(
                env,
                result.u64,
                &returnValue
            );
            return returnValue;

        case FFIKind::WasmPtr:
            Throw(
                env,
                "Native pointer returns require a special handler"
            );
            return nullptr;
    }

    return nullptr;
}

const GeneratedGLDescriptor*
FindGeneratedGLDescriptor(const std::string& name) {
    for (const auto& descriptor :
         generatedGLDescriptors) {
        if (name == descriptor.name) {
            return &descriptor;
        }
    }

    return nullptr;
}

napi_value GetFFIGLFunction(
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

    const GeneratedGLDescriptor* generated =
        FindGeneratedGLDescriptor(name);

    // Special functions continue through their old wrappers.
    if (generated == nullptr) {
        return Undefined(env);
    }

    auto existing = ffiGLFunctions.find(name);

    if (existing != ffiGLFunctions.end()) {
        napi_value function;

        napi_create_function(
            env,
            name.c_str(),
            NAPI_AUTO_LENGTH,
            CallFFIGL,
            existing->second.get(),
            &function
        );

        return function;
    }

    void* address = dlsym(
        RTLD_DEFAULT,
        name.c_str()
    );

    // Missing native functions use the old explicit fallback.
    if (address == nullptr) {
        return Undefined(env);
    }

    auto descriptor =
        std::make_unique<FFIDynamicGLDescriptor>();

    descriptor->name = name;
    descriptor->address = address;
    descriptor->returnKind =
        generated->returnKind;
    descriptor->argumentKinds =
        generated->argumentKinds;
    descriptor->returnType =
        GetFFIType(descriptor->returnKind);

    for (FFIKind kind :
         descriptor->argumentKinds) {
        descriptor->argumentTypes.push_back(
            GetFFIType(kind)
        );
    }

    const ffi_status status = ffi_prep_cif(
        &descriptor->callInterface,
        FFI_DEFAULT_ABI,
        static_cast<unsigned int>(
            descriptor->argumentTypes.size()
        ),
        descriptor->returnType,
        descriptor->argumentTypes.data()
    );

    if (status != FFI_OK) {
        Throw(env, "ffi_prep_cif failed");
        return nullptr;
    }

    FFIDynamicGLDescriptor* descriptorPointer =
        descriptor.get();

    ffiGLFunctions.emplace(
        name,
        std::move(descriptor)
    );

    napi_value function;

    napi_create_function(
        env,
        name.c_str(),
        NAPI_AUTO_LENGTH,
        CallFFIGL,
        descriptorPointer,
        &function
    );

    fprintf(
        stderr,
        "[ffi-gl] resolved: %s -> %p\n",
        name.c_str(),
        address
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
    const CGLError result = CGLCreateContext(pixelFormat, nullptr, &context);
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
        {"createContext", nullptr, CreateContext, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"makeContextCurrent", nullptr, MakeContextCurrent, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"readContextPixels", nullptr, ReadContextPixels, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"destroyContext", nullptr, DestroyContext, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"attachWasmRuntime", nullptr, AttachWasmRuntime, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"getGLFunction", nullptr, GetFFIGLFunction, nullptr, nullptr, nullptr, napi_default, nullptr},
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
