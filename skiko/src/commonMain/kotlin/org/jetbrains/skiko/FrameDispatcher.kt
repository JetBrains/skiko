package org.jetbrains.skiko

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext

/**
 * Dispatch frame after call of [scheduleFrame].
 *
 * After the creating there should be no scheduled frame in the frame loop.
 */
class FrameDispatcher(
    scope: CoroutineScope,
    private val vsyncBarrier: VsyncBarrier? = null,
    private val overCommitmentBarrier: OverCommitmentBarrier? = null,
    private val onFrame: suspend () -> Unit
) {
    constructor(
        context: CoroutineContext,
        vsyncBarrier: VsyncBarrier? = null,
        overCommitmentBarrier: OverCommitmentBarrier? = null,
        onFrame: suspend () -> Unit
    ) : this(
        CoroutineScope(context),
        vsyncBarrier,
        overCommitmentBarrier,
        onFrame
    )

    private val frameChannel = Channel<Unit>(Channel.CONFLATED)
    private var frameScheduled = false

    private val job = scope.launch {
        while (true) {
            // Await for draw request
            frameChannel.receive()

            // Await for vsync
            vsyncBarrier?.await()

            // Await for available slot for commiting new command
            overCommitmentBarrier?.await()

            frameScheduled = false
            onFrame()
            // As per `yield()` documentation:
            //
            // For other dispatchers (not == Unconfined) , this function calls [CoroutineDispatcher.dispatch] and
            // always suspends to be resumed later regardless of the result of [CoroutineDispatcher.isDispatchNeeded].
            //
            // What means for Swing dispatcher we'll process all pending events and resume renderer.
            yield()
        }
    }

    fun cancel() {
        job.cancel()
    }

    /**
     * Schedule next frame to render in the frame loop.
     *
     * Multiple calls of `scheduleFrame` before beginning of the frame will cause only one `onFrame`.
     *
     * Multiple calls of `scheduleFrame` after beginning of the frame but before its ending
     * will schedule next single `onFrame` after the current one.
     */
    fun scheduleFrame() {
        if (!frameScheduled) {
            frameScheduled = true
            frameChannel.trySend(Unit).isSuccess
        }
    }
}