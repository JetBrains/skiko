const karmaLoaded = window.__karma__.loaded.bind(window.__karma__);
window.__karma__.loaded = function() {}

import { instantiate } from './skiko-kjs-wasm-test.uninstantiated.mjs';

import { loadAndInitSkikoWasm, SkikoCallbacks }  from './skiko.mjs';
const skikoWasm = await loadAndInitSkikoWasm();
(await instantiate({ skia: skikoWasm.wasmExports, GL: skikoWasm.GL, SkikoCallbacks: SkikoCallbacks })).exports.startUnitTests()
karmaLoaded();