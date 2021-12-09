package org.jetbrains.skiko

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

private const val NanosecondsPerMillisecond = 1_000_000L

/**
 * Limit the duration of the frames (to avoid high CPU usage) to [frameMillis].
 * The actual delay depends on the precision of the system timer
 * (Windows has ~15ms precision by default, Linux/macOs ~2ms).
 * FrameLimiter will try to delay frames as close as possible to [frameMillis], but not greater
 */
@OptIn(ExperimentalTime::class)
class FrameLimiter(
    private val coroutineScope: CoroutineScope,
    private val frameMillis: () -> Long,
    private val nanoTime: () -> Long = System::nanoTime
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
        val start = nanoTime()
        // delay aren't precise, so we should measure what is the actual precision of delay is,
        // so we don't wait longer than we need
        var actual1msDelay = 1L

        while (nanoTime() - start <= millis * NanosecondsPerMillisecond - actual1msDelay) {
            val beforeDelay = nanoTime()
            delay(1) // TODO do multiple delays instead of the single one consume more energy? Test it
            actual1msDelay = maxOf(actual1msDelay, nanoTime() - beforeDelay)
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