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
    var gcDelayMillis = 10_000L
    var minFramesToRenderer = 1_000

    fun start() {
        // We initiate GC on IO threads, so that rendering is not blocked in
        // cases of concurrent collectors.
        println("Start frame watcher")
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                // Wait some time between collection attempts.
                delay(gcDelayMillis)
                println("Before do GC")
                System.gc()
                println("After do GC")
                // Ensure that certain number of frames were rendered, as we allocate Skia
                // garbage when rendering.
//                if (frameCounter.get() > minFramesToRenderer) {
//                    frameCounter.set(0)
//                }
            }
        }
    }

    fun nextFrame() {
        frameCounter.addAndGet(1)
    }

    private val frameCounter = AtomicInteger(0)
}
