# Skiko Wasmtime C++ host

This target lives beside the working Node runner. It does not replace or
modify `node-runner.mjs` or `native-skiko/`.

The host instantiates the same native `skiko.wasm` side module, provides
WASIp1, and routes all 163 GL imports through Skia's `GrGLInterface`:

- 138 thin generated callbacks
- 25 explicit adapters for Wasm memory, buffer offsets, strings, mapped
  buffers, framebuffer zero, and `GLsync` handles

The host can also instantiate the generated `SkiaNonWebSample.wasm`
application. In application mode it links the 63 imported Skiko functions
directly to exports of the native `skiko.wasm` instance, implements the small
Kotlin/JS host surface, drives requestAnimationFrame callbacks, and presents the
three offscreen canvases in a native macOS window.

## Dependencies

- macOS 11 or newer
- SDL2 (`brew install sdl2`)
- the same release-mode Skia archive used by `native-skiko`
- a Wasmtime C API release archive (the C++ host uses its stable C ABI)
- CMake 3.24 or newer

`NDEBUG`, `SK_GANESH`, `SK_GL`, `SK_BUILD_FOR_SKIKO`, and `SK_ASSUME_GL=1`
must match the Skia archive. In particular, removing `NDEBUG` recreates the
`GrGLInterface` layout mismatch found in the Node proof of concept.

## Generate bindings

```bash
cd samples/SkiaNonWebSample/wasmtime-skiko
node tools/generate-wasmtime-gl-bindings.mjs
```

## Configure and build

```bash
cmake -S . -B build \
  -DWASMTIME_ROOT=/path/to/wasmtime-v49.0.1-aarch64-macos-c-api \
  -DSKIA_ROOT=/Users/dustin.feucht/projects/skia \
  -DSKIA_LIBRARY=/Users/dustin.feucht/projects/skia/out/Release-macos-arm64-gl/libskia.a

cmake --build build -j
```

The application window uses an accelerated, vsynced SDL2 renderer, matching
the presentation model used by the Node runner's `@kmamal/sdl` dependency.

Use the exact Wasm artifact consumed by the Node runner:

```bash
WASM=../build/wasm/packages/SkiaNonWebSample/kotlin/skiko.wasm

build/skiko-wasmtime "$WASM" --list-imports
build/skiko-wasmtime "$WASM"
```

The second command retains the original library-only validation mode.

## Run the Kotlin application

Wasmtime implements the finalized WebAssembly exception proposal, while the
Kotlin `wasmJs` target emits the legacy encoding by default. Build a second
application artifact with this compiler option in the target's
`compilerOptions` block:

```kotlin
freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
```

Then run both modules together:

```bash
CORE=../build/wasm/packages/SkiaNonWebSample/kotlin/skiko.wasm
APP=../build/wasm/packages/SkiaNonWebSample/kotlin/SkiaNonWebSample.wasm

build/skiko-wasmtime "$CORE" "$APP"
```

The window stays open until it is closed. Animation callbacks remain one-shot,
but callbacks queued for the same 60 Hz display tick are invoked with one shared
timestamp and presented together. Callbacks requested while rendering are
queued for the next tick. Batching keeps canvases synchronized and avoids
performing three complete framebuffer readbacks per displayed frame.

To troubleshoot the GL bridge, start the same window with diagnostics enabled:

```bash
SKIKO_WASMTIME_GL_DIAGNOSTICS=1 \
  build/skiko-wasmtime "$CORE" "$APP"
```

The diagnostic mode captures the first OpenGL error and shader/program failures.
It consumes OpenGL error state, so it is opt-in and intended for troubleshooting.

This executes the existing Kotlin `main`, `BouncingBalls`, `DemoApp`, and
`CanvasRenderer` code. The native window uses the same 606x706 three-panel
composition as `node-runner.mjs`.

The existing comparison path remains:

```bash
cd samples/SkiaNonWebSample
node node-runner.mjs
```
