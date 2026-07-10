package org.jetbrains.skiko.linux

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.hostOs

/**
 * Internal per-window/per-display vsync tick source for Linux (X11/GLX).
 *
 * This class is a *separate*, render-path-independent tick source - it does not participate in
 * and must not interfere with `LinuxOpenGLRedrawer`'s render/present path (`swapBuffers`, the JAWT
 * drawing-surface locking, etc.). It drives a dedicated native thread that blocks on the driver's
 * real vblank counter - preferring `GLX_OML_sync_control` (`glXWaitForMscOML`, which reports a real
 * UST timestamp), and falling back to `GLX_SGI_video_sync` (`glXWaitVideoSyncSGI`, which does not)
 * on drivers that only implement the older extension - and reports the observed vsync time back to
 * [onTick]. Both `glXWaitForMscOML` and `glXGetVideoSyncSGI`/`glXWaitVideoSyncSGI` (unlike, say,
 * `glXSwapBuffers`) *do* require a `GLXContext` to be current on the calling thread - and, for the
 * blocking Wait variants, that context must be *direct* - per the GLX_OML_sync_control and
 * GLX_SGI_video_sync specs. What makes a dedicated wait thread possible here without coupling it to
 * the real render/present context is that the native side (see `VsyncTicker.cc`) gives this thread
 * its own minimal, dedicated, direct, offscreen `GLXContext` (backed by a 1x1 `GLXPbuffer`, on its
 * own second X11 connection) that never renders anything and is never shared with or made current
 * outside this thread - decoupled from, not free of, a GL context.
 *
 * If neither extension is available, [start] throws rather than silently never ticking - see its
 * doc.
 *
 * This is internal machinery, not a public type. On Linux it backs the public
 * `DisplayFrameTicker(window)` API through `createLinuxAwtVsyncDriver` (see `DisplayFrameTicker.awt.kt`),
 * feeding each observed vsync into that ticker's `FrameListener` fan-out. The macOS `CVDisplayLinkTicker`
 * plays the same role there.
 *
 * [onTick] is invoked directly on the native tick thread, not on the EDT or any coroutine
 * dispatcher - callers must not perform UI work in it without first hopping to the right thread.
 *
 * Thread-safety: [start] and [stop] may be called from any thread and are mutually exclusive via
 * [lock]. [stop] never holds [lock] while making the blocking `nativeStop` call (which joins the
 * native tick thread) - only [tickerHandle] itself is guarded, not the blocking call - so a vsync
 * tick that is concurrently arriving on the native thread and calling back into [onVsyncTick] can
 * never deadlock against a concurrent [stop]. [onVsyncTick] does not touch [lock] at all, for the
 * same reason. This is the deadlock class where a lock is held across a blocking native
 * teardown call that the callback thread also needs; keeping [lock] out of both the teardown join and
 * the callback path is what makes it impossible here. The macOS `CVDisplayLinkTicker` follows suit.
 *
 * @param display the native `Display*`, as obtained from `LinuxDrawingSurface.display` (see
 * `AWTLinuxDrawingSurface.kt`). This ticker never acquires the JAWT drawing-surface lock itself, so
 * the caller must ensure the display connection stays valid for the duration of [start]. Native code
 * only touches this pointer briefly, on the calling thread, during [start] - to learn which X server
 * to open its own second, dedicated connection to (see `VsyncTicker.cc`'s file-level comment for why
 * a second connection, rather than this one, is used for the actual native tick thread's GLX calls).
 * It is not read again afterwards, so it need not stay valid beyond [start] returning.
 * @param window the native `Window` XID, as obtained from `LinuxDrawingSurface.window`. Unlike
 * [display], this is a plain server-side identifier rather than a pointer into connection state, so
 * it remains safe to pass to the native tick thread and use for its entire lifetime.
 */
internal class LinuxVsyncTicker(
    private val display: Long,
    private val window: Long,
    private val onTick: (vsyncTimeNanos: Long) -> Unit
) {
    init {
        check(hostOs.isLinux) { "LinuxVsyncTicker is only supported on Linux" }
        Library.load()
    }

    private val lock = Any()
    private var tickerHandle: Long = 0L

    val isRunning: Boolean get() = synchronized(lock) { tickerHandle != 0L }

    /**
     * Starts the native vsync tick thread for this window. Must not be called while already
     * running.
     *
     * Throws if this driver/display cannot actually support either mode - either because it
     * advertises neither `GLX_OML_sync_control` nor `GLX_SGI_video_sync`, or because a dedicated
     * direct `GLXContext` could not be obtained for it (see `VsyncTicker.cc`'s `createDedicatedContext`
     * - both extensions require one). The "unsupported" signal from the native side (a `0` ticker
     * handle) is surfaced as an exception here rather than a silently inert ticker, so callers can
     * fall back to another tick source.
     */
    fun start() {
        synchronized(lock) {
            check(tickerHandle == 0L) { "LinuxVsyncTicker is already running" }
            tickerHandle = nativeStart(display, window).also { handle ->
                check(handle != 0L) {
                    "Failed to start the native Linux vsync ticker: this display/driver supports " +
                        "neither GLX_OML_sync_control nor GLX_SGI_video_sync with a usable direct " +
                        "GLXContext"
                }
            }
        }
    }

    /**
     * Stops the native vsync tick thread, blocking until it has fully exited. Safe to call more
     * than once; a no-op if not running.
     *
     * This can block for up to one vblank period (waiting out an in-flight `glXWaitForMscOML`/
     * `glXWaitVideoSyncSGI` call on the tick thread, which - unlike the Windows sibling's
     * `WaitForSingleObject` - GLX offers no way to cancel early) - see `VsyncTicker.cc`.
     */
    fun stop() {
        val handle = synchronized(lock) {
            val current = tickerHandle
            tickerHandle = 0L
            current
        }
        if (handle != 0L) {
            nativeStop(handle)
        }
    }

    // Called from native code, on the dedicated native tick thread (see VsyncTicker.cc). Must not
    // acquire `lock` - see the class doc's thread-safety note.
    private fun onVsyncTick(vsyncTimeNanos: Long) {
        onTick(vsyncTimeNanos)
    }

    private external fun nativeStart(display: Long, window: Long): Long
    private external fun nativeStop(tickerHandle: Long)
}
