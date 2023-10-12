// This file is merged with skiko.mjs by emcc

export async function loadAndInitSkikoWasm() {
    const skikoWasm = await loadSkikoWASM();

    // GL is defined by emcc
    window.GL = skikoWasm.GL;

    // _SkikoCallbacks is defined in skikoCallbacks.js
    window._SkikoCallbacks = _SkikoCallbacks;

    return skikoWasm;
}