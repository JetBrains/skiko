package org.jetbrains.skiko

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Component
import java.awt.GraphicsEnvironment

private const val NanosecondsPerMillisecond = 1_000_000L

private const val MinMainstreamMonitorRefreshRate = 60

@Volatile
private var maxGlobalRefreshRate = MinMainstreamMonitorRefreshRate

private val SwingGlobalFrameLimiter by lazy {
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        maxGlobalRefreshRate = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .screenDevices
            .maxOf { it.displayMode.refreshRate }
            .coerceAtLeast(MinMainstreamMonitorRefreshRate)
    }

    FrameLimiter(
        scope,
        { 1000L / maxGlobalRefreshRate }
    )
}

// TODO because it can be slow on some Linux'es (~100ms), we use global frame limiter instead of the local ones.
//  but it doesn't refresh when user change frame rate or plug external monitors.
@Suppress("UNUSED_PARAMETER")
internal fun FrameLimiter(component: Component) = SwingGlobalFrameLimiter

/**
 * Limit the duration of the frames (to avoid high CPU usage) to [frameMillis].
 * The actual delay depends on the precision of the system timer
 * (Windows has ~15ms precision by default, Linux/macOs ~2ms).
 * FrameLimiter will try to delay frames as close as possible to [frameMillis], but not greater
 */
class FrameLimiter(
    coroutineScope: CoroutineScope,
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
        channel.receive()
    }
}