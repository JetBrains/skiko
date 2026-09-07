// This file is used for loading skiko.wasm and provides the environment for it
import "./emscripten-compat.js";
import "./libwebgl.preprocessed.js";
import "./libwebgl2.preprocessed.js";

let wasmExports = null;

const createWasiImports = () => ({
    proc_exit: (code) => {
        console.log(`WASI exit with code ${code}`);
    },
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
});

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
                if (prop === '_callBooleanCallback') return (cb) => skikoApi._callCallback(cb).value ? 1 : 0;
                if (prop === '_callIntCallback') return (cb) => skikoApi._callCallback(cb).value;
                if (prop === '_callNativePointerCallback') return (cb) => skikoApi._callCallback(cb).value;
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
                            return gl[methodName](...args);
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
                if (prop === '__wasm_longjmp') return () => {
                    throw new Error('longjmp not supported');
                };
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
        wasi_snapshot_preview1: createWasiImports()
    };

    const {instance} = await WebAssembly.instantiateStreaming(response, importObject);
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
const loadedSideModules = new Map();
const dynamicSymbols = {};
const gotEntries = {};
const functionTableIndexes = new WeakMap();
let fallbackStackPointer = null;

const SIDE_MODULE_RUNTIME_ENV_IMPORTS = new Set([
    "__indirect_function_table",
    "__memory_base",
    "__stack_pointer",
    "__table_base",
    "memory",
]);

const alignTo = (value, alignment) => (value + alignment - 1) & ~(alignment - 1);

const readUnsignedLeb = (bytes, offsetRef) => {
    let result = 0;
    let shift = 0;
    while (true) {
        const byte = bytes[offsetRef.offset++];
        result |= (byte & 0x7f) << shift;
        if ((byte & 0x80) === 0) return result;
        shift += 7;
    }
};

const readDylinkString = (bytes, offsetRef) => {
    const length = readUnsignedLeb(bytes, offsetRef);
    const start = offsetRef.offset;
    offsetRef.offset += length;
    return new TextDecoder().decode(bytes.subarray(start, start + length));
};

const parseDylinkMetadata = (module) => {
    const sections = WebAssembly.Module.customSections(module, "dylink.0");
    if (sections.length === 0) {
        throw new Error("WASM side module does not contain a dylink.0 section");
    }

    const bytes = new Uint8Array(sections[0]);
    const offsetRef = { offset: 0 };
    const metadata = {
        memorySize: 0,
        memoryAlign: 0,
        tableSize: 0,
        tableAlign: 0,
        neededDynlibs: []
    };

    while (offsetRef.offset < bytes.length) {
        const subsectionType = bytes[offsetRef.offset++];
        const subsectionSize = readUnsignedLeb(bytes, offsetRef);
        const subsectionEnd = offsetRef.offset + subsectionSize;

        if (subsectionType === 1) {
            metadata.memorySize = readUnsignedLeb(bytes, offsetRef);
            metadata.memoryAlign = readUnsignedLeb(bytes, offsetRef);
            metadata.tableSize = readUnsignedLeb(bytes, offsetRef);
            metadata.tableAlign = readUnsignedLeb(bytes, offsetRef);
        } else if (subsectionType === 2) {
            const neededDynlibsCount = readUnsignedLeb(bytes, offsetRef);
            for (let i = 0; i < neededDynlibsCount; i++) {
                metadata.neededDynlibs.push(readDylinkString(bytes, offsetRef));
            }
        }

        offsetRef.offset = subsectionEnd;
    }

    return metadata;
};

const addFunctionToTable = (func) => {
    if (functionTableIndexes.has(func)) return functionTableIndexes.get(func);
    const index = wasmTable.length;
    wasmTable.grow(1);
    wasmTable.set(index, func);
    functionTableIndexes.set(func, index);
    return index;
};

const toAddress = (value, memoryBase = 0) => {
    if (typeof value === "function") return addFunctionToTable(value);
    if (value instanceof WebAssembly.Global) return value.value + memoryBase;
    if (typeof value === "number") return value;
    return 0;
};

const getGotEntry = (name) => {
    const symbolName = String(name);
    if (!gotEntries[symbolName]) {
        gotEntries[symbolName] = new WebAssembly.Global(
            { value: "i32", mutable: true },
            toAddress(dynamicSymbols[symbolName])
        );
    }
    return gotEntries[symbolName];
};

const updateGot = (exports) => {
    for (const [name, value] of Object.entries(exports)) {
        if (!gotEntries[name]) {
            gotEntries[name] = new WebAssembly.Global({ value: "i32", mutable: true }, 0);
        }
        gotEntries[name].value = toAddress(value);
    }
};

const relocateExports = (exports, memoryBase) => {
    const relocated = {};
    for (const [name, value] of Object.entries(exports)) {
        if (value instanceof WebAssembly.Global) {
            relocated[name] = value.value + memoryBase;
        } else {
            relocated[name] = value;
        }
    }
    return relocated;
};

