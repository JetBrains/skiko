package org.jetbrains.skiko

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext

/**
 * Dispatch frame after call of [scheduleFrame]
 */
class FrameDispatcher(
    context: CoroutineContext,
    private val onFrame: suspend () -> Unit
) {
    private var needFrame = CompletableDeferred<Unit>()

    private val job = GlobalScope.launch(context) {
        while (true) {
            needFrame.await()
            needFrame = CompletableDeferred()
            onFrame()
            yield()
        }
    }

    fun cancel() {
        job.cancel()
    }

    fun scheduleFrame() {
        needFrame.complete(Unit)
    }
}