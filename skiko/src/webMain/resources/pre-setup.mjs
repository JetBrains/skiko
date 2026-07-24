// This file is used for loading skiko.wasm and provides the environment for it
import "./emscripten-compat.js";
import "./libwebgl.preprocessed.js";
import "./libwebgl2.preprocessed.js";

let wasmExports = null;

async function loadSkikoWASM() {
    const url = new URL('./skiko.wasm', import.meta.url).href;
    const response = await fetch(url);

    const importObject = {
        env: new Proxy({}, {
            get(target, prop) {
                if (prop === 'malloc') return (size) => wasmExports.malloc(size);
                if (prop === 'free') return (ptr) => wasmExports.free(ptr);
                
                // Callback support
                if (prop === '_releaseCallback') return (cb) => skikoApi._releaseCallback(cb);
                if (prop === '_callBooleanCallback') return (cb) => skikoApi._callCallback(cb) ? 1 : 0;
                if (prop === '_callIntCallback') return (cb) => skikoApi._callCallback(cb);
                if (prop === '_callNativePointerCallback') return (cb) => skikoApi._callCallback(cb);
                if (prop === '_callVoidCallback') return (cb) => skikoApi._callCallback(cb);

                // Emscripten memory growth notification — refresh HEAP views
                if (prop === 'emscripten_notify_memory_growth') return (memoryIndex) => {
                    updateMemoryViews(wasmExports.memory.buffer);
                };

                // GL support — look up in the merged library, resolving string aliases
                var glFunc = LibraryManager.library[prop];
                // Resolve string aliases (recordGLProcAddressGet sets glX = 'emscripten_glX')
                while (typeof glFunc === 'string') {
                    glFunc = LibraryManager.library[glFunc];
                }
                if (typeof glFunc === 'function') {
                    return (...args) => {
                        // Refresh HEAP views in case wasm memory grew since last call
                        updateMemoryViews(wasmExports.memory.buffer);
                        return glFunc(...args);
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

    // Initialize Emscripten runtime: HEAP views, $-prefixed globals, and wasmTable
    updateMemoryViews(wasmExports.memory.buffer);
    if (wasmExports.__indirect_function_table) {
        wasmTable = wasmExports.__indirect_function_table;
    }
    extractEmscriptenGlobals();


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
    loadedWasm._ = module.wasmExports;
    if (!module.wasmExports.memory) {
        module.wasmExports.memory = {
            get buffer() {
                return module.HEAPU8.buffer;
            }
        };
    }
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
                const webGLCtx = canvas.getContext('webgl2', attr) || canvas.getContext('webgl', attr);
                if (webGLCtx) {
                    // Use Emscripten's registerContext to properly wrap the context
                    // with metadata (handle, version, GLctx) and init extensions.
                    var contextAttributes = {
                        majorVersion: webGLCtx instanceof WebGL2RenderingContext ? 2 : 1,
                        enableExtensionsByDefault: true,
                    };
                    var handle = _emscriptenGL.registerContext(webGLCtx, contextAttributes);
                    console.log("WebGL context created and registered, handle:", handle);
                    return handle;
                }
                console.error("Failed to create any WebGL context.");
                return 0;
            };
        }
        if (prop === 'makeContextCurrent') {
            return (contextId) => {
                // Delegate to Emscripten's makeContextCurrent which sets
                // GL.currentContext, GLctx, etc.
                var result = _emscriptenGL.makeContextCurrent(contextId);
                if (result) {
                    console.log("Setting WebGL context:", contextId);
                }
                return result;
            };
        }
        // Delegate to the Emscripten $GL object for all other properties
        // (genObject, buffers, textures, contexts, currentContext, etc.)
        if (_emscriptenGL && prop in _emscriptenGL) {
            return _emscriptenGL[prop];
        }
        return undefined;
    },
    set(target, prop, value) {
        // Forward property writes to the Emscripten $GL object
        if (_emscriptenGL) {
            _emscriptenGL[prop] = value;
        }
        return true;
    }
})


