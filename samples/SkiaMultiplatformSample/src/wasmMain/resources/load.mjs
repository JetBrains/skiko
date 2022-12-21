import { instantiate } from './SkiaMultiplatformSample-wasm.uninstantiated.mjs';

await wasmSetup;
instantiate({ skia: Module['asm'] });