const loadWasmSideModule = async (extensionPath) => {
    if (loadedSideModules.has(extensionPath)) return loadedSideModules.get(extensionPath);
    if (!wasmExports) throw new Error("Skiko core WASM must be loaded before loading side modules");
    if (!wasmTable) throw new Error("Skiko core WASM does not export __indirect_function_table");

    const response = await fetch(extensionPath);
    const bytes = await response.arrayBuffer();
    const wasmModule = await WebAssembly.compile(bytes);
    const metadata = parseDylinkMetadata(wasmModule);
    const moduleImports = WebAssembly.Module.imports(wasmModule);

    for (const needed of metadata.neededDynlibs) {
        await loadWasmSideModule(new URL(needed, extensionPath).href);
    }

    const memoryAlign = Math.pow(2, metadata.memoryAlign);
    const memoryBase = metadata.memorySize
        ? alignTo(wasmExports.malloc(metadata.memorySize + memoryAlign), memoryAlign)
        : 0;
    const tableBase = metadata.tableSize ? wasmTable.length : 0;
    if (metadata.tableSize) {
        wasmTable.grow(metadata.tableSize);
    }
    updateMemoryViews(wasmExports.memory.buffer);

    let moduleExports = {};
    const resolveSymbol = (prop) => dynamicSymbols[prop] ?? moduleExports[prop];
    const importProxy = new Proxy({}, {
        get(_, prop) {
            if (prop === "__memory_base") return new WebAssembly.Global({ value: "i32", mutable: false }, memoryBase);
            if (prop === "__table_base") return new WebAssembly.Global({ value: "i32", mutable: false }, tableBase);
            if (prop === "__stack_pointer") {
                fallbackStackPointer ||= new WebAssembly.Global({ value: "i32", mutable: true }, 0);
                return wasmExports.__stack_pointer ?? fallbackStackPointer;
            }
            if (prop === "memory") return wasmExports.memory;
            if (prop === "__indirect_function_table") return wasmTable;

            const resolved = resolveSymbol(prop);
            if (typeof resolved === "function") return resolved;

            return (...args) => {
                const deferred = resolveSymbol(prop);
                if (typeof deferred === "function") return deferred(...args);
                throw new Error(`Unresolved Skiko side-module import: ${String(prop)}`);
            };
        }
    });

    const importObject = {
        env: importProxy,
        wasi_snapshot_preview1: createWasiImports(),
        "GOT.func": new Proxy({}, { get: (_, prop) => getGotEntry(prop) }),
        "GOT.mem": new Proxy({}, { get: (_, prop) => getGotEntry(prop) })
    };

    const instance = await WebAssembly.instantiate(wasmModule, importObject);
    moduleExports = relocateExports(instance.exports, memoryBase);
    updateGot(moduleExports);
    Object.assign(dynamicSymbols, moduleExports);

    const unresolvedImports = moduleImports
        .filter(({ module, name }) =>
            (
                (module === "env" && !SIDE_MODULE_RUNTIME_ENV_IMPORTS.has(name)) ||
                module === "GOT.func" ||
                module === "GOT.mem"
            ) &&
            dynamicSymbols[name] === undefined &&
            moduleExports[name] === undefined
        )
        .map(({ module, name }) => `${module}.${name}`);
    if (unresolvedImports.length > 0) {
        throw new Error(`Unresolved Skiko side-module imports: ${unresolvedImports.join(", ")}`);
    }

    if (typeof moduleExports.__wasm_apply_data_relocs === "function") {
        moduleExports.__wasm_apply_data_relocs();
    }
    if (typeof moduleExports.__wasm_call_ctors === "function") {
        moduleExports.__wasm_call_ctors();
    }

    const loadedModule = { exports: moduleExports };
    loadedSideModules.set(extensionPath, loadedModule);
    return loadedModule;
};

export const loadSkikoExtension = (extensionPath) => {
    if (extensionLoadPromises.has(extensionPath)) return extensionLoadPromises.get(extensionPath);
    const loadPromise = awaitSkikoCore.then(async (module) => {
        const sideModuleExports = (await loadWasmSideModule(extensionPath)).exports;
        Object.assign(loadedWasm._, sideModuleExports);

    }).catch((error) => {
        extensionLoadPromises.delete(extensionPath);
        throw error;
    });

    extensionLoadPromises.set(extensionPath, loadPromise);
    return loadPromise;
};

const awaitSkikoCore = loadSkikoWASM().then((module) => {
    Object.assign(loadedWasm._, module.wasmExports);
    Object.assign(dynamicSymbols, module.wasmExports);
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
    await Promise.all(
        wasmReadyCallbacks.map(callback => callback(module))
    );

    return module
});

export const GL = new Proxy({}, {
    get(target, prop) {
        if (prop === 'createContext') {
            return (canvas, attr) => {
                const webGLCtx = canvas.getContext('webgl2', attr);
                if (webGLCtx) {
                    // Use Emscripten's registerContext to properly wrap the context
                    // with metadata (handle, version, GLctx) and init extensions.
                    const contextAttributes = {
                        majorVersion: 2,
                        enableExtensionsByDefault: true,
                    };
                    return _emscriptenGL.registerContext(webGLCtx, contextAttributes);
                }
                console.error("Failed to create WebGL2 context.");
                return 0;
            };
        }
        if (prop === 'makeContextCurrent') {
            return (contextId) => {
                // Delegate to Emscripten's makeContextCurrent which sets
                // GL.currentContext, GLctx, etc.
                return _emscriptenGL.makeContextCurrent(contextId);
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
