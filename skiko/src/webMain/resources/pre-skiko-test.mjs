import { awaitSkiko } from "./skiko.mjs";

const loadedWasm = {
    _: {}
}

loadedWasm._ = awaitSkiko.wasmExports;

for (let exportName of Object.keys(awaitSkiko.wasmExports)) {
    if (exportName.startsWith("org_jetbrains_skiko_tests_")) {
        window[exportName] = awaitSkiko.wasmExports[exportName]
    }
}

export const testApi = {
    testSetup: () => {}
}
