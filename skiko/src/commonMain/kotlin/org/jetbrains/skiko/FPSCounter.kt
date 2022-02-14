package org.jetbrains.skiko

import kotlin.math.roundToInt

class FPSCounter(
    private val periodSeconds: Double = 2.0,
    private val showLongFrames: Boolean = false,
    private val getLongFrameMillis: () -> Double = {
        1.5 * 1000 / 60
    }
) {
    private val times = mutableListOf<Long>()
    private var lastLogTime = currentNanoTime()
    private var lastTime = currentNanoTime()
    private var _average = 0
    private var _min = 0
    private var _max = 0

    fun tick() {
        val time = currentNanoTime()
        val timestamp = time.nanosToMillis().toLong()
        val frameTime = time - lastTime
        lastTime = time

        times.add(frameTime)

        if (showLongFrames && frameTime > getLongFrameMillis().millisToNanos()) {
            println("$timestamp Long frame ${frameTime.nanosToMillis()} ms")
        }

        if ((time - lastLogTime) > periodSeconds.secondsToNanos() && times.isNotEmpty()) {
            _average = (nanosPerSecond / times.average()).roundToInt()
            _min = (nanosPerSecond / times.maxOrNull()!!).roundToInt()
            _max = (nanosPerSecond / times.minOrNull()!!).roundToInt()
            times.clear()
            lastLogTime = time
        }
    }

    val average: Int
        get() = _average

    val min: Int
        get() = _min

    val max: Int
        get() = _max


    private val nanosPerMillis = 1_000_000.0
    private val nanosPerSecond = 1_000_000_000.0
    private fun Long.nanosToMillis(): Double = this / nanosPerMillis
    private fun Double.millisToNanos(): Long = (this * nanosPerMillis).toLong()
    private fun Double.secondsToNanos(): Long = (this * nanosPerSecond).toLong()
}