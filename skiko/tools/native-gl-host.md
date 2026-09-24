# Incremental native GL host

The non-browser runtime can replace Emscripten WebGL imports one function at a time. The loader
prefers an optional native host and keeps the existing Emscripten implementation as a fallback.

Install the host before the first frame, either before dynamically importing Skiko:

```js
globalThis.__skikoNativeGL = nativeAddon;
globalThis.__skikoLogGLFallbacks = true;
const skiko = await import("./skiko.mjs");
```

or through the exported setter:

```js
import {setSkikoNativeGLHost} from "./skiko.mjs";

setSkikoNativeGLHost(nativeAddon);
```

The host exports functions under their exact Wasm import names:

```js
nativeAddon.glClearColor(red, green, blue, alpha);
nativeAddon.glClear(mask);
```

The first host also owns context activation through:

```js
nativeAddon.createContext(canvas, attributes);
nativeAddon.makeContextCurrent(contextId);
```

Until those methods are present, context creation still falls back to the current WebGL path.

It may also export:

```js
nativeAddon.attachWasmMemory(arrayBuffer);
```

The loader calls `attachWasmMemory` initially and again whenever `memory.grow()` replaces the
backing `ArrayBuffer`. Any pointer received by a GL function remains a 32-bit byte offset into that
buffer. The native host must bounds-check the requested range and calculate `base + offset`; it
must never reinterpret the offset itself as a native pointer.

Call `getSkikoGLImportUsage()` to inspect migration coverage:

```js
const {native, emscriptenFallback} = getSkikoGLImportUsage();
```

The attached module imports 141 GL functions. Comparing them with Skia's
`include/gpu/ganesh/gl/GrGLFunctions.h` gives two initial groups:

- 72 functions have scalar-only signatures and do not directly access Wasm memory.
- 69 functions contain pointer data or need special handle treatment.

A deliberately small first native batch is:

- `glClearColor`
- `glClear`
- `glViewport`
- `glScissor`
- `glEnable`
- `glDisable`
- `glColorMask`
- `glBlendColor`
- `glBlendEquation`
- `glBlendFunc`

Do not treat every scalar-looking value as interchangeable. In particular, `GLsync` is a native
pointer type even though the Wasm ABI represents it as an integer. `glFenceSync`, `glIsSync`,
`glClientWaitSync`, `glWaitSync`, and `glDeleteSync` need a 32-bit handle table. `glGetString` and
`glGetStringi` must copy native strings into Wasm-owned memory and return a Wasm offset.

Generate a fresh inventory from any built module with:

```bash
node skiko/tools/wasm-imports.mjs path/to/skiko.wasm --json build/skiko-wasm-imports.json
```