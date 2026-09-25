/*
 * Supplemental raw desktop-OpenGL bindings for Skiko Wasm.
 * Include this inside binding.cc's anonymous namespace, immediately before
 * `#undef GL_VOID_BINDING`.
 */
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

GL_VOID_BINDING(glBindFramebuffer, 2, {
    glBindFramebuffer(
        static_cast<GLenum>(U32(env, args[0])),
        ResolveFramebuffer(
            static_cast<GLuint>(U32(env, args[1]))
        )
    );
})

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

void* PixelPackPointer(uint32_t offset) {
    if (offset == 0) return nullptr;
    GLint packBuffer = 0;
    glGetIntegerv(GL_PIXEL_PACK_BUFFER_BINDING, &packBuffer);
    return packBuffer != 0
        ? const_cast<void*>(BufferOffset(offset))
        : WasmPointer<void>(offset);
}

struct MappedBufferBridge {
    void* nativePointer = nullptr;
    uint32_t wasmOffset = 0;
    size_t length = 0;
    size_t capacity = 0;
    bool writable = false;
};

std::unordered_map<GLenum, MappedBufferBridge> mappedBuffers;

uint32_t MapNativeBufferToWasm(
    napi_env env,
    GLenum target,
    void* nativePointer,
    size_t length,
    bool readable,
    bool writable
) {
    if (nativePointer == nullptr) return 0;
    auto& bridge = mappedBuffers[target];
    if (bridge.wasmOffset == 0 || bridge.capacity < length) {
        bridge.wasmOffset = WasmAllocate(env, static_cast<uint32_t>(length));
        bridge.capacity = length;
    }
    bridge.nativePointer = nativePointer;
    bridge.length = length;
    bridge.writable = writable;
    if (readable && length != 0) {
        std::memcpy(WasmPointer<void>(bridge.wasmOffset), nativePointer, length);
    }
    return bridge.wasmOffset;
}

std::unordered_map<uint32_t, GLsync> syncHandles;
uint32_t nextSyncHandle = 1;

uint32_t StoreSync(GLsync sync) {
    if (sync == nullptr) return 0;
    const uint32_t handle = nextSyncHandle++;
    syncHandles.emplace(handle, sync);
    return handle;
}

GLsync LookupSync(uint32_t handle) {
    if (handle == 0) return nullptr;
    const auto found = syncHandles.find(handle);
    return found == syncHandles.end() ? nullptr : found->second;
}

uint64_t JoinU64(uint32_t low, uint32_t high) {
    return static_cast<uint64_t>(low) |
           (static_cast<uint64_t>(high) << 32u);
}







GL_VOID_BINDING(glCompressedTexImage2D, 8, {
    glCompressedTexImage2D(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
        static_cast<GLenum>(U32(env, args[2])), I32(env, args[3]), I32(env, args[4]),
        I32(env, args[5]), I32(env, args[6]), PixelUnpackPointer(U32(env, args[7])));
})

GL_VOID_BINDING(glCompressedTexSubImage2D, 9, {
    glCompressedTexSubImage2D(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
        I32(env, args[2]), I32(env, args[3]), I32(env, args[4]), I32(env, args[5]),
        static_cast<GLenum>(U32(env, args[6])), I32(env, args[7]),
        PixelUnpackPointer(U32(env, args[8])));
})




#define DELETE_OBJECTS_BINDING(name, type) \
    GL_VOID_BINDING(name, 2, { name(I32(env, args[0]), WasmPointer<const type>(U32(env, args[1]))); })


#undef DELETE_OBJECTS_BINDING



GL_VOID_BINDING(glDrawArraysIndirect, 2, {
    glDrawArraysIndirect(static_cast<GLenum>(U32(env, args[0])), BufferOffset(U32(env, args[1])));
})



GL_VOID_BINDING(glDrawElements, 4, {
    glDrawElements(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
                   static_cast<GLenum>(U32(env, args[2])), BufferOffset(U32(env, args[3])));
})

GL_VOID_BINDING(glDrawElementsIndirect, 3, {
    glDrawElementsIndirect(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), BufferOffset(U32(env, args[2])));
})




#define GEN_OBJECTS_BINDING(name, type) \
    GL_VOID_BINDING(name, 2, { name(I32(env, args[0]), WasmPointer<type>(U32(env, args[1]))); })


