package org.jetbrains.skiko

import kotlinx.coroutines.*

private const val NANOS_PER_MILLIS = 1_000_000L

/**
 * Limit the duration of the frames (to avoid high CPU usage) to [frameMillis].
 * The actual delay depends on the precision of the system timer
 * (Windows has ~15ms precision by default, Linux/macOS ~2ms).
 *
 * FrameLimiter will try to limit frames as close as possible to [frameMillis],
 * but never more than that.
 */
class FrameLimiter(
    private val coroutineScope: CoroutineScope,
    private val frameMillis: () -> Long,
    private val nanoTime: () -> Long = System::nanoTime
) {
    private val channel = RendezvousBroadcastChannel<Unit>()

    init {
        coroutineScope.launch(context = CoroutineName("FrameLimiter")) {
            while (isActive) {
                channel.sendAll(Unit)
                preciseDelay(frameMillis())
            }
        }
    }

    private suspend fun preciseDelay(millis: Long) {
        val start = nanoTime()
        // Delays aren't precise, so we should measure what is the actual precision of delay is,
        // so we don't wait longer than we need
        var actual1msDelay = 1L

        while (nanoTime() - start <= millis * NANOS_PER_MILLIS - actual1msDelay) {
            val beforeDelay = nanoTime()
            delay(1) // TODO do multiple delays instead of the single one consume more energy? Test it
            actual1msDelay = maxOf(actual1msDelay, nanoTime() - beforeDelay)
        }
    }

    /**
     * Await the next frame if it is not ready yet (the previous [awaitNextFrame]
     * was called less than [frameMillis] ago)
     */
    suspend fun awaitNextFrame() {
        withContext(coroutineScope.coroutineContext + CoroutineName("FrameLimiter-Await")) {
            channel.receive()
        }
    }
}
