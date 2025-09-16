import { awaitSkiko } from "./skiko.mjs";

const loadedWasm = {
    _: {}
}

awaitSkiko.then((module) => {
    loadedWasm._ = module.wasmExports;
    return module
});

export const api = {
    testSetup: () => {}
}
