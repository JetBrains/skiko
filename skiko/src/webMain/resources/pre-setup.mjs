// This file is merged with skiko.mjs by emcc

import { loadedSkikoExtensions, registeredSkikoExtensions } from "./skiko-extensions.mjs";

const loadSkikoExtension = (extensionPath, mainModule) =>
    mainModule.loadDynamicLibrary(extensionPath, {
        loadAsync: true,
        global: true,
        nodelete: true,
        nodeJS: false
    }).then(() => {
        const sideModuleExports = mainModule.LDSO.loadedLibsByName[extensionPath].exports;
        for (const [name, value] of Object.entries(sideModuleExports)) {
            if (!(('_' + name) in mainModule)) mainModule['_' + name] = value;
        }
        loadedSkikoExtensions.add(extensionPath);
        return sideModuleExports;
    });

export const awaitSkiko = await loadSkikoWASM().then(async (module) => {
    const originalLocateFile = module.locateFile;
    module.locateFile = (path, prefix) => {
        // If path is already an absolute URL, don't prepend scriptDirectory
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("blob:")) {
            return path;
        }
        return originalLocateFile(path, prefix);
    };
    if (!module.wasmExports.memory) {
        module.wasmExports.memory = {
            get buffer() {
                return module.HEAPU8.buffer;
            }
        };
    }

    const extensionPaths = [...new Set(registeredSkikoExtensions)];
    return Promise.all(extensionPaths.map((extensionPath) => loadSkikoExtension(extensionPath, module))).then(() => module)
});

export const {GL} = awaitSkiko;
