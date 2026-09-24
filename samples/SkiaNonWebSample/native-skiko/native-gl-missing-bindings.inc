/*
 * Supplemental raw desktop-OpenGL bindings for Skiko Wasm.
 * Include this inside binding.cc's anonymous namespace, immediately before
 * `#undef GL_VOID_BINDING`.
 */

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

GL_VOID_BINDING(glBeginQuery, 2, {
    glBeginQuery(static_cast<GLenum>(U32(env, args[0])), U32(env, args[1]));
})

GL_VOID_BINDING(glBindRenderbuffer, 2, {
    glBindRenderbuffer(static_cast<GLenum>(U32(env, args[0])), U32(env, args[1]));
})

GL_VOID_BINDING(glBlendColor, 4, {
    glBlendColor(F32(env, args[0]), F32(env, args[1]), F32(env, args[2]), F32(env, args[3]));
})

GL_VOID_BINDING(glBlitFramebuffer, 10, {
    glBlitFramebuffer(I32(env, args[0]), I32(env, args[1]), I32(env, args[2]), I32(env, args[3]),
                      I32(env, args[4]), I32(env, args[5]), I32(env, args[6]), I32(env, args[7]),
                      U32(env, args[8]), static_cast<GLenum>(U32(env, args[9])));
})

napi_value Host_glCheckFramebufferStatus(napi_env env, napi_callback_info info) {
    napi_value args[1]; size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    return ToU32(env, glCheckFramebufferStatus(static_cast<GLenum>(U32(env, args[0]))));
}

GL_VOID_BINDING(glClearStencil, 1, { glClearStencil(I32(env, args[0])); })

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

GL_VOID_BINDING(glCopyBufferSubData, 5, {
    glCopyBufferSubData(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), I32(env, args[2]), I32(env, args[3]),
        I32(env, args[4]));
})

GL_VOID_BINDING(glCopyTexSubImage2D, 8, {
    glCopyTexSubImage2D(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
        I32(env, args[2]), I32(env, args[3]), I32(env, args[4]), I32(env, args[5]),
        I32(env, args[6]), I32(env, args[7]));
})

GL_VOID_BINDING(glCullFace, 1, { glCullFace(static_cast<GLenum>(U32(env, args[0]))); })

#define DELETE_OBJECTS_BINDING(name, type) \
    GL_VOID_BINDING(name, 2, { name(I32(env, args[0]), WasmPointer<const type>(U32(env, args[1]))); })

DELETE_OBJECTS_BINDING(glDeleteBuffers, GLuint)
DELETE_OBJECTS_BINDING(glDeleteFramebuffers, GLuint)
DELETE_OBJECTS_BINDING(glDeleteQueries, GLuint)
DELETE_OBJECTS_BINDING(glDeleteRenderbuffers, GLuint)
DELETE_OBJECTS_BINDING(glDeleteSamplers, GLuint)
DELETE_OBJECTS_BINDING(glDeleteTextures, GLuint)

#undef DELETE_OBJECTS_BINDING

GL_VOID_BINDING(glDeleteProgram, 1, { glDeleteProgram(U32(env, args[0])); })

GL_VOID_BINDING(glDrawArraysInstanced, 4, {
    glDrawArraysInstanced(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
                          I32(env, args[2]), I32(env, args[3]));
})

GL_VOID_BINDING(glDrawArraysIndirect, 2, {
    glDrawArraysIndirect(static_cast<GLenum>(U32(env, args[0])), BufferOffset(U32(env, args[1])));
})

GL_VOID_BINDING(glDrawBuffer, 1, { glDrawBuffer(static_cast<GLenum>(U32(env, args[0]))); })

GL_VOID_BINDING(glDrawBuffers, 2, {
    glDrawBuffers(I32(env, args[0]), WasmPointer<const GLenum>(U32(env, args[1])));
})

GL_VOID_BINDING(glDrawElements, 4, {
    glDrawElements(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
                   static_cast<GLenum>(U32(env, args[2])), BufferOffset(U32(env, args[3])));
})

GL_VOID_BINDING(glDrawElementsIndirect, 3, {
    glDrawElementsIndirect(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), BufferOffset(U32(env, args[2])));
})

