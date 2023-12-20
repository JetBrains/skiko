// This file is merged with skiko.mjs by emcc")

export const {
    _callCallback,
    _registerCallback,
    _releaseCallback,
    _createLocalCallbackScope,
    _releaseLocalCallbackScope
} = SkikoCallbacks;

export const loadedWasm = await loadSkikoWASM();

export const { GL } = loadedWasm;