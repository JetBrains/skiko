// This file is used for loading skiko.wasm and provides the environment for it
let gl = null;
const contexts = [null];
let wasmExports = null;

const resources = {
    textures: [null],
    buffers: [null],
    programs: [null],
    shaders: [null],
    framebuffers: [null],
    renderbuffers: [null],
    vertexarrays: [null],
    queries: [null],
    samplers: [null],
    transformfeedbacks: [null],
    syncs: [null],
    uniformblocks: [null],
    locations: [null]
};

const reverseResources = new Map();

function getResource(type, id) {
    return resources[type][id] || null;
}

function createResource(type, obj) {
    if (!obj) return 0;
    const id = resources[type].length;
    resources[type].push(obj);
    reverseResources.set(obj, { type, id });
    return id;
}

function toWebGLMethod(prop) {
    let name = prop.substring(2);
    return name.charAt(0).toLowerCase() + name.slice(1);
}

function getExtensions() {
    if (!gl) return [];
    let exts = (gl.getSupportedExtensions() || []).map(e => e.startsWith('GL_') ? e : 'GL_' + e);
    
    // WebGL 2 core features that were extensions in WebGL 1
    const version = gl.getParameter(0x1F02 /* VERSION */) || "";
    const isWebGL2 = version.indexOf("WebGL 2") !== -1;
    if (isWebGL2) {
        const coreExts = [
            "GL_OES_element_index_uint",
            "GL_OES_standard_derivatives",
            "GL_OES_texture_float",
            "GL_OES_texture_half_float",
            "GL_OES_texture_float_linear",
            "GL_OES_texture_half_float_linear",
            "GL_OES_vertex_array_object",
            "GL_WEBGL_depth_texture",
            "GL_EXT_sRGB",
            "GL_EXT_color_buffer_float",
            "GL_EXT_frag_depth",
            "GL_EXT_draw_buffers",
            "GL_EXT_shader_texture_lod",
            "GL_EXT_texture_filter_anisotropic"
        ];
        coreExts.forEach(ce => {
            if (exts.indexOf(ce) === -1) exts.push(ce);
        });
    }

    if (exts.length === 0) {
        return ["GL_OES_element_index_uint", "GL_OES_standard_derivatives", "GL_OES_texture_float", "GL_OES_vertex_array_object", "GL_EXT_texture_format_BGRA8888"];
    }
    return exts;
}

function readString(ptr) {
    if (!ptr) return "";
    let end = ptr;
    const view = new Uint8Array(wasmExports.memory.buffer);
    while (view[end] !== 0) end++;
    return new TextDecoder().decode(view.subarray(ptr, end));
}

const stringCache = new Map();
function getPtr(s) {
    if (!wasmExports) {
        console.error("getPtr called before wasmExports initialized");
        return 0;
    }
    if (stringCache.has(s)) return stringCache.get(s);
    const bytes = new TextEncoder().encode(s + "\0");
    const ptr = wasmExports.malloc(bytes.length);
    const buf = new Uint8Array(wasmExports.memory.buffer, ptr, bytes.length);
    buf.set(bytes);
    stringCache.set(s, ptr);
    return ptr;
}

