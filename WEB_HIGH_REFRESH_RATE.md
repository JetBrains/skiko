# Experimental 120 Hz rendering on the web

Enable the experiment before attaching an ordinary HTML canvas:

```kotlin
@OptIn(ExperimentalSkikoApi::class)
fun attach(canvas: HTMLCanvasElement): SkiaLayer = SkiaLayer().apply {
    experimentalHighRefreshRateEnabled = true
    renderDelegate = SkiaLayerRenderDelegate(this, myRenderDelegate)
    attachTo(canvas)
    needRender()
}
```

Both Kotlin/JS and Kotlin/Wasm use this API. Rendering, application state and DOM
input handlers stay on the main thread. A Worker is not required.

## Mechanism

The implementation follows the rendering mechanism in Rida F'kih's
[article](https://rida.dev/blog/bypassing-ios-safari-60-fps-limit):

- Transfer the HTML canvas to an `OffscreenCanvas` and create Skia's WebGL2
  context on that backing canvas. The DOM canvas remains a placeholder and is
  still returned by `layer.component`. The bypass comes from placeholder frame
  presentation, not from making a timer fire more often.
- Pace requested frames with `setTimeout` followed by a `MessageChannel` task.
  Anchor subsequent deadlines to `requestAnimationFrame` timestamps, dividing
  even a 60 Hz rAF clock into 120 Hz periods. The rAF loop also keeps WebKit's
  rendering updates queued while animating.
- Derive the minimum spacing from the refresh interval to accommodate iOS's
  millisecond `performance.now()` precision. Skip missed deadlines after a stall.
- While frames are being requested, share a one-pixel smooth scroller containing
  a composited transform animation to encourage ProMotion to use its full rate.
  Stop the scroller and rAF loop as soon as the layer is idle. Hidden documents
  suspend pending work; visibility restoration resumes the pending request.

The WebKit source independently supports the presentation mechanism:
[`OffscreenCanvas::scheduleCommitToPlaceholderCanvas`](https://github.com/WebKit/WebKit/blob/main/Source/WebCore/html/OffscreenCanvas.cpp)
posts a task to its script execution context, and
[`PlaceholderRenderingContext`](https://github.com/WebKit/WebKit/blob/main/Source/WebCore/html/canvas/PlaceholderRenderingContext.cpp)
uses an asynchronous contents display delegate. Running this on the document
thread is an inference from these paths; physical presentation still needs an
on-device check.

## Lifecycle and fallback

The flag is off by default and cannot change while attached. If the browser does
not support transfer and OffscreenCanvas WebGL2, or the HTML canvas already has a
context, attachment uses the normal rAF path.

Transfer is irreversible. Once transferred, do **not** assign the HTML element's
`width` or `height`: the browser rejects that for a placeholder. Set its CSS size
normally, then call `layer.resize(physicalWidth, physicalHeight)`. Skiko resizes
the backing canvas and recreates its Skia surface. Callers which assign HTML
canvas dimensions during resize must adopt this API before enabling the flag.
This includes integration work in a Compose host that performs such assignments.
Likewise, a host that advances animation state or requests frames from its own
60 Hz rAF clock must adapt that clock too. Raising Skiko's rendering rate does
not create intermediate application states or replace the host's frame clock.

Skiko caches the backing in a `WeakMap` keyed by the DOM canvas, so detaching and
reattaching, even with another layer instance, does not transfer it twice. A
reattach to the same canvas keeps the existing renderer and its size. After a
transfer, disabling the flag restores rAF scheduling but retains the offscreen
backing; use a new HTML canvas to restore an ordinary rendering context.

`detach()` cancels timers, posted tasks, rAF and visibility listeners, closes the
MessageChannel ports and releases Skia resources. Request each next frame with
`needRender()` (as `SkiaLayerRenderDelegate` already does); idle layers do not run
a continuous timer or display boost. All frame timestamps use the window's
performance time origin.

## Sample and verification

```shell
cd samples/SkiaWebSample
./gradlew jsBrowserDevelopmentRun -Pskiko.composite.build=1
# Or: ./gradlew wasmJsBrowserDevelopmentRun -Pskiko.composite.build=1
```

Open `offscreen.html`, linked from the regular sample. It compares a blue circle
on an ordinary canvas with a red circle on the experimental canvas. Uncheck
Animate to verify the boost disappears while idle, resize the window, and
background/restore the page.

Counters measure render callbacks, not presented frames. Compare the two paths
on a ProMotion iPhone with normal Safari settings, using 240 FPS video or a
presentation trace to count distinct frames. Also check low power mode, display
frame-rate limits, idle energy use and sustained pacing. WebKit may change this
behavior. The software-rendered Chromium smoke checks cannot establish iOS
120 Hz presentation.

## Input is a separate concern

The article's native scroll controllers, passive touch listeners, DPR scaling of
scroll areas, momentum/snap physics, and viewport scroll-driven animation solve
input sampling. Skiko's rendering experiment does not replace DOM input handlers
or install invisible gesture controllers over application content. A host that
needs 120 Hz gesture input must integrate those controllers separately. No
SharedArrayBuffer or COOP/COEP headers are required for this rendering path.
