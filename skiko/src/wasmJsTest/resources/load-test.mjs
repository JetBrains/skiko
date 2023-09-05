const karmaLoaded = window.__karma__.loaded.bind(window.__karma__);
window.__karma__.loaded = function() {}

import { instantiate } from './skiko-kjs-wasm-test.uninstantiated.mjs';

await wasmSetup;
(await instantiate({ skia: Module['asm'] })).exports.startUnitTests();
karmaLoaded();