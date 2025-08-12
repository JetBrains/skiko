// This file is merged with skiko.mjs by emcc

const loadedWasm = {
    _: {}
}

const GL = {
    createContext: () => {},
    makeContextCurrent: () => {}
};

export const awaitSkiko = loadSkikoWASM().then((module) => {
    loadedWasm._ = module.wasmExports;
    let {createContext, makeContextCurrent} = module.GL;
    GL.createContext = createContext;
    GL.makeContextCurrent = makeContextCurrent;
    return module
});

export {GL}