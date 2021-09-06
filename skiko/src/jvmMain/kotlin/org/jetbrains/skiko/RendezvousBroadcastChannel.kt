package org.jetbrains.skiko

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicReference

/**
 * Behaves as Channel<Unit>(Channel.RENDEZVOUS), but with ability to send value to all current consumers
 * (which await on `receive` method)
 */
internal class RendezvousBroadcastChannel<T> {
    private val onRequest = Channel<Unit>(Channel.CONFLATED)
    private val onResult = AtomicReference(CompletableDeferred<T>())

    /**
     * Send value to all current consumers which await value on `receive` method, or await for the first one
     */
    suspend fun sendAll(value: T) {
        onRequest.receive()
        onResult.getAndSet(CompletableDeferred()).complete(value)
    }

    /**
     * Wait when the producer will send a value and return it.
     */
    suspend fun receive(): T {
        onRequest.trySend(Unit)
        return onResult.get().await()
    }
}