async function loadSkikoWASM() {
    const url = new URL('./skiko.wasm', import.meta.url).href;
    const response = await fetch(url);

    const importObject = {
        env: new Proxy({}, {
            get(target, prop) {
                console.log("property: " + prop)
                if (prop === 'malloc') return (size) => wasmExports.malloc(size);
                if (prop === 'free') return (ptr) => wasmExports.free(ptr);
                
                // Callback support
                if (prop === '_releaseCallback') return (cb) => skikoApi._releaseCallback(cb);
                if (prop === '_callBooleanCallback') return (cb) => skikoApi._callCallback(cb) ? 1 : 0;
                if (prop === '_callIntCallback') return (cb) => skikoApi._callCallback(cb);
                if (prop === '_callNativePointerCallback') return (cb) => skikoApi._callCallback(cb);
                if (prop === '_callVoidCallback') return (cb) => skikoApi._callCallback(cb);

                // GL support
                if (prop === 'glGetString'|| prop === 'glGetStringi') {
                    return (name, index) => {
                        let s = "";
                        if (gl) {
                            if (name === 0x1F00 /* GL_VENDOR */) s = gl.getParameter(0x1F00) || "Skiko";
                            else if (name === 0x1F01 /* GL_RENDERER */) s = gl.getParameter(0x1F01) || "WebGL";
                            else if (name === 0x1F02 /* GL_VERSION */) {
                                let v = gl.getParameter(0x1F02) || "";
                                if (v.indexOf("WebGL 2") !== -1) s = "OpenGL ES 3.0 WebGL";
                                else s = "OpenGL ES 2.0 WebGL";
                            }
                            else if (name === 0x1F03 /* GL_EXTENSIONS */) {
                                if (prop === 'glGetStringi') {
                                    const exts = getExtensions();
                                    s = exts[index];
                                    if (s === undefined) return 0;
                                } else {
                                    s = getExtensions().join(' ');
                                }
                            }
                            else if (name === 0x8B8C /* GL_SHADING_LANGUAGE_VERSION */) {
                                let v = gl.getParameter(0x8B8C) || "";
                                if (v.indexOf("3.00") !== -1) s = "OpenGL ES GLSL ES 3.00";
                                else if (v.indexOf("1.00") !== -1) s = "OpenGL ES GLSL ES 1.00";
                                else s = v;
                            }
                        }
                        if (s === null) {
                            console.log(`getString(prop=${prop}, name=${name}, index=${index}) -> NULL`);
                            return 0;
                        }
                        if (!s && name === 0x1F02) s = "OpenGL ES 2.0 WebGL";
                        console.log(`getString(prop=${prop}, name=${name}, index=${index}) -> "${s}"`);
                        return getPtr(s);
                    };
                }

                if (prop === 'glGetIntegerv'||
                    prop === 'glGetFloatv' ||
                    prop === 'glGetBooleanv') {
                    return (pname, ptr) => {
                        if (!gl) return;
                        let val;
                        if (pname === 0x821D /* GL_NUM_EXTENSIONS */) {
                            val = getExtensions().length;
                        } else {
                            val = gl.getParameter(pname);
                        }
                        
                        const view = new DataView(wasmExports.memory.buffer);
                        if (val === null || val === undefined) val = 0;
                        
                        const write = (v, offset) => {
                            if (typeof v === 'object' && v !== null) {
                                const entry = reverseResources.get(v);
                                v = entry ? entry.id : 0;
                            }
                            if (prop.indexOf('Integer') !== -1) view.setInt32(ptr + offset, v, true);
                            else if (prop.indexOf('Float') !== -1) view.setFloat32(ptr + offset, v, true);
                            else view.setUint8(ptr + offset, v ? 1 : 0);
                        };

                        if (typeof val === 'number' || typeof val === 'boolean' || (typeof val === 'object' && val !== null && val.length === undefined)) {
                            write(val, 0);
                        } else if (val && typeof val.length === 'number' && typeof val !== 'string') {
                            for (let i = 0; i < val.length; i++) {
                                write(val[i], i * (prop.indexOf('Integer') !== -1 || prop.indexOf('Float') !== -1 ? 4 : 1));
                            }
                        }
                        console.log(`${prop}(pname=${pname}, ptr=${ptr}) -> ${val}`);
                    };
                }

                if (prop === 'glCreateProgram' || prop === 'glCreateShader') {
                    return (type) => {
                        if (!gl) return 0;
                        const obj = prop === 'glCreateProgram' ? gl.createProgram() : gl.createShader(type);
                        return createResource(prop === 'glCreateProgram' ? 'programs' : 'shaders', obj);
                    };
                }

                if (prop === 'glShaderSource') {
                    return (shaderId, count, stringPtr, lengthPtr) => {
                        const shader = getResource('shaders', shaderId);
                        if (!gl || !shader) return;
                        const view = new DataView(wasmExports.memory.buffer);
                        let source = "";
                        for (let i = 0; i < count; i++) {
                            const p = view.getUint32(stringPtr + i * 4, true);
                            const l = lengthPtr ? view.getInt32(lengthPtr + i * 4, true) : -1;
                            if (l < 0) {
                                source += readString(p);
                            } else {
                                const bytes = new Uint8Array(wasmExports.memory.buffer, p, l);
                                source += new TextDecoder().decode(bytes);
                            }
                        }
                        gl.shaderSource(shader, source);
                    };
                }

                if (prop === 'glGetProgramiv' || prop === 'glGetShaderiv') {
                    return (id, pname, ptr) => {
                        const type = prop.indexOf('Program') !== -1 ? 'programs' : 'shaders';
                        const obj = getResource(type, id);
                        if (!gl || !obj) return;
                        const method = prop.indexOf('Program') !== -1 ? 'getProgramParameter' : 'getShaderParameter';
                        let val = gl[method](obj, pname);
                        if (typeof val === 'boolean') val = val ? 1 : 0;
                        const view = new DataView(wasmExports.memory.buffer);
                        view.setInt32(ptr, val, true);
                    };
                }

                if (prop === 'glGetUniformLocation') {
                    return (programId, namePtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return -1;
                        const name = readString(namePtr);
                        const loc = gl.getUniformLocation(program, name);
                        return loc ? createResource('locations', loc) : -1;
                    };
                }

                if (prop === 'glUseProgram') {
                    return (id) => {
                        if (!gl) return;
                        gl.useProgram(getResource('programs', id));
                    };
                }

                if (prop === 'glAttachShader' || prop === 'glDetachShader') {
                    return (programId, shaderId) => {
                        if (!gl) return;
                        gl[toWebGLMethod(prop)](getResource('programs', programId), getResource('shaders', shaderId));
                    };
                }

                if (prop === 'glCompileShader' || prop === 'glLinkProgram' || prop === 'glValidateProgram') {
                    return (id) => {
                        if (!gl) return;
                        const type = prop.indexOf('Shader') !== -1 ? 'shaders' : 'programs';
                        gl[toWebGLMethod(prop)](getResource(type, id));
                    };
                }

                if (prop === 'glBindAttribLocation') {
                    return (programId, index, namePtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        gl.bindAttribLocation(program, index, readString(namePtr));
                    };
                }

                if (prop === 'glGetAttribLocation') {
                    return (programId, namePtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return -1;
                        return gl.getAttribLocation(program, readString(namePtr));
                    };
                }

                if (prop === 'glGetActiveAttrib' || prop === 'glGetActiveUniform') {
                    return (programId, index, bufSize, lengthPtr, sizePtr, typePtr, namePtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        const method = toWebGLMethod(prop);
                        const info = gl[method](program, index);
                        if (!info) return;
                        
                        const view = new DataView(wasmExports.memory.buffer);
                        if (sizePtr) view.setInt32(sizePtr, info.size, true);
                        if (typePtr) view.setUint32(typePtr, info.type, true);
                        
                        const encodedName = new TextEncoder().encode(info.name);
                        const actualLen = Math.min(encodedName.length, bufSize - 1);
                        const nameBuf = new Uint8Array(wasmExports.memory.buffer, namePtr, actualLen + 1);
                        nameBuf.set(encodedName.subarray(0, actualLen));
                        nameBuf[actualLen] = 0;
                        
                        if (lengthPtr) view.setInt32(lengthPtr, actualLen, true);
                    };
                }

                if (prop === 'glGetProgramInfoLog' || prop === 'glGetShaderInfoLog' || prop === 'glGetShaderSource') {
                    return (id, bufSize, lengthPtr, outPtr) => {
                        const type = prop.indexOf('Program') !== -1 ? 'programs' : 'shaders';
                        const obj = getResource(type, id);
                        if (!gl || !obj) return;
                        const method = toWebGLMethod(prop);
                        const s = gl[method](obj) || "";
                        
                        const encoded = new TextEncoder().encode(s);
                        const actualLen = Math.min(encoded.length, bufSize - 1);
                        const outBuf = new Uint8Array(wasmExports.memory.buffer, outPtr, actualLen + 1);
                        outBuf.set(encoded.subarray(0, actualLen));
                        outBuf[actualLen] = 0;
                        
                        if (lengthPtr) {
                            const view = new DataView(wasmExports.memory.buffer);
                            view.setInt32(lengthPtr, actualLen, true);
                        }
                    };
                }

                if (prop === 'glGetUniformBlockIndex') {
                    return (programId, namePtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return 0xFFFFFFFF;
                        return gl.getUniformBlockIndex(program, readString(namePtr));
                    };
                }

                if (prop === 'glUniformBlockBinding') {
                    return (programId, blockIndex, blockBinding) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        gl.uniformBlockBinding(program, blockIndex, blockBinding);
                    };
                }

                if (prop === 'glGetActiveUniformBlockName') {
                    return (programId, blockIndex, bufSize, lengthPtr, namePtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        const name = gl.getActiveUniformBlockName(program, blockIndex) || "";
                        
                        const encoded = new TextEncoder().encode(name);
                        const actualLen = Math.min(encoded.length, bufSize - 1);
                        const nameBuf = new Uint8Array(wasmExports.memory.buffer, namePtr, actualLen + 1);
                        nameBuf.set(encoded.subarray(0, actualLen));
                        nameBuf[actualLen] = 0;
                        
                        if (lengthPtr) {
                            const view = new DataView(wasmExports.memory.buffer);
                            view.setInt32(lengthPtr, actualLen, true);
                        }
                    };
                }

                if (prop === 'glGetActiveUniformBlockiv') {
                    return (programId, blockIndex, pname, paramsPtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        const val = gl.getActiveUniformBlockParameter(program, blockIndex, pname);
                        const view = new DataView(wasmExports.memory.buffer);
                        if (typeof val === 'number') {
                            view.setInt32(paramsPtr, val, true);
                        } else if (val && typeof val.length === 'number') {
                            for (let i = 0; i < val.length; i++) {
                                view.setInt32(paramsPtr + i * 4, val[i], true);
                            }
                        }
                    };
                }

                if (prop === 'glGetUniformIndices') {
                    return (programId, uniformCount, namesPtr, indicesPtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        const view = new DataView(wasmExports.memory.buffer);
                        const names = [];
                        for (let i = 0; i < uniformCount; i++) {
                            const ptr = view.getUint32(namesPtr + i * 4, true);
                            names.push(readString(ptr));
                        }
                        const indices = gl.getUniformIndices(program, names);
                        for (let i = 0; i < indices.length; i++) {
                            view.setUint32(indicesPtr + i * 4, indices[i], true);
                        }
                    };
                }

                if (prop === 'glGetActiveUniformsiv') {
                    return (programId, uniformCount, indicesPtr, pname, paramsPtr) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        const view = new DataView(wasmExports.memory.buffer);
                        const indices = new Uint32Array(wasmExports.memory.buffer, indicesPtr, uniformCount);
                        const res = gl.getActiveUniforms(program, indices, pname);
                        for (let i = 0; i < res.length; i++) {
                            view.setInt32(paramsPtr + i * 4, res[i], true);
                        }
                    };
                }

                if (prop === 'glTransformFeedbackVaryings') {
                    return (programId, count, varyingsPtr, bufferMode) => {
                        const program = getResource('programs', programId);
                        if (!gl || !program) return;
                        const view = new DataView(wasmExports.memory.buffer);
                        const varyings = [];
                        for (let i = 0; i < count; i++) {
                            const ptr = view.getUint32(varyingsPtr + i * 4, true);
                            varyings.push(readString(ptr));
                        }
                        gl.transformFeedbackVaryings(program, varyings, bufferMode);
                    };
                }

                const genMatch = prop.match(/^glGen([A-Z][a-zA-Z]+)s$/);
                if (genMatch) {
                    let typeName = genMatch[1];
                    if (typeName === 'Querie') typeName = 'Query';
                    const type = typeName.toLowerCase() + (typeName === 'Query' ? 'ies' : 's');
                    if (resources[type]) {
                        const methodName = "create" + typeName;
                        return (n, ptr) => {
                            if (!gl) return;
                            const view = new DataView(wasmExports.memory.buffer);
                            for (let i = 0; i < n; i++) {
                                const obj = gl[methodName]();
                                const id = createResource(type, obj);
                                view.setUint32(ptr + i * 4, id, true);
                            }
                        };
                    }
                }

                if (prop === 'glBindBufferBase') {
                    return (target, index, id) => {
                        if (!gl || typeof gl.bindBufferBase !== 'function') return;
                        gl.bindBufferBase(target, index, getResource('buffers', id));
                    };
                }
                if (prop === 'glBindBufferRange') {
                    return (target, index, id, offset, size) => {
                        if (!gl || typeof gl.bindBufferRange !== 'function') return;
                        gl.bindBufferRange(target, index, getResource('buffers', id), offset, size);
                    };
                }

                if (prop === 'glBindVertexArray') {
                    return (id) => {
                        if (!gl || typeof gl.bindVertexArray !== 'function') return;
                        gl.bindVertexArray(getResource('vertexarrays', id));
                    };
                }

                const bindMatch = prop.match(/^glBind([A-Z][a-zA-Z]+)$/);
                if (bindMatch) {
                    let typeName = bindMatch[1];
                    const type = typeName.toLowerCase() + (typeName === 'Query' ? 'ies' : 's');
                    if (resources[type]) {
                        const methodName = "bind" + typeName;
                        return (target, id) => {
                            if (!gl) return;
                            gl[methodName](target, getResource(type, id));
                        };
                    }
                }

                const deleteMatch = prop.match(/^glDelete([A-Z][a-zA-Z]+)s$/);
                if (deleteMatch) {
                    let typeName = deleteMatch[1];
                    if (typeName === 'Querie') typeName = 'Query';
                    const type = typeName.toLowerCase() + (typeName === 'Query' ? 'ies' : 's');
                    if (resources[type]) {
                        const methodName = "delete" + typeName;
                        return (n, ptr) => {
                            if (!gl) return;
                            const view = new DataView(wasmExports.memory.buffer);
                            for (let i = 0; i < n; i++) {
                                const id = view.getUint32(ptr + i * 4, true);
                                const obj = getResource(type, id);
                                if (obj) {
                                    gl[methodName](obj);
                                    resources[type][id] = null;
                                }
                            }
                        };
                    }
                }

                if (prop === 'glFramebufferTexture2D') {
                    return (target, attachment, textarget, textureId, level) => {
                        if (!gl) return;
                        gl.framebufferTexture2D(target, attachment, textarget, getResource('textures', textureId), level);
                    };
                }
                if (prop === 'glFramebufferRenderbuffer') {
                    return (target, attachment, renderbuffertarget, rbId) => {
                        if (!gl) return;
                        gl.framebufferRenderbuffer(target, attachment, renderbuffertarget, getResource('renderbuffers', rbId));
                    };
                }

                if (prop.startsWith('glUniform')) {
                    return (locId, ...args) => {
                        if (!gl) return;
                        const loc = getResource('locations', locId);
                        if (!loc) return;
                        
                        let methodName = prop.substring(2);
                        methodName = methodName.charAt(0).toLowerCase() + methodName.slice(1);

                        // Handle Uniform*v variants which take a pointer to data
                        if (prop.endsWith('v')) {
                            if (prop.indexOf('Matrix') !== -1) {
                                // C: (location, count, transpose, ptr)
                                // args: (count, transpose, ptr)
                                const count = args[0];
                                const transpose = !!args[1];
                                const ptr = args[2];
                                const elements = (prop.indexOf('2') !== -1 ? 4 : prop.indexOf('3') !== -1 ? 9 : 16);
                                const data = new Float32Array(wasmExports.memory.buffer, ptr, count * elements);
                                gl[methodName](loc, transpose, data);
                            } else {
                                // C: (location, count, ptr)
                                // args: (count, ptr)
                                const count = args[0];
                                const ptr = args[1];
                                const elementCount = prop.indexOf('1') !== -1 ? 1 : prop.indexOf('2') !== -1 ? 2 : prop.indexOf('3') !== -1 ? 3 : 4;
                                let data;
                                if (prop.indexOf('f') !== -1) {
                                    data = new Float32Array(wasmExports.memory.buffer, ptr, count * elementCount);
                                } else {
                                    data = new Int32Array(wasmExports.memory.buffer, ptr, count * elementCount);
                                }
                                gl[methodName](loc, data);
                            }
                            return;
                        }
                        
                        gl[methodName](loc, ...args);
                    };
                }

                if (prop === 'glDeleteProgram' || prop === 'glDeleteShader') {
                    return (id) => {
                        if (!gl) return;
                        const type = prop.indexOf('Program') !== -1 ? 'programs' : 'shaders';
                        const obj = getResource(type, id);
                        if (obj) {
                            gl[toWebGLMethod(prop)](obj);
                            resources[type][id] = null;
                            reverseResources.delete(obj);
                        }
                    };
                }

                if (prop === 'glBufferData') {
                    return (target, size, data, usage) => {
                        if (!gl) return;
                        if (data === 0) {
                            gl.bufferData(target, size, usage);
                        } else {
                            gl.bufferData(target, new Uint8Array(wasmExports.memory.buffer, data, size), usage);
                        }
                    };
                }

                if (prop === 'glBufferSubData') {
                    return (target, offset, size, data) => {
                        if (!gl) return;
                        gl.bufferSubData(target, offset, new Uint8Array(wasmExports.memory.buffer, data, size));
                    };
                }


                if (prop === 'glCompressedTexImage2D') {
                    return (target, level, internalformat, width, height, border, imageSize, data) => {
                        if (!gl) return;
                        if (data === 0) {
                            gl.compressedTexImage2D(target, level, internalformat, width, height, border, imageSize, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.compressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
                            } else {
                                gl.compressedTexImage2D(target, level, internalformat, width, height, border, new Uint8Array(wasmExports.memory.buffer, data, imageSize));
                            }
                        }
                    };
                }

                if (prop === 'glCompressedTexSubImage2D') {
                    return (target, level, xoffset, yoffset, width, height, format, imageSize, data) => {
                        if (!gl) return;
                        if (data === 0) {
                            gl.compressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.compressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
                            } else {
                                gl.compressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, new Uint8Array(wasmExports.memory.buffer, data, imageSize));
                            }
                        }
                    };
                }

                if (prop === 'glInvalidateFramebuffer') {
                    return (target, n, ptr) => {
                        if (!gl || typeof gl.invalidateFramebuffer !== 'function') return;
                        const attachments = new Uint32Array(wasmExports.memory.buffer, ptr, n);
                        gl.invalidateFramebuffer(target, attachments);
                    };
                }

                if (prop === 'glTexImage3D') {
                    return (target, level, internalformat, width, height, depth, border, format, type, pixels) => {
                        if (!gl || typeof gl.texImage3D !== 'function') return;
                        if (pixels === 0) {
                            gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
                            } else {
                                gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, new Uint8Array(wasmExports.memory.buffer, pixels));
                            }
                        }
                    };
                }

                if (prop === 'glTexSubImage3D') {
                    return (target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels) => {
                        if (!gl || typeof gl.texSubImage3D !== 'function') return;
                        if (pixels === 0) {
                            gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
                            } else {
                                gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, new Uint8Array(wasmExports.memory.buffer, pixels));
                            }
                        }
                    };
                }

                if (prop === 'glCompressedTexImage3D') {
                    return (target, level, internalformat, width, height, depth, border, imageSize, data) => {
                        if (!gl || typeof gl.compressedTexImage3D !== 'function') return;
                        if (data === 0) {
                            gl.compressedTexImage3D(target, level, internalformat, width, height, depth, border, imageSize, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.compressedTexImage3D(target, level, internalformat, width, height, depth, border, imageSize, data);
                            } else {
                                gl.compressedTexImage3D(target, level, internalformat, width, height, depth, border, new Uint8Array(wasmExports.memory.buffer, data, imageSize));
                            }
                        }
                    };
                }

                if (prop === 'glCompressedTexSubImage3D') {
                    return (target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data) => {
                        if (!gl || typeof gl.compressedTexSubImage3D !== 'function') return;
                        if (data === 0) {
                            gl.compressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.compressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data);
                            } else {
                                gl.compressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, new Uint8Array(wasmExports.memory.buffer, data, imageSize));
                            }
                        }
                    };
                }

                if (prop === 'glFenceSync') {
                    return (condition, flags) => {
                        if (!gl || typeof gl.fenceSync !== 'function') return 0;
                        return createResource('syncs', gl.fenceSync(condition, flags));
                    };
                }
                if (prop === 'glWaitSync') {
                    return (id, flags, timeout) => {
                        if (!gl || typeof gl.waitSync !== 'function') return;
                        gl.waitSync(getResource('syncs', id), flags, timeout);
                    };
                }
                if (prop === 'glClientWaitSync') {
                    return (id, flags, timeout) => {
                        if (!gl || typeof gl.clientWaitSync !== 'function') return 0;
                        return gl.clientWaitSync(getResource('syncs', id), flags, timeout);
                    };
                }
                if (prop === 'glDeleteSync') {
                    return (id) => {
                        if (!gl || typeof gl.deleteSync !== 'function') return;
                        const obj = getResource('syncs', id);
                        if (obj) {
                            gl.deleteSync(obj);
                            resources.syncs[id] = null;
                        }
                    };
                }

                if (prop === 'glBeginQuery' || prop === 'glEndQuery') {
                    return (target, id) => {
                        if (!gl || (prop === 'glBeginQuery' && typeof gl.beginQuery !== 'function') || (prop === 'glEndQuery' && typeof gl.endQuery !== 'function')) return;
                        if (prop === 'glBeginQuery') {
                            gl.beginQuery(target, getResource('queries', id));
                        } else {
                            gl.endQuery(target);
                        }
                    };
                }

                if (prop === 'glTexImage2D') {
                    return (target, level, internalformat, width, height, border, format, type, pixels) => {
                        if (!gl) return;
                        if (pixels === 0) {
                            gl.texImage2D(target, level, internalformat, width, height, border, format, type, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.texImage2D(target, level, internalformat, width, height, border, format, type, pixels);
                            } else {
                                gl.texImage2D(target, level, internalformat, width, height, border, format, type, new Uint8Array(wasmExports.memory.buffer, pixels));
                            }
                        }
                    };
                }

                if (prop === 'glTexSubImage2D') {
                    return (target, level, xoffset, yoffset, width, height, format, type, pixels) => {
                        if (!gl) return;
                        if (pixels === 0) {
                            gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, null);
                        } else {
                            const pbo = gl.getParameter(0x8896 /* GL_PIXEL_UNPACK_BUFFER_BINDING */);
                            if (pbo) {
                                gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
                            } else {
                                gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, new Uint8Array(wasmExports.memory.buffer, pixels));
                            }
                        }
                    };
                }

                if (prop === 'glReadPixels') {
                    return (x, y, width, height, format, type, pixels) => {
                        if (!gl) return;
                        const pbo = gl.getParameter(0x88ED /* GL_PIXEL_PACK_BUFFER_BINDING */);
                        if (pbo) {
                            gl.readPixels(x, y, width, height, format, type, pixels);
                        } else {
                            gl.readPixels(x, y, width, height, format, type, new Uint8Array(wasmExports.memory.buffer, pixels));
                        }
                    };
                }

                if (prop === 'glDrawBuffers') {
                    return (n, bufsPtr) => {
                        if (!gl || typeof gl.drawBuffers !== 'function') return;
                        const bufs = new Uint32Array(wasmExports.memory.buffer, bufsPtr, n);
                        gl.drawBuffers(bufs);
                    };
                }

                if (prop.startsWith('gl')) {
                    let glProp = prop.substring(2);
                    let methodName = glProp.charAt(0).toLowerCase() + glProp.slice(1);
                    return (...args) => {
                        if (gl && typeof gl[methodName] === 'function') {
                            const res = gl[methodName](...args);
                            if (prop !== 'glClear' && prop !== 'glFlush' && prop !== 'glFinish' && prop !== 'glViewport' && prop !== 'glScissor') {
                                console.log(`GL call: ${prop}(${args.join(', ')}) -> ${res}`);
                            }
                            return res;
                        }
                        console.warn(`Unimplemented Skiko GL call: ${prop} -> ${methodName}`, args);
                        return 0;
                    };
                }

                // System stubs
                if (prop === 'sem_init' || prop === 'sem_destroy' || prop === 'sem_post' || prop === 'sem_wait') return () => 0;
                if (prop === 'mmap') return () => 0;
                if (prop === 'munmap') return () => 0;
                if (prop === 'getpid') return () => 1;
                if (prop === 'fiprintf' || prop === '__small_fprintf') return () => 0;
                if (prop === '__wasm_longjmp') return () => { throw new Error('longjmp not supported'); };
                if (prop === '__wasm_setjmp' || prop === '__wasm_setjmp_test') return () => 0;

                return (...args) => {
                    if (wasmExports && wasmExports[prop]) {
                        return wasmExports[prop](...args);
                    }
                    console.warn(`Unimplemented Skiko import: env.${prop}`, args);
                    return 0;
                };
            }
        }),
        wasi_snapshot_preview1: {
            proc_exit: (code) => { console.log(`WASI exit with code ${code}`); },
            fd_write: (fd, iovs, iovs_len, nwritten_ptr) => {
                const view = new DataView(wasmExports.memory.buffer);
                let written = 0;
                let out = "";
                for (let i = 0; i < iovs_len; i++) {
                    const ptr = view.getUint32(iovs + i * 8, true);
                    const len = view.getUint32(iovs + i * 8 + 4, true);
                    const buf = new Uint8Array(wasmExports.memory.buffer, ptr, len);
                    out += new TextDecoder().decode(buf);
                    written += len;
                }
                if (fd === 1) console.log(out);
                else if (fd === 2) console.error(out);
                view.setUint32(nwritten_ptr, written, true);
                return 0;
            },
            clock_time_get: (id, precision, time_ptr) => {
                const view = new DataView(wasmExports.memory.buffer);
                view.setBigUint64(time_ptr, BigInt(Math.floor(performance.now() * 1000000)), true);
                return 0;
            },
            clock_res_get: (id, res_ptr) => {
                const view = new DataView(wasmExports.memory.buffer);
                view.setBigUint64(res_ptr, 1000000n, true);
                return 0;
            },
            args_sizes_get: (count_ptr, size_ptr) => {
                const view = new DataView(wasmExports.memory.buffer);
                view.setUint32(count_ptr, 0, true);
                view.setUint32(size_ptr, 0, true);
                return 0;
            },
            args_get: (argv_ptr, argv_buf_ptr) => {
                return 0;
            },
            environ_sizes_get: (count_ptr, size_ptr) => {
                const view = new DataView(wasmExports.memory.buffer);
                view.setUint32(count_ptr, 0, true);
                view.setUint32(size_ptr, 0, true);
                return 0;
            },
            environ_get: (environ_ptr, environ_buf_ptr) => {
                return 0;
            },
            random_get: (buf_ptr, buf_len) => {
                const buf = new Uint8Array(wasmExports.memory.buffer, buf_ptr, buf_len);
                crypto.getRandomValues(buf);
                return 0;
            },
            fd_close: (fd) => 0,
            fd_read: () => 52,
            fd_pread: () => 52,
            fd_pwrite: () => 52,
            fd_readdir: () => 52,
            fd_seek: (fd, offset, whence, newoffset_ptr) => 0,
            fd_tell: () => 52,
            fd_sync: () => 52,
            fd_datasync: () => 52,
            fd_renumber: () => 52,
            fd_allocate: () => 52,
            fd_advise: () => 52,
            fd_filestat_get: () => 52,
            fd_filestat_set_size: () => 52,
            fd_filestat_set_times: () => 52,
            fd_fdstat_get: (fd, stat_ptr) => 0,
            fd_fdstat_set_flags: () => 52,
            fd_fdstat_set_rights: () => 52,
            fd_prestat_get: (fd, prestat_ptr) => 8, // 8 = __WASI_ERRNO_BADF
            fd_prestat_dir_name: (fd, path_ptr, path_len) => 8,
            path_create_directory: () => 52,
            path_filestat_get: () => 52,
            path_filestat_set_times: () => 52,
            path_link: () => 52,
            path_open: () => 52,
            path_readlink: () => 52,
            path_remove_directory: () => 52,
            path_rename: () => 52,
            path_symlink: () => 52,
            path_unlink_file: () => 52,
            poll_oneoff: () => 52,
            sched_yield: () => 0,
            sock_accept: () => 52,
            sock_recv: () => 52,
            sock_send: () => 52,
            sock_shutdown: () => 52,
        }
    };
    
    const { instance } = await WebAssembly.instantiateStreaming(response, importObject);
    wasmExports = instance.exports;
    
    // Initialize WASI if needed
    if (wasmExports._initialize) {
        wasmExports._initialize();
    } else if (wasmExports.__wasm_call_ctors) {
        wasmExports.__wasm_call_ctors();
    } else if (wasmExports._start) {
        // We avoid calling _start() as it might call an unreachable main in reactor modules.
        // Some WASI modules use _start for initialization, but Skiko is a library.
    }

    return {
        wasmExports: wasmExports,
        GL: GL
    };
}

