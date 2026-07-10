package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.RendezvousBroadcastChannel


/**
 * A utility allowing several coroutines to wait for the next vsync.
 */
internal class MetalVSyncer(windowPtr: Long) {

    // The underlying throttler that blocks a thread
    private val displayLinkThrottler = DisplayLinkThrottler(windowPtr)

    private val channel = RendezvousBroadcastChannel<Unit>()

    // A channel to trigger the thread that waits on vsync to doing so
    private val triggerResumeOnVSync = Channel<Unit>(Channel.CONFLATED)

    /**
     * The predicted host time (in nanoseconds, derived from CVDisplayLink's outputTime) of the
     * display refresh that unblocked the most recent [waitForVSync], or 0 if unavailable
     * (e.g. no display link yet, or the wait was released due to system sleep).
     *
     * A vsync-driven frame ticker reads this after [waitForVSync] to time the next frame against the
     * display's refresh, falling back to the wall clock when it is 0.
     */
    var lastVSyncOutputTimeNanos: Long = 0
        private set

    private val job = CoroutineScope(dispatcherToBlockOn).launch {
        while (isActive) {
            triggerResumeOnVSync.receive()  // Suspend until needed
            lastVSyncOutputTimeNanos = displayLinkThrottler.waitVSync()  // This blocks (not suspends!) the thread
            if (isActive) {
                channel.sendAll(Unit)
            }
        }
    }.also {
        it.invokeOnCompletion {
            displayLinkThrottler.dispose()
        }
    }

    /**
     * Suspends until the next vsync.
     */
    suspend fun waitForVSync() {
        triggerResumeOnVSync.trySend(Unit)
        channel.receive()
    }

    fun dispose() {
        job.cancel()
    }
}

private class DisplayLinkThrottler(windowPtr: Long) {
    private val implPtr = create(windowPtr)

    fun dispose() = dispose(implPtr)

    /*
     * Creates a DisplayLink if needed with refresh rate matching NSScreen of NSWindow passed in [windowPtr].
     * If DisplayLink is already active, blocks until next vsync for physical screen of NSWindow passed in [windowPtr].
     */
    private external fun create(windowPtr: Long): Long

    /**
     * Blocks until the next vsync (or until there's no display link to wait for), returning the
     * predicted present time (host time, in nanoseconds) reported by CVDisplayLink's outputTime,
     * or 0 if unavailable.
     */
    fun waitVSync(): Long = waitVSync(implPtr)

    private external fun dispose(implPtr: Long)

    private external fun waitVSync(implPtr: Long): Long

    companion object {
        init {
            Library.load()
        }
    }
}