GL_VOID_BINDING(glEndQuery, 1, { glEndQuery(static_cast<GLenum>(U32(env, args[0]))); })
GL_VOID_BINDING(glFinish, 0, { glFinish(); })
GL_VOID_BINDING(glFlush, 0, { glFlush(); })

GL_VOID_BINDING(glFramebufferRenderbuffer, 4, {
    glFramebufferRenderbuffer(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), static_cast<GLenum>(U32(env, args[2])),
        U32(env, args[3]));
})

GL_VOID_BINDING(glGenerateMipmap, 1, {
    glGenerateMipmap(static_cast<GLenum>(U32(env, args[0])));
})

#define GEN_OBJECTS_BINDING(name, type) \
    GL_VOID_BINDING(name, 2, { name(I32(env, args[0]), WasmPointer<type>(U32(env, args[1]))); })

GEN_OBJECTS_BINDING(glGenQueries, GLuint)
GEN_OBJECTS_BINDING(glGenRenderbuffers, GLuint)

#undef GEN_OBJECTS_BINDING

GL_VOID_BINDING(glGetBufferParameteriv, 3, {
    glGetBufferParameteriv(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), WasmPointer<GLint>(U32(env, args[2])));
})

GL_VOID_BINDING(glGetFramebufferAttachmentParameteriv, 4, {
    glGetFramebufferAttachmentParameteriv(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), static_cast<GLenum>(U32(env, args[2])),
        WasmPointer<GLint>(U32(env, args[3])));
})

GL_VOID_BINDING(glGetMultisamplefv, 3, {
    glGetMultisamplefv(static_cast<GLenum>(U32(env, args[0])), U32(env, args[1]),
                       WasmPointer<GLfloat>(U32(env, args[2])));
})

GL_VOID_BINDING(glGetProgramBinary, 5, {
    glGetProgramBinary(U32(env, args[0]), I32(env, args[1]),
        WasmPointer<GLsizei>(U32(env, args[2])), WasmPointer<GLenum>(U32(env, args[3])),
        WasmPointer<void>(U32(env, args[4])));
})

GL_VOID_BINDING(glGetProgramInfoLog, 4, {
    glGetProgramInfoLog(U32(env, args[0]), I32(env, args[1]),
        WasmPointer<GLsizei>(U32(env, args[2])), WasmPointer<GLchar>(U32(env, args[3])));
})

GL_VOID_BINDING(glGetQueryObjecti64v, 3, {
    glGetQueryObjecti64v(U32(env, args[0]), static_cast<GLenum>(U32(env, args[1])),
                         WasmPointer<GLint64>(U32(env, args[2])));
})

GL_VOID_BINDING(glGetQueryObjectui64v, 3, {
    glGetQueryObjectui64v(U32(env, args[0]), static_cast<GLenum>(U32(env, args[1])),
                          WasmPointer<GLuint64>(U32(env, args[2])));
})

GL_VOID_BINDING(glGetQueryObjectuiv, 3, {
    glGetQueryObjectuiv(U32(env, args[0]), static_cast<GLenum>(U32(env, args[1])),
                        WasmPointer<GLuint>(U32(env, args[2])));
})

GL_VOID_BINDING(glGetQueryiv, 3, {
    glGetQueryiv(static_cast<GLenum>(U32(env, args[0])), static_cast<GLenum>(U32(env, args[1])),
                 WasmPointer<GLint>(U32(env, args[2])));
})

GL_VOID_BINDING(glGetRenderbufferParameteriv, 3, {
    glGetRenderbufferParameteriv(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), WasmPointer<GLint>(U32(env, args[2])));
})

GL_VOID_BINDING(glGetShaderInfoLog, 4, {
    glGetShaderInfoLog(U32(env, args[0]), I32(env, args[1]),
        WasmPointer<GLsizei>(U32(env, args[2])), WasmPointer<GLchar>(U32(env, args[3])));
})

GL_VOID_BINDING(glGetTexLevelParameteriv, 4, {
    glGetTexLevelParameteriv(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
        static_cast<GLenum>(U32(env, args[2])), WasmPointer<GLint>(U32(env, args[3])));
})

napi_value Host_glIsTexture(napi_env env, napi_callback_info info) {
    napi_value args[1]; size_t argc = 1;
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);
    return ToU32(env, glIsTexture(U32(env, args[0])));
}

