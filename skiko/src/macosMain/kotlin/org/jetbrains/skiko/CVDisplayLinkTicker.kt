package org.jetbrains.skiko

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGDirectDisplayID
import platform.CoreGraphics.CGMainDisplayID
import platform.CoreVideo.CVDisplayLinkCreateWithCGDisplay
import platform.CoreVideo.CVDisplayLinkRef
import platform.CoreVideo.CVDisplayLinkRefVar
import platform.CoreVideo.CVDisplayLinkRelease
import platform.CoreVideo.CVDisplayLinkSetOutputHandler
import platform.CoreVideo.CVDisplayLinkStart
import platform.CoreVideo.CVDisplayLinkStop
import platform.CoreVideo.CVGetHostClockFrequency
import platform.CoreVideo.CVTimeStamp
import platform.CoreVideo.kCVReturnSuccess
import platform.Foundation.NSCondition

/**
 * The public [DisplayFrameTicker] for the macOS Kotlin/Native (non-AWT) target: a `CVDisplayLink`-backed
 * frame source. Obtain one via the [DisplayFrameTicker] factory function; the caller owns sharing and
 * lifecycle and must [close][DisplayFrameTicker.close] it when done. Callbacks are delivered on the main
 * thread/run loop.
 */
fun DisplayFrameTicker(): DisplayFrameTicker = CVDisplayLinkTicker()

