/**
 * @license
 * Copyright 2010 The Emscripten Authors
 * SPDX-License-Identifier: MIT
 */

export var LibraryWebGL2 = {
  glGetStringi__deps: ['$webglGetExtensions', '$stringToNewUTF8'],
  glGetStringi: (name, index) => {
    if (GL.currentContext.version < 2) {
      GL.recordError(0x502 /* GL_INVALID_OPERATION */); // Calling GLES3/WebGL2 function with a GLES2/WebGL1 context
      return 0;
    }
    var stringiCache = GL.stringiCache[name];
    if (stringiCache) {
      if (index < 0 || index >= stringiCache.length) {
        GL.recordError(0x501/*GL_INVALID_VALUE*/);
        return 0;
      }
      return stringiCache[index];
    }
    switch (name) {
      case 0x1F03 /* GL_EXTENSIONS */:
        var exts = webglGetExtensions().map(stringToNewUTF8);
        stringiCache = GL.stringiCache[name] = exts;
        if (index < 0 || index >= stringiCache.length) {
          GL.recordError(0x501/*GL_INVALID_VALUE*/);
          return 0;
        }
        return stringiCache[index];
      default:
        GL.recordError(0x500/*GL_INVALID_ENUM*/);
        return 0;
    }
  },

  glGetInteger64v__deps: ['$emscriptenWebGLGet'],
  glGetInteger64v: (name_, p) => {
    emscriptenWebGLGet(name_, p, 1);
  },

  glGetInternalformativ: (target, internalformat, pname, bufSize, params) => {
    if (bufSize < 0) {
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    if (!params) {
      // GLES3 specification does not specify how to behave if values is a null pointer. Since calling this function does not make sense
      // if values == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var ret = GLctx.getInternalformatParameter(target, internalformat, pname);
    if (ret === null) return;
    for (var i = 0; i < ret.length && i < bufSize; ++i) {
      HEAP32[(((params)+(i*4))>>2)] = ret[i];
    }
  },

  glCompressedTexImage3D: (target, level, internalFormat, width, height, depth, border, imageSize, data) => {
    if (GLctx.currentPixelUnpackBufferBinding) {
      GLctx.compressedTexImage3D(target, level, internalFormat, width, height, depth, border, imageSize, data);
    } else {
      GLctx.compressedTexImage3D(target, level, internalFormat, width, height, depth, border, HEAPU8, data, imageSize);
    }
  },

  glCompressedTexSubImage3D: (target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data) => {
    if (GLctx.currentPixelUnpackBufferBinding) {
      GLctx.compressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data);
    } else {
      GLctx.compressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, HEAPU8, data, imageSize);
    }
  },

  glGetBufferParameteri64v__deps: ['$writeI53ToI64'],
  glGetBufferParameteri64v: (target, value, data) => {
    if (!data) {
      // GLES2 specification does not specify how to behave if data is a null pointer. Since calling this function does not make sense
      // if data == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    writeI53ToI64(data, GLctx.getBufferParameter(target, value));
  },

  glGetBufferSubData: (target, offset, size, data) => {
    if (!data) {
      // GLES2 specification does not specify how to behave if data is a null pointer. Since calling this function does not make sense
      // if data == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    size && GLctx.getBufferSubData(target, offset, HEAPU8.subarray(data, data+size));
  },

  glInvalidateFramebuffer__deps: ['$tempFixedLengthArray'],
  glInvalidateFramebuffer: (target, numAttachments, attachments) => {
    var list = tempFixedLengthArray[numAttachments];
    for (var i = 0; i < numAttachments; i++) {
      list[i] = HEAP32[(((attachments)+(i*4))>>2)];
    }

    GLctx.invalidateFramebuffer(target, list);
  },

  glInvalidateSubFramebuffer__deps: ['$tempFixedLengthArray'],
  glInvalidateSubFramebuffer: (target, numAttachments, attachments, x, y, width, height) => {
    var list = tempFixedLengthArray[numAttachments];
    for (var i = 0; i < numAttachments; i++) {
      list[i] = HEAP32[(((attachments)+(i*4))>>2)];
    }

    GLctx.invalidateSubFramebuffer(target, list, x, y, width, height);
  },

  glTexImage3D__deps: ['$heapObjectForWebGLType', '$toTypedArrayIndex',
    '$emscriptenWebGLGetTexPixelData',
  ],
  glTexImage3D: (target, level, internalFormat, width, height, depth, border, format, type, pixels) => {
    if (GLctx.currentPixelUnpackBufferBinding) {
      GLctx.texImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    } else if (pixels) {
      var heap = heapObjectForWebGLType(type);
      var pixelData = emscriptenWebGLGetTexPixelData(type, format, width, height * depth, pixels);
      GLctx.texImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixelData);
    } else {
      GLctx.texImage3D(target, level, internalFormat, width, height, depth, border, format, type, null);
    }
  },

  glTexSubImage3D__deps: ['$heapObjectForWebGLType', '$toTypedArrayIndex',
    '$emscriptenWebGLGetTexPixelData',
  ],
  glTexSubImage3D: (target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels) => {
    if (GLctx.currentPixelUnpackBufferBinding) {
      GLctx.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    } else if (pixels) {
      var heap = heapObjectForWebGLType(type);
      var pixelData = emscriptenWebGLGetTexPixelData(type, format, width, height * depth, pixels);
      GLctx.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixelData);
    } else {
      GLctx.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, null);
    }
  },

  // Queries
  glGenQueries: (n, ids) => {
    GL.genObject(n, ids, 'createQuery', GL.queries
      );
  },

  glDeleteQueries: (n, ids) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((ids)+(i*4))>>2)];
      var query = GL.queries[id];
      if (!query) continue; // GL spec: "unused names in ids are ignored, as is the name zero."
      GLctx.deleteQuery(query);
      GL.queries[id] = null;
    }
  },

  glIsQuery: (id) => {
    var query = GL.queries[id];
    if (!query) return 0;
    return GLctx.isQuery(query);
  },

  glBeginQuery: (target, id) => {
    GLctx.beginQuery(target, GL.queries[id]);
  },

  glGetQueryiv: (target, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if p == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAP32[((params)>>2)] = GLctx.getQuery(target, pname);
  },

  glGetQueryObjectuiv: (id, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if p == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var query = GL.queries[id];
    var param = GLctx.getQueryParameter(query, pname);
    var ret;
    if (typeof param == 'boolean') {
      ret = param ? 1 : 0;
    } else {
      ret = param;
    }
    HEAP32[((params)>>2)] = ret;
  },

  // Sampler objects
  glGenSamplers: (n, samplers) => {
    GL.genObject(n, samplers, 'createSampler', GL.samplers
      );
  },

  glDeleteSamplers: (n, samplers) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((samplers)+(i*4))>>2)];
      var sampler = GL.samplers[id];
      if (!sampler) continue;
      GLctx.deleteSampler(sampler);
      sampler.name = 0;
      GL.samplers[id] = null;
    }
  },

  glIsSampler: (id) => {
    var sampler = GL.samplers[id];
    if (!sampler) return 0;
    return GLctx.isSampler(sampler);
  },

  glBindSampler: (unit, sampler) => {
    GLctx.bindSampler(unit, GL.samplers[sampler]);
  },

  glSamplerParameterf: (sampler, pname, param) => {
    GLctx.samplerParameterf(GL.samplers[sampler], pname, param);
  },

  glSamplerParameteri: (sampler, pname, param) => {
    GLctx.samplerParameteri(GL.samplers[sampler], pname, param);
  },

  glSamplerParameterfv: (sampler, pname, params) => {
    var param = HEAPF32[((params)>>2)];
    GLctx.samplerParameterf(GL.samplers[sampler], pname, param);
  },

  glSamplerParameteriv: (sampler, pname, params) => {
    var param = HEAP32[((params)>>2)];
    GLctx.samplerParameteri(GL.samplers[sampler], pname, param);
  },

  glGetSamplerParameterfv: (sampler, pname, params) => {
    if (!params) {
      // GLES3 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if p == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAPF32[((params)>>2)] = GLctx.getSamplerParameter(GL.samplers[sampler], pname);
  },

  glGetSamplerParameteriv: (sampler, pname, params) => {
    if (!params) {
      // GLES3 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if p == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    HEAP32[((params)>>2)] = GLctx.getSamplerParameter(GL.samplers[sampler], pname);
  },

  // Transform Feedback
  glGenTransformFeedbacks: (n, ids) => {
    GL.genObject(n, ids, 'createTransformFeedback', GL.transformFeedbacks
      );
  },

  glDeleteTransformFeedbacks: (n, ids) => {
    for (var i = 0; i < n; i++) {
      var id = HEAP32[(((ids)+(i*4))>>2)];
      var transformFeedback = GL.transformFeedbacks[id];
      if (!transformFeedback) continue; // GL spec: "unused names in ids are ignored, as is the name zero."
      GLctx.deleteTransformFeedback(transformFeedback);
      transformFeedback.name = 0;
      GL.transformFeedbacks[id] = null;
    }
  },

  glIsTransformFeedback: (id) => GLctx.isTransformFeedback(GL.transformFeedbacks[id]),

  glBindTransformFeedback: (target, id) => {
    GLctx.bindTransformFeedback(target, GL.transformFeedbacks[id]);
  },

  glTransformFeedbackVaryings: (program, count, varyings, bufferMode) => {
    program = GL.programs[program];
    var vars = [];
    for (var i = 0; i < count; i++)
      vars.push(UTF8ToString(HEAPU32[(((varyings)+(i*4))>>2)]));

    GLctx.transformFeedbackVaryings(program, vars, bufferMode);
  },

  glGetTransformFeedbackVarying: (program, index, bufSize, length, size, type, name) => {
    program = GL.programs[program];
    var info = GLctx.getTransformFeedbackVarying(program, index);
    if (!info) return; // If an error occurred, the return parameters length, size, type and name will be unmodified.

    if (name && bufSize > 0) {
      var numBytesWrittenExclNull = stringToUTF8(info.name, name, bufSize);
      if (length) HEAP32[((length)>>2)] = numBytesWrittenExclNull;
    } else {
      if (length) HEAP32[((length)>>2)] = 0;
    }

    if (size) HEAP32[((size)>>2)] = info.size;
    if (type) HEAP32[((type)>>2)] = info.type;
  },

  $emscriptenWebGLGetIndexed__deps: ['$writeI53ToI64'],
  $emscriptenWebGLGetIndexed: (target, index, data, type) => {
    if (!data) {
      // GLES2 specification does not specify how to behave if data is a null pointer. Since calling this function does not make sense
      // if data == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var result = GLctx.getIndexedParameter(target, index);
    var ret;
    switch (typeof result) {
      case 'boolean':
        ret = result ? 1 : 0;
        break;
      case 'number':
        ret = result;
        break;
      case 'object':
        if (result === null) {
          switch (target) {
            case 0x8C8F: // TRANSFORM_FEEDBACK_BUFFER_BINDING
            case 0x8A28: // UNIFORM_BUFFER_BINDING
              ret = 0;
              break;
            default: {
              GL.recordError(0x500); // GL_INVALID_ENUM
              return;
            }
          }
        } else if (result instanceof WebGLBuffer) {
          ret = result.name | 0;
        } else {
          GL.recordError(0x500); // GL_INVALID_ENUM
          return;
        }
        break;
      default:
        GL.recordError(0x500); // GL_INVALID_ENUM
        return;
    }

    switch (type) {
      case 1: writeI53ToI64(data, ret); break;
      case 0: HEAP32[((data)>>2)] = ret; break;
      case 2: HEAPF32[((data)>>2)] = ret; break;
      case 4: HEAP8[data] = ret ? 1 : 0; break;
      default: abort('internal emscriptenWebGLGetIndexed() error, bad type: ' + type);
    }
  },

  glGetIntegeri_v__deps: ['$emscriptenWebGLGetIndexed'],
  glGetIntegeri_v: (target, index, data) =>
    emscriptenWebGLGetIndexed(target, index, data, 0),

  glGetInteger64i_v__deps: ['$emscriptenWebGLGetIndexed'],
  glGetInteger64i_v: (target, index, data) =>
    emscriptenWebGLGetIndexed(target, index, data, 1),

  // Uniform Buffer objects
  glBindBufferBase: (target, index, buffer) => {
    GLctx.bindBufferBase(target, index, GL.buffers[buffer]);
  },

  glBindBufferRange: (target, index, buffer, offset, ptrsize) => {
    GLctx.bindBufferRange(target, index, GL.buffers[buffer], offset, ptrsize);
  },

  glGetUniformIndices: (program, uniformCount, uniformNames, uniformIndices) => {
    if (!uniformIndices) {
      // GLES2 specification does not specify how to behave if uniformIndices is a null pointer. Since calling this function does not make sense
      // if uniformIndices == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    if (uniformCount > 0 && (uniformNames == 0 || uniformIndices == 0)) {
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    program = GL.programs[program];
    var names = [];
    for (var i = 0; i < uniformCount; i++)
      names.push(UTF8ToString(HEAPU32[(((uniformNames)+(i*4))>>2)]));

    var result = GLctx.getUniformIndices(program, names);
    if (!result) return; // GL spec: If an error is generated, nothing is written out to uniformIndices.

    var len = result.length;
    for (var i = 0; i < len; i++) {
      HEAP32[(((uniformIndices)+(i*4))>>2)] = result[i];
    }
  },

  glGetActiveUniformsiv: (program, uniformCount, uniformIndices, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if params == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    if (uniformCount > 0 && uniformIndices == 0) {
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    program = GL.programs[program];
    var ids = [];
    for (var i = 0; i < uniformCount; i++) {
      ids.push(HEAP32[(((uniformIndices)+(i*4))>>2)]);
    }

    var result = GLctx.getActiveUniforms(program, ids, pname);
    if (!result) return; // GL spec: If an error is generated, nothing is written out to params.

    var len = result.length;
    for (var i = 0; i < len; i++) {
      HEAP32[(((params)+(i*4))>>2)] = result[i];
    }
  },

  glGetUniformBlockIndex: (program, uniformBlockName) => {
    return GLctx.getUniformBlockIndex(GL.programs[program], UTF8ToString(uniformBlockName));
  },

  glGetActiveUniformBlockiv: (program, uniformBlockIndex, pname, params) => {
    if (!params) {
      // GLES2 specification does not specify how to behave if params is a null pointer. Since calling this function does not make sense
      // if params == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    program = GL.programs[program];

    if (pname == 0x8A41 /* GL_UNIFORM_BLOCK_NAME_LENGTH */) {
      var name = GLctx.getActiveUniformBlockName(program, uniformBlockIndex);
      HEAP32[((params)>>2)] = name.length+1;
      return;
    }

    var result = GLctx.getActiveUniformBlockParameter(program, uniformBlockIndex, pname);
    if (result === null) return; // If an error occurs, nothing should be written to params.
    if (pname == 0x8A43 /*GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES*/) {
      for (var i = 0; i < result.length; i++) {
        HEAP32[(((params)+(i*4))>>2)] = result[i];
      }
    } else {
      HEAP32[((params)>>2)] = result;
    }
  },

  glGetActiveUniformBlockName: (program, uniformBlockIndex, bufSize, length, uniformBlockName) => {
    program = GL.programs[program];

    var result = GLctx.getActiveUniformBlockName(program, uniformBlockIndex);
    if (!result) return; // If an error occurs, nothing will be written to uniformBlockName or length.
    if (uniformBlockName && bufSize > 0) {
      var numBytesWrittenExclNull = stringToUTF8(result, uniformBlockName, bufSize);
      if (length) HEAP32[((length)>>2)] = numBytesWrittenExclNull;
    } else {
      if (length) HEAP32[((length)>>2)] = 0;
    }
  },

  glUniformBlockBinding: (program, uniformBlockIndex, uniformBlockBinding) => {
    program = GL.programs[program];

    GLctx.uniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
  },

  glClearBufferiv: (buffer, drawbuffer, value) => {

    GLctx.clearBufferiv(buffer, drawbuffer, HEAP32, ((value)>>2));
  },

  glClearBufferuiv: (buffer, drawbuffer, value) => {

    GLctx.clearBufferuiv(buffer, drawbuffer, HEAPU32, ((value)>>2));
  },

  glClearBufferfv: (buffer, drawbuffer, value) => {

    GLctx.clearBufferfv(buffer, drawbuffer, HEAPF32, ((value)>>2));
  },

  glFenceSync: (condition, flags) => {
    var sync = GLctx.fenceSync(condition, flags);
    if (sync) {
      var id = GL.getNewId(GL.syncs);
      sync.name = id;
      GL.syncs[id] = sync;
      return id;
    }
    return 0; // Failed to create a sync object
  },

  glDeleteSync: (id) => {
    if (!id) return;
    var sync = GL.syncs[id];
    if (!sync) { // glDeleteSync signals an error when deleting a nonexisting object, unlike some other GL delete functions.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    GLctx.deleteSync(sync);
    sync.name = 0;
    GL.syncs[id] = null;
  },

  glClientWaitSync: (sync, flags, timeout) => {
    // WebGL2 vs GLES3 differences: in GLES3, the timeout parameter is a uint64, where 0xFFFFFFFFFFFFFFFFULL means GL_TIMEOUT_IGNORED.
    // In JS, there's no 64-bit value types, so instead timeout is taken to be signed, and GL_TIMEOUT_IGNORED is given value -1.
    // Inherently the value accepted in the timeout is lossy, and can't take in arbitrary u64 bit pattern (but most likely doesn't matter)
    // See https://www.khronos.org/registry/webgl/specs/latest/2.0/#5.15
    timeout = Number(timeout);
    return GLctx.clientWaitSync(GL.syncs[sync], flags, timeout);
  },

  glWaitSync: (sync, flags, timeout) => {
    // See WebGL2 vs GLES3 difference on GL_TIMEOUT_IGNORED above (https://www.khronos.org/registry/webgl/specs/latest/2.0/#5.15)
    timeout = Number(timeout);
    GLctx.waitSync(GL.syncs[sync], flags, timeout);
  },

  glGetSynciv: (sync, pname, bufSize, length, values) => {
    if (bufSize < 0) {
      // GLES3 specification does not specify how to behave if bufSize < 0, however in the spec wording for glGetInternalformativ, it does say that GL_INVALID_VALUE should be raised,
      // so raise GL_INVALID_VALUE here as well.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    if (!values) {
      // GLES3 specification does not specify how to behave if values is a null pointer. Since calling this function does not make sense
      // if values == null, issue a GL error to notify user about it.
      GL.recordError(0x501 /* GL_INVALID_VALUE */);
      return;
    }
    var ret = GLctx.getSyncParameter(GL.syncs[sync], pname);
    if (ret !== null) {
      HEAP32[((values)>>2)] = ret;
      if (length) HEAP32[((length)>>2)] = 1; // Report a single value outputted.
    }
  },

  glIsSync: (sync) => GLctx.isSync(GL.syncs[sync]),

  glGetUniformuiv__deps: ['$emscriptenWebGLGetUniform'],
  glGetUniformuiv: (program, location, params) =>
    emscriptenWebGLGetUniform(program, location, params, 0),

  glGetFragDataLocation: (program, name) => {
    return GLctx.getFragDataLocation(GL.programs[program], UTF8ToString(name));
  },

  glGetVertexAttribIiv__deps: ['$emscriptenWebGLGetVertexAttrib'],
  glGetVertexAttribIiv: (index, pname, params) => {
    // N.B. This function may only be called if the vertex attribute was specified using the function glVertexAttribI4iv(),
    // otherwise the results are undefined. (GLES3 spec 6.1.12)
    emscriptenWebGLGetVertexAttrib(index, pname, params, 0);
  },

  // N.B. This function may only be called if the vertex attribute was specified using the function glVertexAttribI4uiv(),
  // otherwise the results are undefined. (GLES3 spec 6.1.12)
  glGetVertexAttribIuiv__deps: ['$emscriptenWebGLGetVertexAttrib'],
  glGetVertexAttribIuiv: 'glGetVertexAttribIiv',

  glUniform1ui__deps: ['$webglGetUniformLocation'],
  glUniform1ui: (location, v0) => {
    GLctx.uniform1ui(webglGetUniformLocation(location), v0);
  },

  glUniform2ui__deps: ['$webglGetUniformLocation'],
  glUniform2ui: (location, v0, v1) => {
    GLctx.uniform2ui(webglGetUniformLocation(location), v0, v1);
  },

  glUniform3ui__deps: ['$webglGetUniformLocation'],
  glUniform3ui: (location, v0, v1, v2) => {
    GLctx.uniform3ui(webglGetUniformLocation(location), v0, v1, v2);
  },

  glUniform4ui__deps: ['$webglGetUniformLocation'],
  glUniform4ui: (location, v0, v1, v2, v3) => {
    GLctx.uniform4ui(webglGetUniformLocation(location), v0, v1, v2, v3);
  },

  glUniform1uiv__deps: ['$webglGetUniformLocation'],
  glUniform1uiv: (location, count, value) => {
    count && GLctx.uniform1uiv(webglGetUniformLocation(location), HEAPU32, ((value)>>2), count);
  },

  glUniform2uiv__deps: ['$webglGetUniformLocation'],
  glUniform2uiv: (location, count, value) => {
    count && GLctx.uniform2uiv(webglGetUniformLocation(location), HEAPU32, ((value)>>2), count*2);
  },

  glUniform3uiv__deps: ['$webglGetUniformLocation'],
  glUniform3uiv: (location, count, value) => {
    count && GLctx.uniform3uiv(webglGetUniformLocation(location), HEAPU32, ((value)>>2), count*3);
  },

  glUniform4uiv__deps: ['$webglGetUniformLocation'],
  glUniform4uiv: (location, count, value) => {
    count && GLctx.uniform4uiv(webglGetUniformLocation(location), HEAPU32, ((value)>>2), count*4);
  },

  glUniformMatrix2x3fv__deps: ['$webglGetUniformLocation'],
  glUniformMatrix2x3fv: (location, count, transpose, value) => {
    count && GLctx.uniformMatrix2x3fv(webglGetUniformLocation(location), !!transpose, HEAPF32, ((value)>>2), count*6);
  },

  glUniformMatrix3x2fv__deps: ['$webglGetUniformLocation'],
  glUniformMatrix3x2fv: (location, count, transpose, value) => {
    count && GLctx.uniformMatrix3x2fv(webglGetUniformLocation(location), !!transpose, HEAPF32, ((value)>>2), count*6);
  },

  glUniformMatrix2x4fv__deps: ['$webglGetUniformLocation'],
  glUniformMatrix2x4fv: (location, count, transpose, value) => {
    count && GLctx.uniformMatrix2x4fv(webglGetUniformLocation(location), !!transpose, HEAPF32, ((value)>>2), count*8);
  },

  glUniformMatrix4x2fv__deps: ['$webglGetUniformLocation'],
  glUniformMatrix4x2fv: (location, count, transpose, value) => {
    count && GLctx.uniformMatrix4x2fv(webglGetUniformLocation(location), !!transpose, HEAPF32, ((value)>>2), count*8);
  },

  glUniformMatrix3x4fv__deps: ['$webglGetUniformLocation'],
  glUniformMatrix3x4fv: (location, count, transpose, value) => {
    count && GLctx.uniformMatrix3x4fv(webglGetUniformLocation(location), !!transpose, HEAPF32, ((value)>>2), count*12);
  },

  glUniformMatrix4x3fv__deps: ['$webglGetUniformLocation'],
  glUniformMatrix4x3fv: (location, count, transpose, value) => {
    count && GLctx.uniformMatrix4x3fv(webglGetUniformLocation(location), !!transpose, HEAPF32, ((value)>>2), count*12);
  },

  glVertexAttribI4iv: (index, v) => {
    GLctx.vertexAttribI4i(index, HEAP32[v>>2], HEAP32[v+4>>2], HEAP32[v+8>>2], HEAP32[v+12>>2]);
  },

  glVertexAttribI4uiv: (index, v) => {
    GLctx.vertexAttribI4ui(index, HEAPU32[v>>2], HEAPU32[v+4>>2], HEAPU32[v+8>>2], HEAPU32[v+12>>2]);
  },

  glProgramParameteri: (program, pname, value) => {
    GL.recordError(0x500/*GL_INVALID_ENUM*/);
  },

  glGetProgramBinary: (program, bufSize, length, binaryFormat, binary) => {
    GL.recordError(0x502/*GL_INVALID_OPERATION*/);
  },

  glProgramBinary: (program, binaryFormat, binary, length) => {
    GL.recordError(0x500/*GL_INVALID_ENUM*/);
  },

  glFramebufferTextureLayer: (target, attachment, texture, level, layer) => {
    GLctx.framebufferTextureLayer(target, attachment, GL.textures[texture], level, layer);
  },

  glVertexAttribIPointer: (index, size, type, stride, ptr) => {
    GLctx.vertexAttribIPointer(index, size, type, stride, ptr);
  },

  // Defined in library_glemu.js when LEGACY_GL_EMULATION is set
  glDrawRangeElements__deps: ['glDrawElements'],
  glDrawRangeElements: (mode, start, end, count, type, indices) => {
    // TODO: This should be a trivial pass-through function registered at the bottom of this page as
    // glFuncs[6][1] += ' drawRangeElements';
    // but due to https://bugzil.la/1202427,
    // we work around by ignoring the range.
    _glDrawElements(mode, count, type, indices);
  },

  glDrawArraysInstancedBaseInstanceWEBGL__sig: 'viiiii',
  glDrawArraysInstancedBaseInstanceWEBGL: (mode, first, count, instanceCount, baseInstance) => {
    GLctx.dibvbi['drawArraysInstancedBaseInstanceWEBGL'](mode, first, count, instanceCount, baseInstance);
  },
  glDrawArraysInstancedBaseInstance: 'glDrawArraysInstancedBaseInstanceWEBGL',
  glDrawArraysInstancedBaseInstanceANGLE: 'glDrawArraysInstancedBaseInstanceWEBGL',

  glDrawElementsInstancedBaseVertexBaseInstanceWEBGL__sig: 'viiiiiii',
  glDrawElementsInstancedBaseVertexBaseInstanceWEBGL: (mode, count, type, offset, instanceCount, baseVertex, baseinstance) => {
    GLctx.dibvbi['drawElementsInstancedBaseVertexBaseInstanceWEBGL'](mode, count, type, offset, instanceCount, baseVertex, baseinstance);
  },
  glDrawElementsInstancedBaseVertexBaseInstanceANGLE: 'glDrawElementsInstancedBaseVertexBaseInstanceWEBGL',

  $webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance: (ctx) =>
    // Closure is expected to be allowed to minify the '.dibvbi' property, so not accessing it quoted.
    !!(ctx.dibvbi = ctx.getExtension('WEBGL_draw_instanced_base_vertex_base_instance')),

  emscripten_webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance__deps: ['$webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance'],
  emscripten_webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance: (ctx) =>
    webgl_enable_WEBGL_draw_instanced_base_vertex_base_instance(GL.contexts[ctx].GLctx),

  glMultiDrawArraysInstancedBaseInstanceWEBGL__sig: 'viiiiii',
  glMultiDrawArraysInstancedBaseInstanceWEBGL: (mode, firsts, counts, instanceCounts, baseInstances, drawCount) => {
    GLctx.mdibvbi['multiDrawArraysInstancedBaseInstanceWEBGL'](
      mode,
      HEAP32,
      ((firsts)>>2),
      HEAP32,
      ((counts)>>2),
      HEAP32,
      ((instanceCounts)>>2),
      HEAPU32,
      ((baseInstances)>>2),
      drawCount);
  },
  glMultiDrawArraysInstancedBaseInstanceANGLE: 'glMultiDrawArraysInstancedBaseInstanceWEBGL',

  glMultiDrawElementsInstancedBaseVertexBaseInstanceWEBGL__sig: 'viiiiiiii',
  glMultiDrawElementsInstancedBaseVertexBaseInstanceWEBGL: (mode, counts, type, offsets, instanceCounts, baseVertices, baseInstances, drawCount) => {
    GLctx.mdibvbi['multiDrawElementsInstancedBaseVertexBaseInstanceWEBGL'](
      mode,
      HEAP32,
      ((counts)>>2),
      type,
      HEAP32,
      ((offsets)>>2),
      HEAP32,
      ((instanceCounts)>>2),
      HEAP32,
      ((baseVertices)>>2),
      HEAPU32,
      ((baseInstances)>>2),
      drawCount);
  },
  glMultiDrawElementsInstancedBaseVertexBaseInstanceANGLE: 'glMultiDrawElementsInstancedBaseVertexBaseInstanceWEBGL',

  $webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance: (ctx) => {
    // Closure is expected to be allowed to minify the '.mdibvbi' property, so not accessing it quoted.
    return !!(ctx.mdibvbi = ctx.getExtension('WEBGL_multi_draw_instanced_base_vertex_base_instance'));
  },

  emscripten_webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance__deps: ['$webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance'],
  emscripten_webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance: (ctx) =>
    webgl_enable_WEBGL_multi_draw_instanced_base_vertex_base_instance(GL.contexts[ctx].GLctx),
};

// Simple pass-through functions.
// - Starred ones have return values.
// - [X] ones have X in the C name but not in the JS name
var webgl2PassthroughFuncs = [
  [0, 'endTransformFeedback pauseTransformFeedback resumeTransformFeedback'],
  [1, 'beginTransformFeedback readBuffer endQuery'],
  [4, 'clearBufferfi'],
  [5, 'vertexAttribI4i vertexAttribI4ui copyBufferSubData texStorage2D renderbufferStorageMultisample'],
  [6, 'texStorage3D'],
  [9, 'copyTexSubImage3D'],
  [10, 'blitFramebuffer']
];

// If user passes -sMAX_WEBGL_VERSION >= 2 -sSTRICT but not -lGL (to link in
// WebGL 1), then WebGL2 library should not be linked in as well.
if (typeof createGLPassthroughFunctions == 'undefined') {
  error('In order to use WebGL 2 in strict mode with -sMAX_WEBGL_VERSION=2, you need to link in WebGL support with -lGL');
}

createGLPassthroughFunctions(LibraryWebGL2, webgl2PassthroughFuncs);

recordGLProcAddressGet(LibraryWebGL2);

addToLibrary(LibraryWebGL2);