GL_VOID_BINDING(glPatchParameteri, 2, {
    glPatchParameteri(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]));
})

GL_VOID_BINDING(glProgramBinary, 4, {
    glProgramBinary(U32(env, args[0]), static_cast<GLenum>(U32(env, args[1])),
        WasmPointer<void>(U32(env, args[2])), I32(env, args[3]));
})

GL_VOID_BINDING(glProgramParameteri, 3, {
    glProgramParameteri(U32(env, args[0]), static_cast<GLenum>(U32(env, args[1])), I32(env, args[2]));
})

GL_VOID_BINDING(glQueryCounter, 2, {
    glQueryCounter(U32(env, args[0]), static_cast<GLenum>(U32(env, args[1])));
})

GL_VOID_BINDING(glReadBuffer, 1, { glReadBuffer(static_cast<GLenum>(U32(env, args[0]))); })

GL_VOID_BINDING(glReadPixels, 7, {
    glReadPixels(I32(env, args[0]), I32(env, args[1]), I32(env, args[2]), I32(env, args[3]),
        static_cast<GLenum>(U32(env, args[4])), static_cast<GLenum>(U32(env, args[5])),
        PixelPackPointer(U32(env, args[6])));
})

GL_VOID_BINDING(glRenderbufferStorage, 4, {
    glRenderbufferStorage(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), I32(env, args[2]), I32(env, args[3]));
})

GL_VOID_BINDING(glRenderbufferStorageMultisample, 5, {
    glRenderbufferStorageMultisample(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]),
        static_cast<GLenum>(U32(env, args[2])), I32(env, args[3]), I32(env, args[4]));
})

GL_VOID_BINDING(glSamplerParameteriv, 3, {
    glSamplerParameteriv(U32(env, args[0]), static_cast<GLenum>(U32(env, args[1])),
                         WasmPointer<const GLint>(U32(env, args[2])));
})

GL_VOID_BINDING(glScissor, 4, {
    glScissor(I32(env, args[0]), I32(env, args[1]), I32(env, args[2]), I32(env, args[3]));
})

GL_VOID_BINDING(glStencilFunc, 3, {
    glStencilFunc(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]), U32(env, args[2]));
})

GL_VOID_BINDING(glStencilFuncSeparate, 4, {
    glStencilFuncSeparate(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), I32(env, args[2]), U32(env, args[3]));
})

GL_VOID_BINDING(glStencilMask, 1, { glStencilMask(U32(env, args[0])); })

GL_VOID_BINDING(glStencilMaskSeparate, 2, {
    glStencilMaskSeparate(static_cast<GLenum>(U32(env, args[0])), U32(env, args[1]));
})

GL_VOID_BINDING(glStencilOp, 3, {
    glStencilOp(static_cast<GLenum>(U32(env, args[0])), static_cast<GLenum>(U32(env, args[1])),
                static_cast<GLenum>(U32(env, args[2])));
})

GL_VOID_BINDING(glStencilOpSeparate, 4, {
    glStencilOpSeparate(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), static_cast<GLenum>(U32(env, args[2])),
        static_cast<GLenum>(U32(env, args[3])));
})

GL_VOID_BINDING(glTexBuffer, 3, {
    glTexBuffer(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), U32(env, args[2]));
})

GL_VOID_BINDING(glTexImage2D, 9, {
    glTexImage2D(static_cast<GLenum>(U32(env, args[0])), I32(env, args[1]), I32(env, args[2]),
        I32(env, args[3]), I32(env, args[4]), I32(env, args[5]),
        static_cast<GLenum>(U32(env, args[6])), static_cast<GLenum>(U32(env, args[7])),
        PixelUnpackPointer(U32(env, args[8])));
})

GL_VOID_BINDING(glTexParameterf, 3, {
    glTexParameterf(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), F32(env, args[2]));
})

GL_VOID_BINDING(glTexParameterfv, 3, {
    glTexParameterfv(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), WasmPointer<const GLfloat>(U32(env, args[2])));
})

GL_VOID_BINDING(glTexParameteriv, 3, {
    glTexParameteriv(static_cast<GLenum>(U32(env, args[0])),
        static_cast<GLenum>(U32(env, args[1])), WasmPointer<const GLint>(U32(env, args[2])));
})

