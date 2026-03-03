import { awaitSkiko } from "./skiko.mjs";

const loadedWasm = {
    _: {}
}

awaitSkiko.then((module) => {
    loadedWasm._ = module.wasmExports;

    for (let exportName of Object.keys(module.wasmExports)) {
        if (exportName.startsWith("org_jetbrains_skiko_tests_")) {
            window[exportName] = module.wasmExports[exportName]
        }
    }

    return module
});

export const api = {
    testSetup: () => {}
}
