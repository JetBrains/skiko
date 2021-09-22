package org.jetbrains.skiko

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Behaves as Channel<Unit>(Channel.RENDEZVOUS), but with ability to send value to all current consumers
 * (which await on `receive` method).
 */
internal class RendezvousBroadcastChannel<T> {
    private val onRequest = Channel<Unit>(Channel.CONFLATED)
    private val receivers = mutableListOf<Continuation<T>>()
    private val receiversCopy = mutableListOf<Continuation<T>>()

    /**
     * Send value to all current consumers which await value on `receive` method, or await for the first one.
     *
     * Can't be called concurrently from multiple threads.
     */
    suspend fun sendAll(value: T) {
        onRequest.receive()
        synchronized(receivers) {
            receiversCopy.addAll(receivers)
            receivers.clear()
        }
        for (receiver in receiversCopy) {
            receiver.resume(value)
        }
        receiversCopy.clear()
    }

    /**
     * Wait when the producer will send a value and return it.
     *
     * Can be called concurrently from multiple threads.
     */
    suspend fun receive(): T = suspendCancellableCoroutine { continuation ->
        synchronized(receivers) {
            receivers.add(continuation)
        }
        onRequest.trySend(Unit)
    }
}