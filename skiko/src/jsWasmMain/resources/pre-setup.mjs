// This file is merged with skiko.mjs by emcc")

export const {
    _callCallback,
    _registerCallback,
    _releaseCallback,
    _createLocalCallbackScope,
    _releaseLocalCallbackScope
} = SkikoCallbacks;

// export const loadedWasm = await loadSkikoWASM();
//
// export const { GL } = loadedWasm;

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


