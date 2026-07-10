package org.jetbrains.skiko.windows

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.hostOs

/**
 * Internal per-window vsync tick source for Windows.
 *
 * Windows has no EDT-deliverable vsync callback today: on this platform vsync is fused into
 * the dwmFlush/D3D `swap()` calls that happen as part of rendering a frame (see
 * `Direct3DRedrawer.swap` and `directXRedrawer.cc`). This class is a *separate* tick source -
 * it does not participate in and must not interfere with that render path. It drives a
 * dedicated native thread per window that predicts the next compositor vblank (via
 * `DwmGetCompositionTimingInfo`, falling back to a fixed-interval poll if that is unavailable)
 * and reports the predicted present timestamp back to [onTick].
 *
 * This is internal machinery. It backs the public `DisplayFrameTicker`; callers subscribe to
 * frames through that API rather than constructing this tick source directly.
 *
 * [onTick] is invoked directly on the native tick thread, not on the EDT or any coroutine
 * dispatcher - callers must not perform UI work in it without first hopping to the right thread.
 */
internal class WindowsVsyncTicker(
    private val windowHandle: Long,
    private val onTick: (predictedPresentTimeNanos: Long) -> Unit
) {
    init {
        check(hostOs.isWindows) { "WindowsVsyncTicker is only supported on Windows" }
        Library.load()
    }

    private var tickerHandle: Long = 0L

    val isRunning: Boolean get() = tickerHandle != 0L

    /**
     * Starts the native vsync tick thread for this window. Must not be called while already running.
     */
    fun start() {
        check(tickerHandle == 0L) { "WindowsVsyncTicker is already running" }
        tickerHandle = nativeStart(windowHandle).also { handle ->
            check(handle != 0L) { "Failed to start the native Windows vsync ticker" }
        }
    }

    /**
     * Stops the native vsync tick thread, blocking until it has fully exited. Safe to call
     * more than once; a no-op if not running.
     */
    fun stop() {
        if (tickerHandle != 0L) {
            nativeStop(tickerHandle)
            tickerHandle = 0L
        }
    }

    // Called from native code, on the dedicated native tick thread (see VsyncTicker.cc).
    private fun onVsyncTick(predictedPresentTimeNanos: Long) {
        onTick(predictedPresentTimeNanos)
    }

    private external fun nativeStart(windowHandle: Long): Long
    private external fun nativeStop(tickerHandle: Long)
}