/**
 * [DisplayFrameTicker] backed by `CVDisplayLink`, for the macOS Kotlin/Native (non-AWT) target.
 *
 * The display link is created lazily on first demand (a subscriber present *and* a frame requested) and
 * torn down again once idle, so an app that never calls [scheduleFrame] never pays for a running display
 * link — unlike a naive always-on timer. Frame delivery is coalesced (one pending frame at a time) and
 * always happens on the main thread/run loop, via [SkikoDispatchers.Main], regardless of which thread
 * triggers [scheduleFrame] or the `CVDisplayLink` callback fires on.
 *
 * `CVDisplayLinkStart`/`CVDisplayLinkStop` must not be invoked from inside the display link's own output
 * callback (Apple's docs call this out for `Start`; in practice it's unsafe for `Stop` too, since that
 * thread *is* the callback thread). This implementation only ever starts/stops from external callers
 * ([subscribe], [unsubscribe], [scheduleFrame], [close]) or from the main-thread coroutine that delivers
 * frames — never synchronously from the `CVDisplayLink` callback itself.
 *
 * `CVDisplayLinkStop` additionally blocks synchronously until any in-flight invocation of the output
 * callback returns. [onVSyncTick] (running on the callback thread) acquires [lock] as its first action, so
 * [lock] must never be held across a `CVDisplayLinkStop` call made by another thread — doing so can
 * deadlock a caller of e.g. [close] against a vsync tick that is already in flight. [updateRunningLocked]
 * therefore releases [lock] around the blocking stop call (tracked via [stopping]) and re-validates state
 * once it reacquires the lock, since a subscriber/frame request may have arrived while it was unlocked.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalSkikoApi::class)
internal class CVDisplayLinkTicker(
    private val displayId: CGDirectDisplayID = CGMainDisplayID()
) : DisplayFrameTicker {

    private val lock = NSCondition()
    private val listeners = mutableListOf<FrameListener>()
    private var framePending = false
    private var closed = false
    private var displayLink: CVDisplayLinkRef? = null

    /**
     * True while some thread has released [lock] to make the blocking `CVDisplayLinkStop` call in
     * [updateRunningLocked]. Guards against two threads concurrently deciding to start/stop the display
     * link: while true, other callers of [updateRunningLocked] wait on [lock] instead of acting, and
     * re-evaluate desired state only after the in-flight stop finishes (and clears this flag).
     */
    private var stopping = false

    private val mainScope = CoroutineScope(SkikoDispatchers.Main)

    override fun subscribe(listener: FrameListener): AutoCloseable {
        lock.lock()
        try {
            if (closed) return AutoCloseable {}
            listeners.add(listener)
            updateRunningLocked()
        } finally {
            lock.unlock()
        }
        return AutoCloseable { unsubscribe(listener) }
    }

    private fun unsubscribe(listener: FrameListener) {
        lock.lock()
        try {
            listeners.remove(listener)
            updateRunningLocked()
        } finally {
            lock.unlock()
        }
    }

    override fun scheduleFrame() {
        lock.lock()
        try {
            if (closed) return
            framePending = true
            updateRunningLocked()
        } finally {
            lock.unlock()
        }
    }

    override fun close() {
        lock.lock()
        try {
            if (closed) return
            closed = true
            listeners.clear()
            framePending = false
            // Routes through updateRunningLocked (rather than stopping directly) so that, if another
            // thread is already mid-stop, this waits for it instead of racing it — see updateRunningLocked.
            updateRunningLocked()
        } finally {
            lock.unlock()
        }
        mainScope.cancel()
    }

    /**
     * Must be called while holding [lock]. Never call from the `CVDisplayLink` callback thread.
     *
     * Starting never releases [lock] (it can't block on the callback thread), but stopping must: it calls
     * `CVDisplayLinkStop`, which blocks until any in-flight callback invocation returns, and that callback
     * ([onVSyncTick]) itself blocks trying to acquire [lock]. So this releases [lock] around the stop call
     * and then loops to re-check desired state against the lock's *current* contents, since it may have
     * changed while unlocked (e.g. a fresh [subscribe]/[scheduleFrame] came in). While a stop is in flight
     * ([stopping]), other callers wait on [lock] rather than acting, so at most one thread ever starts or
     * stops the display link at a time, and this call always returns with [lock] held.
     */
    private fun updateRunningLocked() {
        while (true) {
            while (stopping) {
                lock.wait()
            }
            val shouldRun = !closed && framePending && listeners.isNotEmpty()
            val isRunning = displayLink != null
            when {
                shouldRun && !isRunning -> {
                    startLocked()
                    return
                }
                !shouldRun && isRunning -> {
                    val link = displayLink ?: return
                    displayLink = null
                    stopping = true
                    lock.unlock()
                    try {
                        CVDisplayLinkStop(link)
                        CVDisplayLinkRelease(link)
                    } finally {
                        lock.lock()
                        stopping = false
                        lock.broadcast()
                    }
                    // Loop back around: re-validate against fresh state now that the lock is held again.
                }
                else -> return
            }
        }
    }

    /** Must be called while holding [lock]. Never call from the `CVDisplayLink` callback thread. */
    private fun startLocked() {
        memScoped {
            val linkVar = alloc<CVDisplayLinkRefVar>()
            if (CVDisplayLinkCreateWithCGDisplay(displayId, linkVar.ptr) != kCVReturnSuccess) {
                return
            }
            val link = linkVar.value ?: return

            val handlerStatus = CVDisplayLinkSetOutputHandler(link) { _, _, outputTime, _, _ ->
                onVSyncTick(outputTime)
                kCVReturnSuccess
            }
            if (handlerStatus != kCVReturnSuccess) {
                CVDisplayLinkRelease(link)
                return
            }

            if (CVDisplayLinkStart(link) != kCVReturnSuccess) {
                CVDisplayLinkRelease(link)
                return
            }

            displayLink = link
        }
    }

    /**
     * Invoked on `CVDisplayLink`'s own real-time output thread — must stay off the lock for as short as
     * possible, and must never start/stop the display link directly (see class doc).
     */
    private fun onVSyncTick(outputTime: CPointer<CVTimeStamp>?) {
        val targetTimeNanos = outputTime?.pointed?.let(::toNanos) ?: return
        val listenersSnapshot: List<FrameListener>

        lock.lock()
        try {
            if (closed || !framePending) return
            framePending = false
            listenersSnapshot = listeners.toList()
        } finally {
            lock.unlock()
        }

        if (listenersSnapshot.isEmpty()) return

        // Hop to the main thread both to deliver the (suspend) callback on the platform UI thread, and to
        // safely re-evaluate (and possibly stop) the display link off of its own callback thread.
        mainScope.launch {
            // Per-listener exception isolation (DisplayFrameTicker contract): a listener that throws must
            // not tear down delivery for the others, nor skip the updateRunningLocked() cleanup below.
            for (listener in listenersSnapshot) {
                dispatchFrame(listener, targetTimeNanos)
            }
            lock.lock()
            try {
                updateRunningLocked()
            } finally {
                lock.unlock()
            }
        }
    }

    private fun toNanos(timestamp: CVTimeStamp): Long {
        val frequency = CVGetHostClockFrequency()
        if (frequency <= 0.0) return currentNanoTime()
        return (timestamp.hostTime.toDouble() / frequency * 1_000_000_000.0).toLong()
    }
}
