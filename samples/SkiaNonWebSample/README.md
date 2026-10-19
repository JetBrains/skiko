# Skiko Wasmtime C++ host

This target lives beside the Node runner so the two runtime paths can be
compared. Both runners use shared GPU textures and avoid presentation readback.

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
  -DWASMTIME_ROOT=/path/to/wasmtime-c-api \
  -DSKIA_ROOT=/path/to/skia \
  -DSKIA_LIBRARY=/path/to/skia/out/Release-macos-arm64-gl/libskia.a

cmake --build build -j
```

### Running via Gradle

You can build and launch both runners directly through Gradle from the project root:

#### Node.js Runner (`wasmJsNodeRun`)
```bash
./gradlew -p samples/SkiaNonWebSample wasmJsNodeRun \
  -Pskia.dir=/path/to/skia \
  -Pwasmtime.root=/path/to/wasmtime-c-api \
  -Pskia.library=/path/to/skia/out/Release-macos-arm64-gl/libskia.a \
  --rerun-tasks
```

#### Wasmtime Runner (`wasmJsWasmtimeRun`)
```bash
./gradlew -p samples/SkiaNonWebSample wasmJsWasmtimeRun \
  -Pskia.dir=/path/to/skia \
  -Pwasmtime.root=/path/to/wasmtime-c-api \
  -Pskia.library=/path/to/skia/out/Release-macos-arm64-gl/libskia.a \
  --rerun-tasks
```

*(Note: If you have configured the environment variables `SKIA_DIR`, `WASMTIME_ROOT`, and optional `SKIA_LIBRARY`, the `-P` property flags can be omitted.)*

### Environment Variables and Gradle Properties

Paths can be configured via environment variables or Gradle project properties:

- **Skia directory**: Set `SKIA_DIR` or `SKIA_ROOT` in the environment, or pass `-Pskia.dir=/path/to/skia` in Gradle.
- **Wasmtime root**: Set `WASMTIME_ROOT` or `WASMTIME_DIR` in the environment, or pass `-Pwasmtime.root=/path/to/wasmtime-c-api` in Gradle.
- **Skia library** (optional): Set `SKIA_LIBRARY` in the environment, or pass `-Pskia.library=/path/to/libskia.a` in Gradle (defaults automatically to `${SKIA_ROOT}/out/Release-macos-<arch>[-gl]/libskia.a`).
- **Executable overrides** (optional): Pass `-Pskiko.node.executable=...`, `-Pskiko.npm.executable=...`, or `-Pskiko.cmake.executable=...` in Gradle.

The application window is an SDL2 OpenGL window. Each Skiko context renders to
a texture shared with the presentation context; `glBlitFramebuffer` scales and
composites those textures before `SDL_GL_SwapWindow`.

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
queued for the next tick. Presentation performs no `glReadPixels`, CPU scaling,
CPU compositing, or texture re-upload.

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

The Node runner uses the existing `native-skiko` bridge. That addon owns the
SDL window and composites the three shared CGL textures directly on the GPU;
there is no JavaScript WebGL package or CPU pixel readback. Install SDL2 and
build the addon once:

```bash
brew install sdl2
cd samples/SkiaNonWebSample
npm --prefix native-skiko ci
npm --prefix native-skiko run build
node node-runner.mjs
```

Both runners batch animation callbacks, give each canvas the same timestamp,
compose entirely on the GPU, and perform one buffer swap per displayed frame.
