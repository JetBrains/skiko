import { instantiate } from './clocks-wasm.uninstantiated.mjs';

import { loadAndInitSkikoWasm }  from './skiko.mjs';
const skikoWasm = await loadAndInitSkikoWasm();
instantiate({ skia:  skikoWasm.asm});