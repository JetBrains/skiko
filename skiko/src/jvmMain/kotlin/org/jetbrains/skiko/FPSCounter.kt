package org.jetbrains.skiko

import java.awt.Component
import kotlin.math.roundToInt

internal class FPSCounter(
    private val periodSeconds: Double,
    private val showLongFrames: Boolean,
    private val getLongFrameMillis: () -> Double
) {
    private val times = mutableListOf<Long>()
    private var lastLogTime = System.nanoTime()
    private var lastTime = System.nanoTime()

    fun tick() {
        val time = System.nanoTime()
        val timestamp = time.nanosToMillis().toLong()
        val frameTime = time - lastTime
        lastTime = time

        times.add(frameTime)

        if (showLongFrames && frameTime > getLongFrameMillis().millisToNanos()) {
            println("[%d] Long frame %.2f ms".format(timestamp, frameTime.nanosToMillis()))
        }

        if ((time - lastLogTime) > periodSeconds.secondsToNanos()) {
            val average = (nanosPerSecond / times.average()).roundToInt()
            val min = (nanosPerSecond / times.max()!!).roundToInt()
            val max = (nanosPerSecond / times.min()!!).roundToInt()
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

internal fun defaultFPSCounter(
    component: Component
): FPSCounter? = with(SkikoProperties) {
    if (!SkikoProperties.fpsEnabled) return@with null

    // it is slow on Linux (100ms), so we cache it. Also refreshRate available only after window is visible
    val refreshRate by lazy { component.graphicsConfiguration.device.displayMode.refreshRate }

    FPSCounter(
        periodSeconds = fpsPeriodSeconds,
        showLongFrames = fpsLongFramesShow
    ) {
        when {
            fpsLongFramesShow && fpsLongFramesMillis != null -> fpsLongFramesMillis!!
            fpsLongFramesShow -> 1.5 * 1000 / refreshRate
            else -> 0.0
        }
    }
}