#undef GEN_OBJECTS_BINDING



















GL_VOID_BINDING(glReadPixels, 7, {
    glReadPixels(I32(env, args[0]), I32(env, args[1]), I32(env, args[2]), I32(env, args[3]),
        static_cast<GLenum>(U32(env, args[4])), static_cast<GLenum>(U32(env, args[5])),
        PixelPackPointer(U32(env, args[6])));
})












GL_VOID_BINDING(glTexImage2D, 9, {
    glTexImage2D(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]), I32(env, args[2]),
        I32(env, args[3]), I32(env, args[4]), I32(env, args[5]),
        static_cast<GLenum>(U32(env, args[6])), static_cast<GLenum>(U32(env, args[7])),
        PixelUnpackPointer(U32(env, args[8])));
})





#define MATRIX_BINDING(name) \
    GL_VOID_BINDING(name, 4, { name(I32(env, args[0]), I32(env, args[1]), static_cast<GLboolean>(U32(env, args[2])), WasmPointer<const GLfloat>(U32(env, args[3]))); })


#undef MATRIX_BINDING


napi_value Host_glMapBuffer(napi_env env, napi_callback_info info) {
    napi_value args[2]; size_t argc = 2;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    const GLenum target = static_cast<GLenum>(U32(env, args[0]));
    const GLenum access = static_cast<GLenum>(U32(env, args[1]));
    GLint size = 0;
    glGetBufferParameteriv(target, GL_BUFFER_SIZE, &size);
    void* pointer = glMapBuffer(target, access);
    return ToU32(env, MapNativeBufferToWasm(env, target, pointer, static_cast<size_t>(size),
        access != GL_WRITE_ONLY, access != GL_READ_ONLY));
}

napi_value Host_glMapBufferRange(napi_env env, napi_callback_info info) {
    napi_value args[4]; size_t argc = 4;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    const GLenum target = static_cast<GLenum>(U32(env, args[0]));
    const GLintptr offset = I32(env, args[1]);
    const GLsizeiptr length = I32(env, args[2]);
    const GLbitfield access = U32(env, args[3]);
    void* pointer = glMapBufferRange(target, offset, length, access);
    return ToU32(env, MapNativeBufferToWasm(env, target, pointer, static_cast<size_t>(length),
        (access & GL_MAP_READ_BIT) != 0, (access & GL_MAP_WRITE_BIT) != 0));
}


napi_value Host_glUnmapBuffer(napi_env env, napi_callback_info info) {
    napi_value args[1]; size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    const GLenum target = static_cast<GLenum>(U32(env, args[0]));
    auto& bridge = mappedBuffers[target];
    if (bridge.nativePointer && bridge.writable && bridge.length != 0) {
        std::memcpy(bridge.nativePointer, WasmPointer<const void>(bridge.wasmOffset), bridge.length);
    }
    bridge.nativePointer = nullptr;
    bridge.length = 0;
    bridge.writable = false;
    return ToU32(env, glUnmapBuffer(target));
}

napi_value Host_glFenceSync(napi_env env, napi_callback_info info) {
    napi_value args[2]; size_t argc = 2;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    return ToU32(env, StoreSync(glFenceSync(static_cast<GLenum>(U32(env, args[0])), U32(env, args[1]))));
}

napi_value Host_glIsSync(napi_env env, napi_callback_info info) {
    napi_value args[1]; size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    return ToU32(env, glIsSync(LookupSync(U32(env, args[0]))));
}

napi_value Host_glClientWaitSync(napi_env env, napi_callback_info info) {
    napi_value args[4]; size_t argc = 4;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    return ToU32(env, glClientWaitSync(LookupSync(U32(env, args[0])), U32(env, args[1]),
                                      JoinU64(U32(env, args[2]), U32(env, args[3]))));
}

GL_VOID_BINDING(glWaitSync, 4, {
    glWaitSync(LookupSync(U32(env, args[0])), U32(env, args[1]),
               JoinU64(U32(env, args[2]), U32(env, args[3])));
})

GL_VOID_BINDING(glDeleteSync, 1, {
    const uint32_t handle = U32(env, args[0]);
    glDeleteSync(LookupSync(handle));
    syncHandles.erase(handle);
})
