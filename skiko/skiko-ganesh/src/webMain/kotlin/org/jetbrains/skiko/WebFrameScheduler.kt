@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.jetbrains.skiko

import kotlin.js.js

/**
 * By default uses the browser's animation clock. A requested rate uses a timer, for
 * OffscreenCanvas rendering in a worker where rAF can still be limited to 60 Hz.
 * Does not run continuously: callers must request each frame.
 */
internal class WebFrameScheduler(
    targetFrameRate: Double? = null,
    private val clock: WebFrameClock = BrowserFrameClock,
    private val drawFrame: (Double) -> Unit
) {
    init {
        require(targetFrameRate == null || targetFrameRate.isFinite() && targetFrameRate > 0) {
            "targetFrameRate must be finite and positive"
        }
    }

    private val interval = targetFrameRate?.let { 1000.0 / it }
    private var pending: Int? = null
    private var disposed = false
    private var deadline = Double.NEGATIVE_INFINITY

    private val animationCallback: (Double) -> Unit = { timestamp ->
        pending = null
        if (!disposed) drawFrame(timestamp)
    }

    private val timerCallback: () -> Unit = {
        pending = null
        if (!disposed) {
            val now = clock.now()
            if (now < deadline) {
                // setTimeout can round a fractional delay down. Never draw early.
                scheduleTimer(deadline - now)
            } else {
                if (now - deadline >= interval!!) deadline = now
                drawFrame(now)
            }
        }
    }

    fun requestFrame() {
        if (disposed || pending != null) return
        if (interval == null) {
            pending = clock.requestAnimationFrame(animationCallback)
        } else {
            val now = clock.now()
            // Preserve fractional frame periods, but skip missed frames after a stall
            // or idle time instead of accumulating a backlog of catch-up renders.
            deadline = maxOf(deadline + interval, now)
            scheduleTimer(deadline - now)
        }
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        pending?.let {
            if (interval == null) clock.cancelAnimationFrame(it) else clock.cancelTimeout(it)
        }
        pending = null
    }

    private fun scheduleTimer(delay: Double) {
        pending = clock.setTimeout(timerCallback, delay)
    }
}

internal interface WebFrameClock {
    fun now(): Double
    fun requestAnimationFrame(callback: (Double) -> Unit): Int
    fun cancelAnimationFrame(id: Int)
    fun setTimeout(callback: () -> Unit, delay: Double): Int
    fun cancelTimeout(id: Int)
}

private object BrowserFrameClock : WebFrameClock {
    override fun now(): Double = performanceNow()
    override fun requestAnimationFrame(callback: (Double) -> Unit): Int = browserRequestAnimationFrame(callback)
    override fun cancelAnimationFrame(id: Int) = browserCancelAnimationFrame(id)
    override fun setTimeout(callback: () -> Unit, delay: Double): Int = browserSetTimeout(callback, delay)
    override fun cancelTimeout(id: Int) = browserCancelTimeout(id)
}

private fun performanceNow(): Double = js("globalThis.performance.now()")
private fun browserRequestAnimationFrame(callback: (Double) -> Unit): Int = js("globalThis.requestAnimationFrame(callback)")
private fun browserCancelAnimationFrame(id: Int): Unit = js("globalThis.cancelAnimationFrame(id)")
private fun browserSetTimeout(callback: () -> Unit, delay: Double): Int = js("globalThis.setTimeout(callback, delay)")
private fun browserCancelTimeout(id: Int): Unit = js("globalThis.clearTimeout(id)")
