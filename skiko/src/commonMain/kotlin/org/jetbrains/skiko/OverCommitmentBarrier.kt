package org.jetbrains.skiko

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

/**
 * Barrier for preventing more than [MAX_INFLIGHT_COMMAND_BUFFERS] being commited at the same time.
 * It prevents situation where CPU encodes more commands than GPU can process (GPU bottleneck)
 */
class OverCommitmentBarrier {
    private val channel = Channel<Unit>(capacity = MAX_INFLIGHT_COMMAND_BUFFERS, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        repeat(MAX_INFLIGHT_COMMAND_BUFFERS) {
            signal()
        }
    }

    fun signal() {
        channel.trySend(Unit)
    }

    suspend fun await() {
        channel.receive()
    }

    companion object {
        const val MAX_INFLIGHT_COMMAND_BUFFERS = 3
    }
}