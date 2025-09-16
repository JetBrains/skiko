// This file is merged with skiko.mjs by emcc

const loadedWasm = {
    _: {}
}

let skikoGl = null;

export const awaitSkiko = loadSkikoWASM().then((module) => {
    loadedWasm._ = module.wasmExports;
    skikoGl = module.GL;
    return module
});

export const GL = new Proxy({}, {
    get(object, propName) {
        return skikoGl[propName];
    }
})


