package org.jetbrains.skiko

import kotlin.math.roundToInt

class FPSCounter(
    private val periodSeconds: Double,
    private val showLongFrames: Boolean,
    private val getLongFrameMillis: () -> Double = {
        1.5 * 1000 / 60
    }
) {
    private val times = mutableListOf<Long>()
    private var lastLogTime = currentNanoTime()
    private var lastTime = currentNanoTime()

    fun tick() {
        val time = currentNanoTime()
        val timestamp = time.nanosToMillis().toLong()
        val frameTime = time - lastTime
        lastTime = time

        times.add(frameTime)

        if (showLongFrames && frameTime > getLongFrameMillis().millisToNanos()) {
            println("$timestamp Long frame ${frameTime.nanosToMillis()} ms")
        }

        if ((time - lastLogTime) > periodSeconds.secondsToNanos()) {
            val average = (nanosPerSecond / times.average()).roundToInt()
            val min = (nanosPerSecond / times.maxOrNull()!!).roundToInt()
            val max = (nanosPerSecond / times.minOrNull()!!).roundToInt()
            println("[$timestamp] FPS $average ($min-$max)")
            times.clear()
            lastLogTime = time
        }
    }

    private val nanosPerMillis = 1_000_000.0
    private val nanosPerSecond = 1_000_000_000.0
    private fun Long.nanosToMillis(): Double = this / nanosPerMillis
    private fun Double.millisToNanos(): Long = (this * nanosPerMillis).toLong()
    private fun Double.secondsToNanos(): Long = (this * nanosPerSecond).toLong()
}