// Generated file. Do not edit manually.

napi_value GeneratedUndefined(napi_env env) {
    napi_value value;
    napi_get_undefined(env, &value);
    return value;
}

int64_t GeneratedI64(napi_env env, napi_value value) {
    int64_t result = 0;
    napi_get_value_int64(env, value, &result);
    return result;
}

uint64_t GeneratedU64(napi_env env, napi_value value) {
    bool lossless = false;
    uint64_t result = 0;

    if (napi_get_value_bigint_uint64(
            env,
            value,
            &result,
            &lossless
        ) == napi_ok) {
        return result;
    }

    double number = 0;
    napi_get_value_double(env, value, &number);
    return static_cast<uint64_t>(number);
}

napi_value Generated_glActiveTexture(
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
        Throw(
            env,
            "glActiveTexture expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fActiveTexture(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glAttachShader(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glAttachShader expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fAttachShader(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBeginQuery(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glBeginQuery expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBeginQuery(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindAttribLocation(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glBindAttribLocation expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindAttribLocation(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<const GrGLchar>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindBuffer(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glBindBuffer expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindBuffer(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindFragDataLocation(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glBindFragDataLocation expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindFragDataLocation(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<const GrGLchar>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindFragDataLocationIndexed(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glBindFragDataLocationIndexed expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindFragDataLocationIndexed(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2]),
            WasmPointer<const GrGLchar>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindRenderbuffer(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glBindRenderbuffer expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindRenderbuffer(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindSampler(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glBindSampler expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindSampler(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindTexture(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glBindTexture expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindTexture(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBindVertexArray(
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
        Throw(
            env,
            "glBindVertexArray expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBindVertexArray(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBlendColor(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glBlendColor expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBlendColor(
            F32(env, args[0]),
            F32(env, args[1]),
            F32(env, args[2]),
            F32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBlendEquation(
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
        Throw(
            env,
            "glBlendEquation expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBlendEquation(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBlendFunc(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glBlendFunc expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBlendFunc(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBlitFramebuffer(
        napi_env env,
        napi_callback_info info) {
    napi_value args[10];
    size_t argc = 10;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 10) {
        Throw(
            env,
            "glBlitFramebuffer expects 10 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBlitFramebuffer(
            I32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3]),
            I32(env, args[4]),
            I32(env, args[5]),
            I32(env, args[6]),
            I32(env, args[7]),
            U32(env, args[8]),
            U32(env, args[9])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBufferData(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glBufferData expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBufferData(
            U32(env, args[0]),
            GeneratedI64(env, args[1]),
            WasmPointer<const uint8_t>(U32(env, args[2])),
            U32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glBufferSubData(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glBufferSubData expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fBufferSubData(
            U32(env, args[0]),
            GeneratedI64(env, args[1]),
            GeneratedI64(env, args[2]),
            WasmPointer<const uint8_t>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glCheckFramebufferStatus(
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
        Throw(
            env,
            "glCheckFramebufferStatus expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    const auto result = gl->fFunctions.fCheckFramebufferStatus(
            U32(env, args[0])
        );
    napi_value value;
    napi_create_uint32(
        env,
        static_cast<uint32_t>(result),
        &value
    );
    return value;
}

napi_value Generated_glClear(
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
        Throw(
            env,
            "glClear expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fClear(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glClearColor(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glClearColor expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fClearColor(
            F32(env, args[0]),
            F32(env, args[1]),
            F32(env, args[2]),
            F32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glClearStencil(
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
        Throw(
            env,
            "glClearStencil expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fClearStencil(
            I32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glColorMask(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glColorMask expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fColorMask(
            static_cast<uint8_t>(U32(env, args[0])),
            static_cast<uint8_t>(U32(env, args[1])),
            static_cast<uint8_t>(U32(env, args[2])),
            static_cast<uint8_t>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glCompileShader(
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
        Throw(
            env,
            "glCompileShader expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fCompileShader(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glCopyBufferSubData(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glCopyBufferSubData expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fCopyBufferSubData(
            U32(env, args[0]),
            U32(env, args[1]),
            GeneratedI64(env, args[2]),
            GeneratedI64(env, args[3]),
            GeneratedI64(env, args[4])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glCopyTexSubImage2D(
        napi_env env,
        napi_callback_info info) {
    napi_value args[8];
    size_t argc = 8;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 8) {
        Throw(
            env,
            "glCopyTexSubImage2D expects 8 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fCopyTexSubImage2D(
            U32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3]),
            I32(env, args[4]),
            I32(env, args[5]),
            I32(env, args[6]),
            I32(env, args[7])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glCreateProgram(
        napi_env env,
        napi_callback_info info) {
    size_t argc = 0;
    napi_get_cb_info(
        env,
        info,
        &argc,
        nullptr,
        nullptr,
        nullptr
    );

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    const auto result = gl->fFunctions.fCreateProgram();
    napi_value value;
    napi_create_uint32(
        env,
        static_cast<uint32_t>(result),
        &value
    );
    return value;
}

napi_value Generated_glCreateShader(
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
        Throw(
            env,
            "glCreateShader expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    const auto result = gl->fFunctions.fCreateShader(
            U32(env, args[0])
        );
    napi_value value;
    napi_create_uint32(
        env,
        static_cast<uint32_t>(result),
        &value
    );
    return value;
}

napi_value Generated_glCullFace(
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
        Throw(
            env,
            "glCullFace expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fCullFace(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteBuffers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDeleteBuffers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteBuffers(
            I32(env, args[0]),
            WasmPointer<const GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteFramebuffers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDeleteFramebuffers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteFramebuffers(
            I32(env, args[0]),
            WasmPointer<const GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteProgram(
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
        Throw(
            env,
            "glDeleteProgram expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteProgram(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteQueries(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDeleteQueries expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteQueries(
            I32(env, args[0]),
            WasmPointer<const GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteRenderbuffers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDeleteRenderbuffers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteRenderbuffers(
            I32(env, args[0]),
            WasmPointer<const GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteSamplers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDeleteSamplers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteSamplers(
            I32(env, args[0]),
            WasmPointer<const GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteShader(
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
        Throw(
            env,
            "glDeleteShader expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteShader(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteTextures(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDeleteTextures expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteTextures(
            I32(env, args[0]),
            WasmPointer<const GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDeleteVertexArrays(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDeleteVertexArrays expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDeleteVertexArrays(
            I32(env, args[0]),
            WasmPointer<const GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDepthMask(
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
        Throw(
            env,
            "glDepthMask expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDepthMask(
            static_cast<uint8_t>(U32(env, args[0]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDisable(
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
        Throw(
            env,
            "glDisable expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDisable(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDisableVertexAttribArray(
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
        Throw(
            env,
            "glDisableVertexAttribArray expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDisableVertexAttribArray(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDrawArrays(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glDrawArrays expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDrawArrays(
            U32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDrawArraysInstanced(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glDrawArraysInstanced expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDrawArraysInstanced(
            U32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDrawBuffer(
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
        Throw(
            env,
            "glDrawBuffer expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDrawBuffer(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glDrawBuffers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glDrawBuffers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fDrawBuffers(
            I32(env, args[0]),
            WasmPointer<const GrGLenum>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glEnable(
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
        Throw(
            env,
            "glEnable expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fEnable(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glEnableVertexAttribArray(
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
        Throw(
            env,
            "glEnableVertexAttribArray expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fEnableVertexAttribArray(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glEndQuery(
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
        Throw(
            env,
            "glEndQuery expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fEndQuery(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glFinish(
        napi_env env,
        napi_callback_info info) {
    size_t argc = 0;
    napi_get_cb_info(
        env,
        info,
        &argc,
        nullptr,
        nullptr,
        nullptr
    );

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fFinish();
    return GeneratedUndefined(env);
}

napi_value Generated_glFlush(
        napi_env env,
        napi_callback_info info) {
    size_t argc = 0;
    napi_get_cb_info(
        env,
        info,
        &argc,
        nullptr,
        nullptr,
        nullptr
    );

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fFlush();
    return GeneratedUndefined(env);
}

napi_value Generated_glFlushMappedBufferRange(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glFlushMappedBufferRange expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fFlushMappedBufferRange(
            U32(env, args[0]),
            GeneratedI64(env, args[1]),
            GeneratedI64(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glFramebufferRenderbuffer(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glFramebufferRenderbuffer expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fFramebufferRenderbuffer(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2]),
            U32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glFramebufferTexture2D(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glFramebufferTexture2D expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fFramebufferTexture2D(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2]),
            U32(env, args[3]),
            I32(env, args[4])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glFrontFace(
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
        Throw(
            env,
            "glFrontFace expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fFrontFace(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenBuffers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGenBuffers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenBuffers(
            I32(env, args[0]),
            WasmPointer<GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenFramebuffers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGenFramebuffers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenFramebuffers(
            I32(env, args[0]),
            WasmPointer<GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenQueries(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGenQueries expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenQueries(
            I32(env, args[0]),
            WasmPointer<GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenRenderbuffers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGenRenderbuffers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenRenderbuffers(
            I32(env, args[0]),
            WasmPointer<GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenSamplers(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGenSamplers expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenSamplers(
            I32(env, args[0]),
            WasmPointer<GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenTextures(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGenTextures expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenTextures(
            I32(env, args[0]),
            WasmPointer<GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenVertexArrays(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGenVertexArrays expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenVertexArrays(
            I32(env, args[0]),
            WasmPointer<GrGLuint>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGenerateMipmap(
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
        Throw(
            env,
            "glGenerateMipmap expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGenerateMipmap(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetBufferParameteriv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetBufferParameteriv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetBufferParameteriv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetError(
        napi_env env,
        napi_callback_info info) {
    size_t argc = 0;
    napi_get_cb_info(
        env,
        info,
        &argc,
        nullptr,
        nullptr,
        nullptr
    );

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    const auto result = gl->fFunctions.fGetError();
    napi_value value;
    napi_create_uint32(
        env,
        static_cast<uint32_t>(result),
        &value
    );
    return value;
}

napi_value Generated_glGetFloatv(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGetFloatv expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetFloatv(
            U32(env, args[0]),
            WasmPointer<GrGLfloat>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetFramebufferAttachmentParameteriv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glGetFramebufferAttachmentParameteriv expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetFramebufferAttachmentParameteriv(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2]),
            WasmPointer<GrGLint>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetInternalformativ(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glGetInternalformativ expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetInternalformativ(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2]),
            I32(env, args[3]),
            WasmPointer<GrGLint>(U32(env, args[4]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetMultisamplefv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetMultisamplefv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetMultisamplefv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLfloat>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetProgramBinary(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glGetProgramBinary expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetProgramBinary(
            U32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<GrGLsizei>(U32(env, args[2])),
            WasmPointer<GrGLenum>(U32(env, args[3])),
            WasmPointer<uint8_t>(U32(env, args[4]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetProgramInfoLog(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glGetProgramInfoLog expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetProgramInfoLog(
            U32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<GrGLsizei>(U32(env, args[2])),
            WasmPointer<GrGLchar>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetProgramiv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetProgramiv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetProgramiv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetQueryObjecti64v(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetQueryObjecti64v expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetQueryObjecti64v(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLint64>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetQueryObjectui64v(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetQueryObjectui64v expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetQueryObjectui64v(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLuint64>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetQueryObjectuiv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetQueryObjectuiv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetQueryObjectuiv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLuint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetQueryiv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetQueryiv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetQueryiv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetRenderbufferParameteriv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetRenderbufferParameteriv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetRenderbufferParameteriv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetShaderInfoLog(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glGetShaderInfoLog expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetShaderInfoLog(
            U32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<GrGLsizei>(U32(env, args[2])),
            WasmPointer<GrGLchar>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetShaderPrecisionFormat(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glGetShaderPrecisionFormat expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetShaderPrecisionFormat(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLint>(U32(env, args[2])),
            WasmPointer<GrGLint>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetShaderiv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glGetShaderiv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetShaderiv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetTexLevelParameteriv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glGetTexLevelParameteriv expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fGetTexLevelParameteriv(
            U32(env, args[0]),
            I32(env, args[1]),
            U32(env, args[2]),
            WasmPointer<GrGLint>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glGetUniformLocation(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glGetUniformLocation expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    const auto result = gl->fFunctions.fGetUniformLocation(
            U32(env, args[0]),
            WasmPointer<const GrGLchar>(U32(env, args[1]))
        );
    napi_value value;
    napi_create_int32(
        env,
        static_cast<int32_t>(result),
        &value
    );
    return value;
}

napi_value Generated_glInsertEventMarkerEXT(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glInsertEventMarkerEXT expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fInsertEventMarker(
            I32(env, args[0]),
            WasmPointer<const char>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glIsTexture(
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
        Throw(
            env,
            "glIsTexture expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    const auto result = gl->fFunctions.fIsTexture(
            U32(env, args[0])
        );
    napi_value value;
    napi_create_uint32(
        env,
        static_cast<uint32_t>(result),
        &value
    );
    return value;
}

napi_value Generated_glLineWidth(
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
        Throw(
            env,
            "glLineWidth expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fLineWidth(
            F32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glLinkProgram(
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
        Throw(
            env,
            "glLinkProgram expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fLinkProgram(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glPatchParameteri(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glPatchParameteri expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fPatchParameteri(
            U32(env, args[0]),
            I32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glPixelStorei(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glPixelStorei expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fPixelStorei(
            U32(env, args[0]),
            I32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glPolygonMode(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glPolygonMode expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fPolygonMode(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glPopGroupMarkerEXT(
        napi_env env,
        napi_callback_info info) {
    size_t argc = 0;
    napi_get_cb_info(
        env,
        info,
        &argc,
        nullptr,
        nullptr,
        nullptr
    );

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fPopGroupMarker();
    return GeneratedUndefined(env);
}

napi_value Generated_glProgramBinary(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glProgramBinary expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fProgramBinary(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<uint8_t>(U32(env, args[2])),
            I32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glProgramParameteri(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glProgramParameteri expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fProgramParameteri(
            U32(env, args[0]),
            U32(env, args[1]),
            I32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glPushGroupMarkerEXT(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glPushGroupMarkerEXT expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fPushGroupMarker(
            I32(env, args[0]),
            WasmPointer<const char>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glQueryCounter(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glQueryCounter expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fQueryCounter(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glReadBuffer(
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
        Throw(
            env,
            "glReadBuffer expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fReadBuffer(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glRenderbufferStorage(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glRenderbufferStorage expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fRenderbufferStorage(
            U32(env, args[0]),
            U32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glRenderbufferStorageMultisample(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glRenderbufferStorageMultisample expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fRenderbufferStorageMultisample(
            U32(env, args[0]),
            I32(env, args[1]),
            U32(env, args[2]),
            I32(env, args[3]),
            I32(env, args[4])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glSamplerParameterf(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glSamplerParameterf expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fSamplerParameterf(
            U32(env, args[0]),
            U32(env, args[1]),
            F32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glSamplerParameteri(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glSamplerParameteri expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fSamplerParameteri(
            U32(env, args[0]),
            U32(env, args[1]),
            I32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glSamplerParameteriv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glSamplerParameteriv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fSamplerParameteriv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<const GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glScissor(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glScissor expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fScissor(
            I32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glStencilFunc(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glStencilFunc expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fStencilFunc(
            U32(env, args[0]),
            I32(env, args[1]),
            U32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glStencilFuncSeparate(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glStencilFuncSeparate expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fStencilFuncSeparate(
            U32(env, args[0]),
            U32(env, args[1]),
            I32(env, args[2]),
            U32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glStencilMask(
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
        Throw(
            env,
            "glStencilMask expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fStencilMask(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glStencilMaskSeparate(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glStencilMaskSeparate expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fStencilMaskSeparate(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glStencilOp(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glStencilOp expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fStencilOp(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glStencilOpSeparate(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glStencilOpSeparate expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fStencilOpSeparate(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2]),
            U32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glTexBuffer(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glTexBuffer expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fTexBuffer(
            U32(env, args[0]),
            U32(env, args[1]),
            U32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glTexParameterf(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glTexParameterf expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fTexParameterf(
            U32(env, args[0]),
            U32(env, args[1]),
            F32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glTexParameterfv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glTexParameterfv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fTexParameterfv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<const GrGLfloat>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glTexParameteri(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glTexParameteri expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fTexParameteri(
            U32(env, args[0]),
            U32(env, args[1]),
            I32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glTexParameteriv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glTexParameteriv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fTexParameteriv(
            U32(env, args[0]),
            U32(env, args[1]),
            WasmPointer<const GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glTexStorage2D(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glTexStorage2D expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fTexStorage2D(
            U32(env, args[0]),
            I32(env, args[1]),
            U32(env, args[2]),
            I32(env, args[3]),
            I32(env, args[4])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glTextureBarrierNV(
        napi_env env,
        napi_callback_info info) {
    size_t argc = 0;
    napi_get_cb_info(
        env,
        info,
        &argc,
        nullptr,
        nullptr,
        nullptr
    );

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fTextureBarrier();
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform1f(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glUniform1f expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform1f(
            I32(env, args[0]),
            F32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform1fv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform1fv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform1fv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLfloat>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform1i(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glUniform1i expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform1i(
            I32(env, args[0]),
            I32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform1iv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform1iv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform1iv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform2f(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform2f expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform2f(
            I32(env, args[0]),
            F32(env, args[1]),
            F32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform2fv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform2fv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform2fv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLfloat>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform2i(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform2i expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform2i(
            I32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform2iv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform2iv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform2iv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform3f(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glUniform3f expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform3f(
            I32(env, args[0]),
            F32(env, args[1]),
            F32(env, args[2]),
            F32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform3fv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform3fv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform3fv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLfloat>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform3i(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glUniform3i expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform3i(
            I32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform3iv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform3iv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform3iv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform4f(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glUniform4f expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform4f(
            I32(env, args[0]),
            F32(env, args[1]),
            F32(env, args[2]),
            F32(env, args[3]),
            F32(env, args[4])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform4fv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform4fv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform4fv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLfloat>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform4i(
        napi_env env,
        napi_callback_info info) {
    napi_value args[5];
    size_t argc = 5;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 5) {
        Throw(
            env,
            "glUniform4i expects 5 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform4i(
            I32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3]),
            I32(env, args[4])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniform4iv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[3];
    size_t argc = 3;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 3) {
        Throw(
            env,
            "glUniform4iv expects 3 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniform4iv(
            I32(env, args[0]),
            I32(env, args[1]),
            WasmPointer<const GrGLint>(U32(env, args[2]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniformMatrix2fv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glUniformMatrix2fv expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniformMatrix2fv(
            I32(env, args[0]),
            I32(env, args[1]),
            static_cast<uint8_t>(U32(env, args[2])),
            WasmPointer<const GrGLfloat>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniformMatrix3fv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glUniformMatrix3fv expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniformMatrix3fv(
            I32(env, args[0]),
            I32(env, args[1]),
            static_cast<uint8_t>(U32(env, args[2])),
            WasmPointer<const GrGLfloat>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUniformMatrix4fv(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glUniformMatrix4fv expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUniformMatrix4fv(
            I32(env, args[0]),
            I32(env, args[1]),
            static_cast<uint8_t>(U32(env, args[2])),
            WasmPointer<const GrGLfloat>(U32(env, args[3]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glUseProgram(
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
        Throw(
            env,
            "glUseProgram expects 1 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fUseProgram(
            U32(env, args[0])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glVertexAttrib1f(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glVertexAttrib1f expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fVertexAttrib1f(
            U32(env, args[0]),
            F32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glVertexAttrib2fv(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glVertexAttrib2fv expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fVertexAttrib2fv(
            U32(env, args[0]),
            WasmPointer<const GrGLfloat>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glVertexAttrib3fv(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glVertexAttrib3fv expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fVertexAttrib3fv(
            U32(env, args[0]),
            WasmPointer<const GrGLfloat>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glVertexAttrib4fv(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glVertexAttrib4fv expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fVertexAttrib4fv(
            U32(env, args[0]),
            WasmPointer<const GrGLfloat>(U32(env, args[1]))
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glVertexAttribDivisor(
        napi_env env,
        napi_callback_info info) {
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

    if (argc != 2) {
        Throw(
            env,
            "glVertexAttribDivisor expects 2 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fVertexAttribDivisor(
            U32(env, args[0]),
            U32(env, args[1])
        );
    return GeneratedUndefined(env);
}

napi_value Generated_glViewport(
        napi_env env,
        napi_callback_info info) {
    napi_value args[4];
    size_t argc = 4;

    napi_get_cb_info(
        env,
        info,
        &argc,
        args,
        nullptr,
        nullptr
    );

    if (argc != 4) {
        Throw(
            env,
            "glViewport expects 4 arguments"
        );
        return nullptr;
    }

    const GrGLInterface* gl = CurrentSkiaGL(env);
    if (gl == nullptr) {
        return nullptr;
    }

    gl->fFunctions.fViewport(
            I32(env, args[0]),
            I32(env, args[1]),
            I32(env, args[2]),
            I32(env, args[3])
        );
    return GeneratedUndefined(env);
}

struct GeneratedSkiaGLBinding {
    const char* name;
    napi_callback callback;
};

const GeneratedSkiaGLBinding generatedSkiaGLBindings[] = {
    {"glActiveTexture", Generated_glActiveTexture},
    {"glAttachShader", Generated_glAttachShader},
    {"glBeginQuery", Generated_glBeginQuery},
    {"glBindAttribLocation", Generated_glBindAttribLocation},
    {"glBindBuffer", Generated_glBindBuffer},
    {"glBindFragDataLocation", Generated_glBindFragDataLocation},
    {"glBindFragDataLocationIndexed", Generated_glBindFragDataLocationIndexed},
    {"glBindRenderbuffer", Generated_glBindRenderbuffer},
    {"glBindSampler", Generated_glBindSampler},
    {"glBindTexture", Generated_glBindTexture},
    {"glBindVertexArray", Generated_glBindVertexArray},
    {"glBlendColor", Generated_glBlendColor},
    {"glBlendEquation", Generated_glBlendEquation},
    {"glBlendFunc", Generated_glBlendFunc},
    {"glBlitFramebuffer", Generated_glBlitFramebuffer},
    {"glBufferData", Generated_glBufferData},
    {"glBufferSubData", Generated_glBufferSubData},
    {"glCheckFramebufferStatus", Generated_glCheckFramebufferStatus},
    {"glClear", Generated_glClear},
    {"glClearColor", Generated_glClearColor},
    {"glClearStencil", Generated_glClearStencil},
    {"glColorMask", Generated_glColorMask},
    {"glCompileShader", Generated_glCompileShader},
    {"glCopyBufferSubData", Generated_glCopyBufferSubData},
    {"glCopyTexSubImage2D", Generated_glCopyTexSubImage2D},
    {"glCreateProgram", Generated_glCreateProgram},
    {"glCreateShader", Generated_glCreateShader},
    {"glCullFace", Generated_glCullFace},
    {"glDeleteBuffers", Generated_glDeleteBuffers},
    {"glDeleteFramebuffers", Generated_glDeleteFramebuffers},
    {"glDeleteProgram", Generated_glDeleteProgram},
    {"glDeleteQueries", Generated_glDeleteQueries},
    {"glDeleteRenderbuffers", Generated_glDeleteRenderbuffers},
    {"glDeleteSamplers", Generated_glDeleteSamplers},
    {"glDeleteShader", Generated_glDeleteShader},
    {"glDeleteTextures", Generated_glDeleteTextures},
    {"glDeleteVertexArrays", Generated_glDeleteVertexArrays},
    {"glDepthMask", Generated_glDepthMask},
    {"glDisable", Generated_glDisable},
    {"glDisableVertexAttribArray", Generated_glDisableVertexAttribArray},
    {"glDrawArrays", Generated_glDrawArrays},
    {"glDrawArraysInstanced", Generated_glDrawArraysInstanced},
    {"glDrawBuffer", Generated_glDrawBuffer},
    {"glDrawBuffers", Generated_glDrawBuffers},
    {"glEnable", Generated_glEnable},
    {"glEnableVertexAttribArray", Generated_glEnableVertexAttribArray},
    {"glEndQuery", Generated_glEndQuery},
    {"glFinish", Generated_glFinish},
    {"glFlush", Generated_glFlush},
    {"glFlushMappedBufferRange", Generated_glFlushMappedBufferRange},
    {"glFramebufferRenderbuffer", Generated_glFramebufferRenderbuffer},
    {"glFramebufferTexture2D", Generated_glFramebufferTexture2D},
    {"glFrontFace", Generated_glFrontFace},
    {"glGenBuffers", Generated_glGenBuffers},
    {"glGenFramebuffers", Generated_glGenFramebuffers},
    {"glGenQueries", Generated_glGenQueries},
    {"glGenRenderbuffers", Generated_glGenRenderbuffers},
    {"glGenSamplers", Generated_glGenSamplers},
    {"glGenTextures", Generated_glGenTextures},
    {"glGenVertexArrays", Generated_glGenVertexArrays},
    {"glGenerateMipmap", Generated_glGenerateMipmap},
    {"glGetBufferParameteriv", Generated_glGetBufferParameteriv},
    {"glGetError", Generated_glGetError},
    {"glGetFloatv", Generated_glGetFloatv},
    {"glGetFramebufferAttachmentParameteriv", Generated_glGetFramebufferAttachmentParameteriv},
    {"glGetInternalformativ", Generated_glGetInternalformativ},
    {"glGetMultisamplefv", Generated_glGetMultisamplefv},
    {"glGetProgramBinary", Generated_glGetProgramBinary},
    {"glGetProgramInfoLog", Generated_glGetProgramInfoLog},
    {"glGetProgramiv", Generated_glGetProgramiv},
    {"glGetQueryObjecti64v", Generated_glGetQueryObjecti64v},
    {"glGetQueryObjectui64v", Generated_glGetQueryObjectui64v},
    {"glGetQueryObjectuiv", Generated_glGetQueryObjectuiv},
    {"glGetQueryiv", Generated_glGetQueryiv},
    {"glGetRenderbufferParameteriv", Generated_glGetRenderbufferParameteriv},
    {"glGetShaderInfoLog", Generated_glGetShaderInfoLog},
    {"glGetShaderPrecisionFormat", Generated_glGetShaderPrecisionFormat},
    {"glGetShaderiv", Generated_glGetShaderiv},
    {"glGetTexLevelParameteriv", Generated_glGetTexLevelParameteriv},
    {"glGetUniformLocation", Generated_glGetUniformLocation},
    {"glInsertEventMarkerEXT", Generated_glInsertEventMarkerEXT},
    {"glIsTexture", Generated_glIsTexture},
    {"glLineWidth", Generated_glLineWidth},
    {"glLinkProgram", Generated_glLinkProgram},
    {"glPatchParameteri", Generated_glPatchParameteri},
    {"glPixelStorei", Generated_glPixelStorei},
    {"glPolygonMode", Generated_glPolygonMode},
    {"glPopGroupMarkerEXT", Generated_glPopGroupMarkerEXT},
    {"glProgramBinary", Generated_glProgramBinary},
    {"glProgramParameteri", Generated_glProgramParameteri},
    {"glPushGroupMarkerEXT", Generated_glPushGroupMarkerEXT},
    {"glQueryCounter", Generated_glQueryCounter},
    {"glReadBuffer", Generated_glReadBuffer},
    {"glRenderbufferStorage", Generated_glRenderbufferStorage},
    {"glRenderbufferStorageMultisample", Generated_glRenderbufferStorageMultisample},
    {"glSamplerParameterf", Generated_glSamplerParameterf},
    {"glSamplerParameteri", Generated_glSamplerParameteri},
    {"glSamplerParameteriv", Generated_glSamplerParameteriv},
    {"glScissor", Generated_glScissor},
    {"glStencilFunc", Generated_glStencilFunc},
    {"glStencilFuncSeparate", Generated_glStencilFuncSeparate},
    {"glStencilMask", Generated_glStencilMask},
    {"glStencilMaskSeparate", Generated_glStencilMaskSeparate},
    {"glStencilOp", Generated_glStencilOp},
    {"glStencilOpSeparate", Generated_glStencilOpSeparate},
    {"glTexBuffer", Generated_glTexBuffer},
    {"glTexParameterf", Generated_glTexParameterf},
    {"glTexParameterfv", Generated_glTexParameterfv},
    {"glTexParameteri", Generated_glTexParameteri},
    {"glTexParameteriv", Generated_glTexParameteriv},
    {"glTexStorage2D", Generated_glTexStorage2D},
    {"glTextureBarrierNV", Generated_glTextureBarrierNV},
    {"glUniform1f", Generated_glUniform1f},
    {"glUniform1fv", Generated_glUniform1fv},
    {"glUniform1i", Generated_glUniform1i},
    {"glUniform1iv", Generated_glUniform1iv},
    {"glUniform2f", Generated_glUniform2f},
    {"glUniform2fv", Generated_glUniform2fv},
    {"glUniform2i", Generated_glUniform2i},
    {"glUniform2iv", Generated_glUniform2iv},
    {"glUniform3f", Generated_glUniform3f},
    {"glUniform3fv", Generated_glUniform3fv},
    {"glUniform3i", Generated_glUniform3i},
    {"glUniform3iv", Generated_glUniform3iv},
    {"glUniform4f", Generated_glUniform4f},
    {"glUniform4fv", Generated_glUniform4fv},
    {"glUniform4i", Generated_glUniform4i},
    {"glUniform4iv", Generated_glUniform4iv},
    {"glUniformMatrix2fv", Generated_glUniformMatrix2fv},
    {"glUniformMatrix3fv", Generated_glUniformMatrix3fv},
    {"glUniformMatrix4fv", Generated_glUniformMatrix4fv},
    {"glUseProgram", Generated_glUseProgram},
    {"glVertexAttrib1f", Generated_glVertexAttrib1f},
    {"glVertexAttrib2fv", Generated_glVertexAttrib2fv},
    {"glVertexAttrib3fv", Generated_glVertexAttrib3fv},
    {"glVertexAttrib4fv", Generated_glVertexAttrib4fv},
    {"glVertexAttribDivisor", Generated_glVertexAttribDivisor},
    {"glViewport", Generated_glViewport},
};

napi_callback FindGeneratedSkiaGLCallback(
        const std::string& name) {
    for (const auto& binding : generatedSkiaGLBindings) {
        if (name == binding.name) {
            return binding.callback;
        }
    }

    return nullptr;
}
