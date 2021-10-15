package org.jetbrains.skiko

import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean

internal class Task {
    private val onFinish = Channel<Unit>(1)

    private var done = AtomicBoolean(true)

    /**
     * Run task and await its finishing (i.e. calling of [finish])
     */
    suspend fun runAndAwait(run: suspend () -> Unit) {
        done.set(false)
        run()
        onFinish.receive()
    }

    /**
     * Finish running task. If there is no running task, do nothing.
     */
    fun finish() {
        if (!done.getAndSet(true)) {
            onFinish.trySend(Unit)
        }
    }
}