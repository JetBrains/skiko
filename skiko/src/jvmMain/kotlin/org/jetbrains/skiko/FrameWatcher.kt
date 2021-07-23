package org.jetbrains.skiko

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class is intended to mitigate issues coming from the situation that we have
 * pretty large native peers (Skia objects) for rather tiny Java wrappers.
 * It is especially visible for situation with paragraph classes.
 * As a result, memory consumption grows dramatically.
 * To solve this issue, we force periodic GC if certain amount of frames was rendered,
 * making sure that we'll have memory consumption under control.
 */
internal object FrameWatcher {
    fun start() {
        // We initiate GC on IO threads, so that rendering is not blocked in
        // cases of concurrent collectors.
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                delay(5000L)
                if (frameCounter.get() > 1000) {
                    System.gc()
                    frameCounter.set(0)
                }
            }
        }
    }

    fun nextFrame() {
        frameCounter.addAndGet(1)
    }

    private val frameCounter = AtomicInteger(0)
}
