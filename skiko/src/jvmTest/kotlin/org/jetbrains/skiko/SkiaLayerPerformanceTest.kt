package org.jetbrains.skiko

import kotlinx.coroutines.delay
import org.jetbrains.skia.*
import org.jetbrains.skiko.util.UiTestScope
import org.jetbrains.skiko.util.UiTestWindow
import org.jetbrains.skiko.util.uiTest
import org.junit.Test
import java.awt.Point
import javax.swing.WindowConstants
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
class SkiaLayerPerformanceTest {
    internal interface PerformanceHelper {
        val window: UiTestWindow
        val isCollected: Boolean
        fun startCollect()
        fun printInfo()
    }

    private fun UiTestScope.performanceHelper(
        width: Int,
        height: Int,
        frameCount: Int,
        deviatedTerminalCount: Int
    ) = object : PerformanceHelper {
        override val window = UiTestWindow()

        private val expectedDeviatePercent1 = 0.05
        private val expectedDeviatePercent2 = 0.15
        private val expectedDeviatePercent3 = 0.30
        private val expectedDeviatePercentTerminal = 0.50

        private val expectedFrameNanos get() = 1E9 / window.layer.backedLayer.getDisplayRefreshRate()
        private val frameTimes = mutableListOf<Long>()
        private var canCollect = false

        val frameTimeDeltas get() = frameTimes.zipWithNext { a, b -> b - a }

        override val isCollected get() = frameTimes.size >= frameCount

        private fun deviated(deviatePercent: Double) = frameTimeDeltas.filter {
            abs(log2(it / expectedFrameNanos)) > log2(1 + deviatePercent)
        }

        init {
            window.setSize(width, height)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.skikoView = object : SkikoView {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    if (canCollect && frameTimes.size < frameCount) {
                        frameTimes.add(System.nanoTime()) // we check the real time, not the time provided by the argument
                    }
                    window.layer.needRedraw()
                }
            }
            window.isUndecorated = true
            window.isVisible = true
        }

        override fun startCollect() {
            canCollect = true
        }

        override fun printInfo() {
            println("[Window frame times ($frameCount frames)]")
            val millis = frameTimeDeltas.map { it / 1E6 }
            println("Deltas " + millis.map { String.format("%.1f", it) })
            println("Average %.2f".format(millis.average()))
            println("Standard deviation %.2f".format(millis.stddev()))

            fun deviateMessage(percent: Double, deviated: List<Long>): String {
                val deviatedStr = deviated.map { String.format("%.1f", it / 1E6) }
                val percentTStr = (percent * 100).roundToInt()
                return "$deviatedStr deviate by $percentTStr%"
            }

            if (
                renderApi == GraphicsApi.SOFTWARE ||
                renderApi == GraphicsApi.DIRECT_SOFTWARE
            ) {
                val slowFrames = frameTimeDeltas.filter { it > 1E9 / 55 }
                val fastFrames = frameTimeDeltas.filter { it < expectedFrameNanos * 0.5 }
                if (slowFrames.size > deviatedTerminalCount) {
                    val str = slowFrames.map { String.format("%.1f", it / 1E6) }
                    throw AssertionError("Framerate is too low:\n$str")
                }
                if (fastFrames.size > deviatedTerminalCount) {
                    val str = fastFrames.map { String.format("%.1f", it / 1E6) }
                    throw AssertionError("Framerate is too high:\n$str")
                }
            } else {
                val deviated1 = deviated(expectedDeviatePercent1)
                val deviated2 = deviated(expectedDeviatePercent2)
                val deviated3 = deviated(expectedDeviatePercent3)
                val deviatedTerminal = deviated(expectedDeviatePercentTerminal)

                println(deviateMessage(expectedDeviatePercent1, deviated1 - deviated2 - deviated3 - deviatedTerminal))
                println(deviateMessage(expectedDeviatePercent2, deviated2 - deviated3 - deviatedTerminal))
                println(deviateMessage(expectedDeviatePercent3, deviated3 - deviatedTerminal))

                if (deviatedTerminal.size > deviatedTerminalCount) {
                    throw AssertionError(deviateMessage(expectedDeviatePercentTerminal, deviatedTerminal))
                } else {
                    println(deviateMessage(expectedDeviatePercentTerminal, deviatedTerminal))
                }
            }

            println()
        }

        private fun List<Double>.stddev(): Double {
            val average = average()
            fun f(x: Double) = (x - average) * (x - average)
            return sqrt(map(::f).average())
        }
    }

    private suspend fun awaitFrameCollection(windows: List<PerformanceHelper>) {
        while (!windows.all(PerformanceHelper::isCollected)) {
            delay(100)
        }
    }

    @Test
    fun `FPS is near display refresh rate (multiple windows)`() = uiTest {
        val helpers = (1..3).map { index ->
            performanceHelper(width = 40, height = 20, frameCount = 300, deviatedTerminalCount = 20).apply {
                window.toFront()
                window.location = Point((index + 1) * 200, 200)
            }
        }
        delay(1000)
        try {
            helpers.forEach { it.startCollect() }
            awaitFrameCollection(helpers)
            helpers.forEach { it.printInfo() }
        } finally {
            helpers.forEach { it.window.dispose() }
        }
    }
}