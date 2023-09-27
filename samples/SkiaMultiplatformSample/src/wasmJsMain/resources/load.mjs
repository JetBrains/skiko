import { instantiate } from './SkiaMultiplatformSample-wasm-js.uninstantiated.mjs';

await wasmSetup;
instantiate({ skia: Module['asm'] });