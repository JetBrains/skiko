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
    get(object, propName) {
        return skikoGl[propName];
    }
})


