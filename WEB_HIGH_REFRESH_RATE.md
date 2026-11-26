# Experimental 120 Hz rendering on the web

Enable the experiment before attaching an HTML canvas:

```kotlin
@OptIn(ExperimentalSkikoApi::class)
fun attach(canvas: HTMLCanvasElement): SkiaLayer = SkiaLayer().apply {
    experimentalHighRefreshRateEnabled = true
    renderDelegate = SkiaLayerRenderDelegate(this, myRenderDelegate)
    attachTo(canvas)
    needRender()
}
```

The API is available on Kotlin/JS and Kotlin/Wasm. Rendering and DOM input
handlers stay on the main thread. The flag is off by default and cannot change
while attached.

## How it works

Based on Rida F'kih's [Safari 60 FPS bypass experiment](https://rida.dev/blog/bypassing-ios-safari-60-fps-limit):

- Transfer the HTML canvas to an `OffscreenCanvas` and draw through its WebGL2
  context. The original element remains a placeholder, returned by `component`.
  Placeholder presentation is the bypass; a faster timer alone is insufficient.
- Pace requested frames with timer/MessageChannel tasks aligned to rAF timestamps,
  allowing two frames per 60 Hz rAF interval. Account for iOS clock rounding and
  skip missed deadlines after a stall.
- While rendering, share a tiny smooth scroller with a composited animation to
  encourage ProMotion to use 120 Hz. Idle or hidden layers stop their timers,
  rAF loop and display boost. Visibility restoration resumes pending requests.

The main-thread presentation path is inferred from WebKit's
[OffscreenCanvas task submission](https://github.com/WebKit/WebKit/blob/main/Source/WebCore/html/OffscreenCanvas.cpp)
and [asynchronous placeholder display](https://github.com/WebKit/WebKit/blob/main/Source/WebCore/html/canvas/PlaceholderRenderingContext.cpp).
Physical 120 Hz presentation on iOS still requires device validation; it has not
been established by the Chromium checks.

## Resize, reattach and fallback

If transfer or OffscreenCanvas WebGL2 is unavailable, or the HTML canvas already
has a context, attachment falls back to ordinary rAF.

Transfer is irreversible. Set CSS size normally, then call
`layer.resize(physicalWidth, physicalHeight)` instead of assigning the HTML
canvas's `width` or `height`. Skiko resizes the backing buffer and drawing surface.
The backing is cached in a `WeakMap`, so detach/reattach does not transfer it twice.
Disabling the flag after detach restores rAF scheduling but retains that backing;
use a new HTML canvas to restore an ordinary rendering context.

Request each next frame with `needRender()`, as `SkiaLayerRenderDelegate` does.
`detach()` cancels pending work, removes listeners, closes MessageChannel ports
and releases Skia resources. Frame timestamps use the window's performance clock.

Compose and other hosts must adopt `layer.resize()` before enabling the flag.
A host using its own 60 Hz animation clock must adapt it too: Skiko does not
produce intermediate application states or replace that clock. Native scroll
controllers and higher-frequency gesture input from the article are separate
host integrations; this experiment changes rendering only.

## Sample and device checks

```shell
cd samples/SkiaWebSample
./gradlew jsBrowserDevelopmentRun -Pskiko.composite.build=1
# Or: ./gradlew wasmJsBrowserDevelopmentRun -Pskiko.composite.build=1
```

Open `offscreen.html`, linked from the regular sample. It compares normal and
experimental rendering. Uncheck Animate, resize, and background/restore the page
to check lifecycle behavior.

Counters measure render callbacks, not presented frames. Validate on a ProMotion
iPhone using 240 FPS video or a presentation trace, with the normal canvas as a
60 Hz control. Check power settings, sustained pacing and idle energy use too.
WebKit may change this behavior, and the API remains experimental.
