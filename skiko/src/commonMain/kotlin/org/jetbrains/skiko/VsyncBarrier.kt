package org.jetbrains.skiko

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

/**
 * A synchronization barrier for handling vertical synchronization event.
 */
class VsyncBarrier {
    private val channel = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun signal() {
        channel.trySend(Unit)
    }

    suspend fun await() {
        channel.receive()
    }
}