export const loadedWasm = {
    _: {}
}

let skikoGl = null;
const wasmReadyCallbacks = [];

export const registerSkikoWasmReadyCallback = (callback) => {
    wasmReadyCallbacks.push(callback);
};

const extensionLoadPromises = new Map();

export const loadSkikoExtension = (extensionPath) => {
    if (extensionLoadPromises.has(extensionPath)) return extensionLoadPromises.get(extensionPath);
    const loadPromise = awaitSkikoCore.then(async (module) => {
        await module.loadDynamicLibrary(extensionPath, {
            loadAsync: true,
            global: true,
            nodelete: true,
            nodeJS: false
        });

        const sideModuleExports = module.LDSO.loadedLibsByName[extensionPath].exports;
        Object.assign(loadedWasm._, sideModuleExports);

    }).catch((error) => {
        extensionLoadPromises.delete(extensionPath);
        throw error;
    });

    extensionLoadPromises.set(extensionPath, loadPromise);
    return loadPromise;
};

const awaitSkikoCore = loadSkikoWASM().then((module) => {
    const originalLocateFile = module.locateFile;

    module.locateFile = (path, prefix) => {
        // If path is already an absolute URL, don't prepend scriptDirectory
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("blob:")) {
            return path;
        }
        return originalLocateFile(path, prefix);
    };
    loadedWasm._ = module.wasmExports;
    if (!module.wasmExports.memory) {
        module.wasmExports.memory = {
            get buffer() {
                return module.HEAPU8.buffer;
            }
        };
    }
    skikoGl = module.GL;
    return module;
});

