package org.jetbrains.skiko

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Component
import java.awt.GraphicsEnvironment
import java.lang.Long.max

private const val NanosecondsPerMillisecond = 1_000_000L

private const val MinMainstreamMonitorRefreshRate = 60

private val SwingGlobalFrameLimiter by lazy {
    val maxGlobalRefreshRate = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .screenDevices
        .maxOf { it.displayMode.refreshRate }
        .coerceAtLeast(MinMainstreamMonitorRefreshRate)

    FrameLimiter(
        CoroutineScope(Dispatchers.IO),
        1000L / maxGlobalRefreshRate
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
    private val frameMillis: Long,
    private val nanoTime: () -> Long = System::nanoTime
) {
    private val channel = RendezvousBroadcastChannel<Unit>()

    init {
        coroutineScope.launch {
            while (true) {
                channel.sendAll(Unit)
                preciseDelay(frameMillis)
            }
        }
    }

    private suspend fun preciseDelay(millis: Long) {
        var t = nanoTime()
        val startNanos = t
        // delay aren't precise, so we should measure what is the actual precision of delay is,
        // so we don't wait longer than we need
        var maxOverDelay = 0L
        while (t < startNanos + millis * NanosecondsPerMillisecond - maxOverDelay) {
            delay(1) // TODO do multiple delays instead of the single one consume more energy? Test it
            val time = nanoTime()
            val overDelay = (time - t) - 1
            maxOverDelay = max(maxOverDelay, overDelay)
            t = time
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