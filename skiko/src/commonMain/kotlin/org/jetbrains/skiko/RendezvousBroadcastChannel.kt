package org.jetbrains.skiko

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.*

/**
 * Behaves like `Channel<Unit>(Channel.RENDEZVOUS)`, but with the ability to send a value to all current consumers
 * suspended in [receive].
 */
internal class RendezvousBroadcastChannel<T> {
    private val onRequest = Channel<Unit>(Channel.CONFLATED)
    private var suspended = mutableListOf<Continuation<T>>()
    private var suspendedCopy = mutableListOf<Continuation<T>>()

    /**
     * Send value to all current consumers that await value on `receive` method, or await for the first one.
     *
     * Can't be called concurrently from multiple threads.
     */
    suspend fun sendAll(value: T) {
        onRequest.receive()

        // Swap the lists
        maybeSynchronized(this) {
            val tmp = suspended
            suspended = suspendedCopy
            suspendedCopy = tmp
        }

        // Safe to touch `suspendedCopy` without lock because receive will now add to `suspended`.
        for (cont in suspendedCopy) {
            cont.resume(value)
        }
        suspendedCopy.clear()
    }

    /**
     * Wait until the producer sends a value, and returns it.
     *
     * Can be called concurrently from multiple threads.
     */
    suspend fun receive(): T = suspendCancellableCoroutine { continuation ->
        maybeSynchronized(this) {
            suspended.add(continuation)
        }
        onRequest.trySend(Unit)
    }
}
