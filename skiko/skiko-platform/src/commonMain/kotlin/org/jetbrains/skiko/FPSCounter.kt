package org.jetbrains.skiko

import kotlin.math.roundToInt

class FPSCounter(
    private val periodSeconds: Double = 2.0,
    private val showLongFrames: Boolean = false,
    private val getLongFrameMillis: () -> Double = {
        1.5 * 1000 / 60
    },
    private val logOnTick: Boolean = false
) {
    private val times = mutableListOf<Long>()
    private var lastLogTime = currentNanoTime()
    private var lastTime = currentNanoTime()
    var average = 0
        private set
    var min = 0
        private set
    var max = 0
        private set

    fun tick() {
        val time = currentNanoTime()
        val timestamp = time.nanosToMillis().toLong()
        val frameTime = time - lastTime
        lastTime = time

        times.add(frameTime)

        if (logOnTick && showLongFrames && frameTime > getLongFrameMillis().millisToNanos()) {
            println("$timestamp Long frame ${frameTime.nanosToMillis()} ms")
        }

        if ((time - lastLogTime) > periodSeconds.secondsToNanos() && times.isNotEmpty()) {
            average = (nanosPerSecond / times.average()).roundToInt()
            min = (nanosPerSecond / times.maxOrNull()!!).roundToInt()
            max = (nanosPerSecond / times.minOrNull()!!).roundToInt()
            times.clear()
            lastLogTime = time
            if (logOnTick) {
                println("[$timestamp] FPS $average ($min-$max)")
            }
        }
    }

    private val nanosPerMillis = 1_000_000.0
    private val nanosPerSecond = 1_000_000_000.0
    private fun Long.nanosToMillis(): Double = this / nanosPerMillis
    private fun Double.millisToNanos(): Long = (this * nanosPerMillis).toLong()
    private fun Double.secondsToNanos(): Long = (this * nanosPerSecond).toLong()
}