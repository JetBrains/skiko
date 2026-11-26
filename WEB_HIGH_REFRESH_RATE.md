# Experimental high refresh rate rendering on the web

Skiko supports rendering a transferred `OffscreenCanvas` in a dedicated worker,
with a timer targeting 120 frames per second. Both Kotlin/JS and Kotlin/Wasm use
the same experimental API:

```kotlin
@OptIn(ExperimentalSkikoApi::class)
fun startRendering(canvas: JsAny, scale: Float) {
    val layer = SkiaLayer()
    layer.renderDelegate = SkiaLayerRenderDelegate(layer, myRenderDelegate)
    layer.attachToOffscreenCanvas(canvas, contentScale = scale, targetFrameRate = 120.0)
    layer.needRender()
}
```

Call this **inside the worker**, after Skiko has initialized. The `canvas` argument
is the `OffscreenCanvas` received in a message from the main window. Skiko does
not create the worker or move existing application state into it.

On the main thread, feature-detect worker rendering before transferring the canvas:

```javascript
const canvas = document.querySelector('canvas');
const supported = typeof Worker !== 'undefined' &&
    typeof OffscreenCanvas !== 'undefined' &&
    typeof canvas.transferControlToOffscreen === 'function' &&
    new OffscreenCanvas(1, 1).getContext('webgl2') !== null;

if (supported) {
    // Install the worker message handler and wait for its Skiko-ready message first.
    const offscreen = canvas.transferControlToOffscreen();
    worker.postMessage({ canvas: offscreen, scale: devicePixelRatio }, [offscreen]);
} else {
    // Start the application's ordinary main-thread renderer.
}
```

Transferring an HTML canvas is irreversible and must happen before any context is
created on it. If worker initialization fails after transfer, replace the HTML
canvas element before falling back to main-thread rendering.

The worker owns the drawing buffer. Forward sizes in physical pixels and call
`layer.resize(width, height)` in the worker. To update the scale, call
`attachToOffscreenCanvas` again with the same canvas and the new scale; this reuses
the existing WebGL context. Forward page visibility changes too: `detach()` stops
pending work and releases Skia resources; reattach the same offscreen canvas and
request a frame to resume. Stop or terminate the worker when the page is destroyed.

The ordinary `attachTo(HTMLCanvasElement)` path continues to use
`requestAnimationFrame`. The worker path defaults to a timer because worker rAF
can also be capped at 60 Hz. Passing `targetFrameRate = null` selects worker rAF.
Requests are coalesced, idle layers do not run a timer loop, and missed frame
deadlines are skipped rather than replayed. Timestamps use `performance.now()` in
the worker's time origin; do not compare them directly with window timestamps.

## Running and evaluating the sample

```shell
cd samples/SkiaWebSample
./gradlew jsBrowserDevelopmentRun -Pskiko.composite.build=1
# Or: ./gradlew wasmJsBrowserDevelopmentRun -Pskiko.composite.build=1
```

Open `offscreen.html`, also linked from the regular sample. It draws a moving
circle, displays the render callback rate, forwards resizes and device pixel
ratio, and pauses rendering while the document is hidden.
An optional checkbox tries to keep ProMotion active through a one-pixel smooth
scroller containing a composited transform animation. It is off by default and
removed while the page is hidden. This workaround is browser dependent and uses
additional power; it is deliberately controlled by the sample rather than Skiko.

This is a rendering experiment, not a guarantee of 120 presented frames per
second. Safari's display power policy, low power mode, hardware, workload and
browser version can reduce the actual rate. Timer callbacks are not vsync signals.
In particular, a counter near 120 does not prove that the screen presents 120
distinct frames. Validate on a ProMotion device using a high speed camera or a
presentation trace, with a 60 Hz control and the usual Safari settings. Check
resize, background/resume, idle power usage and sustained frame pacing as well.

The [Safari 60 FPS bypass article](https://rida.dev/blog/bypassing-ios-safari-60-fps-limit)
also discusses input sampling and keeping ProMotion active. This implementation
provides worker rendering; it does not automatically install native scroll controllers,
create background scrolling, or increase touch event delivery
frequency. Applications using DOM APIs, including an existing Compose UI, need
separate work to run their rendering state in a worker.
