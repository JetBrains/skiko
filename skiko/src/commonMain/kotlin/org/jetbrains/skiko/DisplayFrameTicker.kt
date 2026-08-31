package org.jetbrains.skiko

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * A vsync-aligned frame source shared across platforms.
 *
 * It is the scheduling companion of [RenderContext]: `RenderContext` is *how* you draw a frame,
 * `DisplayFrameTicker` is *when*. A consumer subscribes a [FrameListener], calls [scheduleFrame], and on
 * each callback draws exactly one frame &mdash; without writing a platform-specific frame loop by hand
 * (`requestAnimationFrame`, `Choreographer`, `CADisplayLink`, `CVDisplayLink`, an AWT vsync thread, &hellip;).
 *
 * Skiko owns *how* to tick: vsync pacing, coalescing, delivery on the platform's UI thread, idle
 * pause/resume, and the per-frame predicted present time. It does not decide how many tickers exist: a
 * consumer creates one (typically per display or per window), shares it, subscribes, and [close]s it.
 *
 * Obtain one from the matching per-platform factory:
 * - Android &mdash; `DisplayFrameTicker()` (backed by [android.view.Choreographer]).
 * - iOS &mdash; `DisplayFrameTicker()` (backed by `CADisplayLink`).
 * - Web &mdash; `DisplayFrameTicker()` (backed by `requestAnimationFrame`).
 * - macOS (Kotlin/Native) &mdash; `DisplayFrameTicker()` (backed by `CVDisplayLink`).
 * - AWT/Desktop JVM &mdash; `DisplayFrameTicker(window)` (a per-window vsync source; see that factory's doc
 *   for the honest per-OS backing: `CVDisplayLink` on macOS, `DwmGetCompositionTimingInfo` on Windows,
 *   GLX vblank on Linux).
 *
 * Not thread-safe with respect to its own lifecycle beyond what each factory documents: create it on, and
 * drive it from, the platform's UI thread unless a factory says otherwise. [close] releases the underlying
 * frame source; after it, the ticker must not be used again.
 */
interface DisplayFrameTicker : AutoCloseable {
    /**
     * Register [listener] to receive frame callbacks. The returned [AutoCloseable] unsubscribes it.
     * Subscribing does not by itself start frames &mdash; call [scheduleFrame].
     *
     * A listener that throws does not tear the ticker down for the other subscribers: the exception is
     * caught per listener, logged, and delivery continues. (Only cooperative cancellation of the ticker's
     * own delivery loop propagates.)
     */
    fun subscribe(listener: FrameListener): AutoCloseable

    /**
     * Request the next frame. Coalescing: multiple calls before a frame begins collapse to a single frame;
     * calls made *during* a frame schedule exactly one follow-up frame. Resumes the ticker if it was idle.
     * Safe to call from any thread.
     */
    fun scheduleFrame()
}

/**
 * A frame callback for [DisplayFrameTicker], invoked on the platform's UI thread. It is a `suspend`
 * function so a listener can pace itself and rasterize off the UI thread within the frame.
 *
 * @param targetTimeNanos the predicted present time of this frame, in nanoseconds, on a monotonic clock.
 * It is intended as an animation clock (advance animations toward the moment the frame will actually be
 * shown). The **timestamp base is platform-specific** &mdash; only compare values produced by the *same*
 * ticker instance, never against [currentNanoTime] or another ticker/platform:
 * - iOS &mdash; `CADisplayLink.targetTimestamp`, a genuine predicted present time.
 * - macOS (Kotlin/Native) and AWT-on-macOS &mdash; derived from `CVDisplayLink`'s `outputTime` (the host
 *   clock's predicted present time).
 * - AWT-on-Windows &mdash; the predicted next compositor vblank from `DwmGetCompositionTimingInfo`.
 * - AWT-on-Linux &mdash; the driver's vblank timestamp (`GLX_OML_sync_control`'s real UST when available;
 *   a best-effort value on drivers that only expose the older `GLX_SGI_video_sync`).
 * - Android and Web &mdash; the vsync-aligned frame *start* (`Choreographer`'s `frameTimeNanos` /
 *   `requestAnimationFrame`'s timestamp), which is the vsync boundary rather than a true predicted present.
 */
fun interface FrameListener {
    suspend fun onFrame(targetTimeNanos: Long)
}

/**
 * Delivers one frame to a single [listener] with per-listener exception isolation: a listener that throws
 * is logged and does not tear the ticker down for the other subscribers. Only cooperative cancellation of
 * the delivery coroutine propagates. Shared by every platform's [DisplayFrameTicker] implementation.
 */
internal suspend fun dispatchFrame(listener: FrameListener, targetTimeNanos: Long) {
    try {
        listener.onFrame(targetTimeNanos)
    } catch (e: CancellationException) {
        // Propagate only genuine cancellation of this delivery coroutine. A CancellationException that
        // surfaces while this coroutine is still active leaked from the listener's own scope (for
        // example an uncaught withTimeout); isolate it like any other listener failure so one listener
        // cannot tear down the shared ticker loop.
        if (!currentCoroutineContext().isActive) throw e
        Logger.error(e) { "DisplayFrameTicker: FrameListener.onFrame cancelled itself; continuing for other listeners" }
    } catch (t: Throwable) {
        Logger.error(t) { "DisplayFrameTicker: FrameListener.onFrame threw; continuing for other listeners" }
    }
}
