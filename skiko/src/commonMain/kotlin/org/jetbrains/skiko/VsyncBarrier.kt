package org.jetbrains.skiko

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.internal.AtomicDesc
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A synchronization barrier for handling vertical synchronization event.
 */
class VsyncBarrier {
    private var channel: Channel<Unit>? = null

    fun enable() {
        disable()

        channel = Channel(1, BufferOverflow.DROP_OLDEST)
    }

    /**
     * Closes current [channel] if any, so possible current waiters on [await] can continue.
     */
    fun disable() {
        channel?.close()
        channel = null
    }

    fun signal() {
        channel?.trySend(Unit)
    }

    suspend fun await() {
        channel?.receive()
    }
}