export const awaitSkiko = awaitSkikoCore.then(async (module) => {
    await Promise.allSettled(
        wasmReadyCallbacks.map(callback => callback(module))
    );

    return module
});

export const GL = new Proxy({}, {
    get(target, prop) {
        if (prop === 'createContext') {
            return (canvas, attr) => {
                const newGl = canvas.getContext('webgl2', attr) || canvas.getContext('webgl', attr);
                if (newGl) {
                    const id = contexts.length;
                    contexts.push(newGl);
                    // gl = newGl;
                    console.log("WebGL context created successfully, id:", id);
                    return id;
                }
                console.error("Failed to create any WebGL context.");
                return 0;
            };
        }
        if (prop === 'makeContextCurrent') {
            return (contextId) => {
                const newGl = contexts[contextId];
                if (newGl) {
                    gl = newGl;
                    console.log("Setting WebGL context:", contextId);
                    return true;
                }
                console.error("Failed to set WebGL context:", contextId);
                return false;
            };
        }
        return (...args) => {
            console.log("GL call:", prop, args);
            let methodName = prop;
            if (prop.startsWith('gl')) {
                methodName = prop.substring(2);
                methodName = methodName.charAt(0).toLowerCase() + methodName.slice(1);
            }
            if (gl && typeof gl[methodName] === 'function') {
                return gl[methodName](...args);
            }
            if (!gl) {
                console.warn(`GL call while gl is null: ${prop}`);
            } else {
                console.warn(`Unimplemented Skiko GL call on JS side: ${prop}`, args);
            }
        };
    }
})


