// This file is merged with skiko.mjs by emcc

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
        // Mimic the wasm loader: route this side module through
        // locateFile so Emscripten does not fall back to a file:// scriptDirectory.
        const originalLocateFile = module.locateFile;
        module.locateFile = (path, prefix) => {
            if (path === extensionPath) return extensionPath;
            return originalLocateFile(path, prefix);
        };

        try {
            await module.loadDynamicLibrary(extensionPath, {
                loadAsync: true,
                global: true,
                nodelete: true,
                nodeJS: false
            });

            const sideModuleExports = module.LDSO.loadedLibsByName[extensionPath].exports;
            Object.assign(loadedWasm._, sideModuleExports);
        } finally {
            module.locateFile = originalLocateFile;
        }

    }).catch((error) => {
        extensionLoadPromises.delete(extensionPath);
        throw error;
    });

    extensionLoadPromises.set(extensionPath, loadPromise);
    return loadPromise;
};

const awaitSkikoCore = loadSkikoWASM().then((module) => {
    loadedWasm._ = module.wasmExports;
    skikoGl = module.GL;
    return module;
});

export const awaitSkiko = awaitSkikoCore.then(async (module) => {
    for (const callback of wasmReadyCallbacks) {
        await callback(module);
    }

    return module
});

export const GL = new Proxy({}, {
    get(object, propName) {
        return skikoGl[propName];
    }
})


