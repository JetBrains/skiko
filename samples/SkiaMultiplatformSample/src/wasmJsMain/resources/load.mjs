import { instantiate } from './clocks-wasm.uninstantiated.mjs';

import loadSkikoWASM  from './skiko.mjs';

const skikoWasm = await loadSkikoWASM();
window.GL = skikoWasm.GL;
instantiate({ skia:  skikoWasm.asm});