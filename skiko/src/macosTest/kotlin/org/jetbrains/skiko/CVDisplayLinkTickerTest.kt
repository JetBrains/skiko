package org.jetbrains.skiko

import kotlinx.cinterop.ExperimentalForeignApi
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait
import platform.darwin.dispatch_time
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * Minimal lifecycle smoke test for [CVDisplayLinkTicker].
 *
 * This intentionally does not assert that [FrameListener.onFrame] actually fires: the ticker delivers
 * frames by hopping to the main dispatch queue ([SkikoDispatchers.Main]), which a plain
 * Kotlin/Native test binary never pumps (there is no running `NSApplication`/`CFRunLoop`), and
 * `CVDisplayLinkCreateWithCGDisplay` itself may legitimately fail in a headless/CI environment with no
 * display session. Both are handled gracefully by the implementation (it simply never starts), so what
 * this test verifies is that subscribe/schedule/unsubscribe/close never throw and are safe to call
 * repeatedly and in any order — including after [CVDisplayLinkTicker.close].
 */
@OptIn(ExperimentalSkikoApi::class)
class CVDisplayLinkTickerTest {

    @Test
    fun subscribeScheduleAndCloseDoNotThrow() {
        val ticker = CVDisplayLinkTicker()

        val subscription = ticker.subscribe(FrameListener { })
        ticker.scheduleFrame()
        ticker.scheduleFrame() // coalesced: must not throw or double-schedule anything observable here

        subscription.close()
        ticker.close()
    }

    @Test
    fun closeIsIdempotent() {
        val ticker = CVDisplayLinkTicker()
        ticker.close()
        ticker.close()
    }

    @Test
    fun scheduleFrameWithNoSubscribersIsSafe() {
        val ticker = CVDisplayLinkTicker()
        ticker.scheduleFrame()
        ticker.close()
    }

    @Test
    fun operationsAfterCloseAreNoOps() {
        val ticker = CVDisplayLinkTicker()
        ticker.close()

        val subscription = ticker.subscribe(FrameListener { })
        ticker.scheduleFrame()
        subscription.close()
    }

    @Test
    fun multipleSubscribersCanUnsubscribeIndependently() {
        val ticker = CVDisplayLinkTicker()
        val a = ticker.subscribe(FrameListener { })
        val b = ticker.subscribe(FrameListener { })

        ticker.scheduleFrame()
        a.close()
        ticker.scheduleFrame()
        b.close()

        ticker.close()
    }

    /**
     * Regression test for a deadlock where a thread stopping the display link (via [CVDisplayLinkTicker.close]
     * or an unsubscribe that drops the last listener) blocked on `CVDisplayLinkStop` while holding the
     * ticker's lock, and the `CVDisplayLink` callback thread — already inside an in-flight vsync callback —
     * blocked trying to acquire that same lock as its first action. `CVDisplayLinkStop` itself blocks until
     * any in-flight callback invocation returns, so the two threads deadlocked each other.
     *
     * This can only be reproduced with a *real*, actively-ticking `CVDisplayLink` racing against real
     * start/stop calls from another thread — a fake/mocked clock can't recreate "a callback is already
     * executing, past entry, about to acquire the lock" at the precise moment another thread calls Stop.
     * So this hammers subscribe/schedule/unsubscribe (each of which starts/stops the display link) from a
     * background thread for a short window, so many real start/stop transitions race against whatever real
     * vsync callbacks the environment produces, and bounds the whole thing with a watchdog timeout so a
     * regression fails the test instead of hanging the run forever.
     *
     * If `CVDisplayLinkCreateWithCGDisplay` can't actually start a display link in this environment (e.g.
     * headless CI with no display session — see the class doc on [CVDisplayLinkTickerTest]), every
     * start/stop in the loop below is a fast no-op (nothing to race against), and this test still passes —
     * it just isn't exercising the race in that environment. It was verified locally (real macOS, real
     * display) that `CVDisplayLinkCreateWithCGDisplay`/`CVDisplayLinkStart` do succeed and tick in a plain
     * Kotlin/Native test binary here, independent of any run loop (unlike [FrameListener.onFrame] delivery,
     * which does need one — see the class doc — and is deliberately not used by this test).
     */
    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun rapidStartStopDoesNotDeadlockAgainstLiveVSyncCallbacks() {
        val ticker = CVDisplayLinkTicker()
        val queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
        val hammeringDone = dispatch_semaphore_create(0)

        dispatch_async(queue) {
            val deadlineNanos = currentNanoTime() + 6_000_000_000L // ~6s of start/stop churn
            while (currentNanoTime() < deadlineNanos) {
                val subscription = ticker.subscribe(FrameListener { })
                ticker.scheduleFrame()
                // Give the just-(re)started display link a real chance to fire at least one genuine vsync
                // callback on its own thread before we stop it again — otherwise Stop always wins the race
                // trivially (nothing was ever in flight) and the test can't exercise anything. Each stop
                // attempt is then one independent, essentially-randomly-phased sample of "does this Stop
                // call land while a callback invocation is genuinely in flight" — so what matters for
                // catching the race is the number of iterations, not a long per-iteration wait.
                platform.posix.usleep(10_000u)
                subscription.close() // unsubscribe: drops the last listener, stops the display link again
            }
            dispatch_semaphore_signal(hammeringDone)
        }

        val timedOut = dispatch_semaphore_wait(hammeringDone, dispatch_time(DISPATCH_TIME_NOW, 15_000_000_000L)) != 0L
        if (!timedOut) {
            // Only safe to tear down if the hammering thread actually finished — if it's deadlocked,
            // close() would just be a second thread blocking on the same stuck lock/CVDisplayLinkStop.
            ticker.close()
        }
        assertFalse(
            timedOut,
            "Rapid subscribe/schedule/unsubscribe against a live CVDisplayLink did not finish within 15s " +
                "(expected ~6s) — this indicates a deadlock between a thread stopping the display link and " +
                "an in-flight vsync callback (see CVDisplayLinkTicker's class doc on the lock/CVDisplayLinkStop " +
                "ordering)."
        )
    }
}
