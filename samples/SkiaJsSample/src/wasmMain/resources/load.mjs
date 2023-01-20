import { instantiate } from './SkiaJsSample-wasm.uninstantiated.mjs';

await wasmSetup;
instantiate({ skia: Module['asm'] });