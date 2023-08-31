package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Limit the duration of the frames (to avoid high CPU usage) to [frameMillis].
 * The actual delay depends on the precision of the system timer
 * (Windows has ~15ms precision by default, Linux/macOs ~2ms).
 * FrameLimiter will try to delay frames as close as possible to [frameMillis], but not greater
 */
@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class FrameLimiter(
    private val coroutineScope: CoroutineScope,
    private val frameMillis: () -> Long,
    private val dispatcherToBlockOn: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(64),
    private val impreciseDelay: suspend (Long) -> Unit = ::delay,
    private val timeSource: TimeSource = TimeSource.Monotonic
) {
    private val channel = RendezvousBroadcastChannel<Unit>()

    init {
        coroutineScope.launch(dispatcherToBlockOn) {
            while (true) {
                channel.sendAll(Unit)
                preciseDelay(frameMillis())
            }
        }
    }

    private suspend fun preciseDelay(millis: Long) {
        val start = timeSource.markNow()
        // delay aren't precise, so we should measure what is the actual precision of delay is,
        // so we don't wait longer than we need
        var actual1msDelay = 1.milliseconds

        while (start.elapsedNow() <= millis.milliseconds - actual1msDelay) {
            val beforeDelay = timeSource.markNow()
            impreciseDelay(1) // TODO do multiple delays instead of the single one consume more energy? Test it
            actual1msDelay = maxOf(actual1msDelay, beforeDelay.elapsedNow())
        }
    }

    /**
     * Await the next frame, if it is not ready yet (the previous [awaitNextFrame]
     * was called less than [frameMillis] ago)
     */
    suspend fun awaitNextFrame() {
        withContext(coroutineScope.coroutineContext + dispatcherToBlockOn) {
            channel.receive()
        }
    }
}