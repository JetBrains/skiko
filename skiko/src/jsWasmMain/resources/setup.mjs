// This file is merged with skiko.mjs by emcc
export const SkikoCallbacks = {
    _callCallback, _registerCallback, _releaseCallback, _createLocalCallbackScope, _releaseLocalCallbackScope
}

export async function loadAndInitSkikoWasm() {
    return await loadSkikoWASM();
}