GL_VOID_BINDING(glUniform1f, 2, { glUniform1f(I32(env, args[0]), F32(env, args[1])); })
GL_VOID_BINDING(glUniform1fv, 3, { glUniform1fv(I32(env, args[0]), I32(env, args[1]), WasmPointer<const GLfloat>(U32(env, args[2]))); })
GL_VOID_BINDING(glUniform1iv, 3, { glUniform1iv(I32(env, args[0]), I32(env, args[1]), WasmPointer<const GLint>(U32(env, args[2]))); })
GL_VOID_BINDING(glUniform2i, 3, { glUniform2i(I32(env, args[0]), I32(env, args[1]), I32(env, args[2])); })
GL_VOID_BINDING(glUniform2fv, 3, { glUniform2fv(I32(env, args[0]), I32(env, args[1]), WasmPointer<const GLfloat>(U32(env, args[2]))); })
GL_VOID_BINDING(glUniform2iv, 3, { glUniform2iv(I32(env, args[0]), I32(env, args[1]), WasmPointer<const GLint>(U32(env, args[2]))); })
GL_VOID_BINDING(glUniform3f, 4, { glUniform3f(I32(env, args[0]), F32(env, args[1]), F32(env, args[2]), F32(env, args[3])); })
GL_VOID_BINDING(glUniform3i, 4, { glUniform3i(I32(env, args[0]), I32(env, args[1]), I32(env, args[2]), I32(env, args[3])); })
GL_VOID_BINDING(glUniform3fv, 3, { glUniform3fv(I32(env, args[0]), I32(env, args[1]), WasmPointer<const GLfloat>(U32(env, args[2]))); })
GL_VOID_BINDING(glUniform3iv, 3, { glUniform3iv(I32(env, args[0]), I32(env, args[1]), WasmPointer<const GLint>(U32(env, args[2]))); })
GL_VOID_BINDING(glUniform4f, 5, { glUniform4f(I32(env, args[0]), F32(env, args[1]), F32(env, args[2]), F32(env, args[3]), F32(env, args[4])); })
GL_VOID_BINDING(glUniform4i, 5, { glUniform4i(I32(env, args[0]), I32(env, args[1]), I32(env, args[2]), I32(env, args[3]), I32(env, args[4])); })
GL_VOID_BINDING(glUniform4iv, 3, { glUniform4iv(I32(env, args[0]), I32(env, args[1]), WasmPointer<const GLint>(U32(env, args[2]))); })

#define MATRIX_BINDING(name) \
    GL_VOID_BINDING(name, 4, { name(I32(env, args[0]), I32(env, args[1]), static_cast<GLboolean>(U32(env, args[2])), WasmPointer<const GLfloat>(U32(env, args[3]))); })

MATRIX_BINDING(glUniformMatrix2fv)
MATRIX_BINDING(glUniformMatrix3fv)
MATRIX_BINDING(glUniformMatrix4fv)

#undef MATRIX_BINDING

GL_VOID_BINDING(glVertexAttrib1f, 2, { glVertexAttrib1f(U32(env, args[0]), F32(env, args[1])); })
GL_VOID_BINDING(glVertexAttrib2fv, 2, { glVertexAttrib2fv(U32(env, args[0]), WasmPointer<const GLfloat>(U32(env, args[1]))); })
GL_VOID_BINDING(glVertexAttrib3fv, 2, { glVertexAttrib3fv(U32(env, args[0]), WasmPointer<const GLfloat>(U32(env, args[1]))); })
GL_VOID_BINDING(glVertexAttrib4fv, 2, { glVertexAttrib4fv(U32(env, args[0]), WasmPointer<const GLfloat>(U32(env, args[1]))); })

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

GL_VOID_BINDING(glFlushMappedBufferRange, 3, {
    const GLenum target = static_cast<GLenum>(U32(env, args[0]));
    const GLintptr offset = I32(env, args[1]);
    const GLsizeiptr length = I32(env, args[2]);
    auto& bridge = mappedBuffers[target];
    if (bridge.nativePointer && bridge.writable) {
        std::memcpy(static_cast<uint8_t*>(bridge.nativePointer) + offset,
                    WasmPointer<const uint8_t>(bridge.wasmOffset) + offset,
                    static_cast<size_t>(length));
    }
    glFlushMappedBufferRange(target, offset, length);
})

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
