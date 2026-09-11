package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

/**
 * Limit the duration of the frames (to avoid high CPU usage) to [frameMillis].
 * The actual delay depends on the precision of the system timer
 * (Windows has ~15ms precision by default, Linux/macOs ~2ms).
 * FrameLimiter will try to delay frames as close as possible to [frameMillis], but not greater
 */
class FrameLimiter(
    private val coroutineScope: CoroutineScope,
    private val frameMillis: () -> Long,
    private val impreciseDelay: suspend (Long) -> Unit = ::delay,
    private val currentTime: () -> Duration = { System.nanoTime().nanoseconds }
) {
    private val channel = RendezvousBroadcastChannel<Unit>()

    init {
        coroutineScope.launch {
            while (true) {
                channel.sendAll(Unit)
                preciseDelay(frameMillis())
            }
        }
    }

    private suspend fun preciseDelay(millis: Long) {
        val start = currentTime()
        // delay isn't precise, so we should measure what the actual precision of delay is,
        // so we don't wait longer than we need
        var actual1msDelay = 1.milliseconds

        while (currentTime() - start <= millis.milliseconds - actual1msDelay) {
            val beforeDelay = currentTime()
            impreciseDelay(1) // TODO do multiple delays instead of the single one consume more energy? Test it
            actual1msDelay = maxOf(actual1msDelay, currentTime() - beforeDelay)
        }
    }

    /**
     * Await the next frame, if it is not ready yet (the previous [awaitNextFrame]
     * was called less than [frameMillis] ago)
     */
    suspend fun awaitNextFrame() {
        withContext(coroutineScope.coroutineContext) {
            channel.receive()
        }
    }
}