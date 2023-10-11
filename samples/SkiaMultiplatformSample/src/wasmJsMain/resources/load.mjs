import { instantiate } from './clocks-wasm.uninstantiated.mjs';

import loadSkikoWASM  from './skiko.mjs';

import {
    _callCallback,
    _registerCallback,
    _releaseCallback,
    _createLocalCallbackScope,
    _releaseLocalCallbackScope
} from './skiko.mjs'

const skikoWasm = await loadSkikoWASM();
window.GL = skikoWasm.GL;
window._callCallback = _callCallback;
window._registerCallback = _registerCallback;
window._releaseCallback = _releaseCallback;
window._createLocalCallbackScope = _createLocalCallbackScope;
window._releaseLocalCallbackScope = _releaseLocalCallbackScope;

instantiate({ skia:  skikoWasm.asm});