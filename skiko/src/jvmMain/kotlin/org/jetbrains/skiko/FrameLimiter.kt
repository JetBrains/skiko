package org.jetbrains.skiko

import kotlinx.coroutines.*
import java.awt.Component
import java.awt.GraphicsEnvironment
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private const val NanosecondsPerMillisecond = 1_000_000L

@Volatile
private var maxGlobalRefreshRate = MinMainstreamMonitorRefreshRate

private val SwingGlobalFrameLimiter by lazy {
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        // TODO instead of getting max refresh rate of all displays and cache it, we should use individual frame limiters for each window, and update refresh rate when the display mode or the display is changed.
        //  we can't call it every frame, as it can be expensive (~3ms on Linux on my machine)
        maxGlobalRefreshRate = getMaxDisplayRefreshRate()
    }

    FrameLimiter(
        scope,
        { (1000L / maxGlobalRefreshRate).toLong() }
    )
}

@Suppress("UNUSED_PARAMETER")
internal fun FrameLimiter(component: Component) = SwingGlobalFrameLimiter

/**
 * Limit the duration of the frames (to avoid high CPU usage) to [frameMillis].
 * The actual delay depends on the precision of the system timer
 * (Windows has ~15ms precision by default, Linux/macOs ~2ms).
 * FrameLimiter will try to delay frames as close as possible to [frameMillis], but not greater
 */
@OptIn(ExperimentalTime::class)
class FrameLimiter(
    coroutineScope: CoroutineScope,
    private val frameMillis: () -> Long,
    private val nanoTime: () -> Long = System::nanoTime
) {
    private val channel = RendezvousBroadcastChannel<Unit>()

    init {
        coroutineScope.launch(Dispatchers.IO) {
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
        channel.receive()
    }
}