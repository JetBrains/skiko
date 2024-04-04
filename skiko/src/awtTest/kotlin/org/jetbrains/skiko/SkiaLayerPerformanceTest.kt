package org.jetbrains.skiko

import kotlinx.coroutines.delay
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.util.UiTestScope
import org.jetbrains.skiko.util.UiTestWindow
import org.jetbrains.skiko.util.uiTest
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.awt.Point
import javax.swing.WindowConstants
import kotlin.math.*

@Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
class SkiaLayerPerformanceTest {
    internal interface PerformanceHelper {
        val window: UiTestWindow
        val isCollected: Boolean
        fun startCollect()
        fun printInfo(assert: Boolean)
    }

    private fun UiTestScope.performanceHelper(
        scene: (canvas: Canvas, width: Int, height: Int, nanoTime: Long) -> Unit,
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
            window.layer.renderDelegate = object : SkikoRenderDelegate {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    scene(canvas, width, height, nanoTime)
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

        override fun printInfo(assert: Boolean) {
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
                renderApi == GraphicsApi.SOFTWARE_COMPAT ||
                renderApi == GraphicsApi.SOFTWARE_FAST
            ) {
                val slowFrames = frameTimeDeltas.filter { it > 1E9 / 55 }
                val fastFrames = frameTimeDeltas.filter { it < expectedFrameNanos * 0.5 }
                if (assert && slowFrames.size > deviatedTerminalCount) {
                    val str = slowFrames.map { String.format("%.1f", it / 1E6) }
                    throw AssertionError("Framerate is too low:\n$str")
                }
                if (assert && fastFrames.size > deviatedTerminalCount) {
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

                if (assert && deviatedTerminal.size > deviatedTerminalCount) {
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

    private val emptyScene = { canvas: Canvas, width: Int, height: Int, nanoTime: Long -> }

    private val clocksScene = { canvas: Canvas, width: Int, height: Int, nanoTime: Long ->
        val watchFill = Paint().apply { color = 0xFFFFFFFF.toInt() }
        val watchStroke = Paint().apply {
            color = 0xFF000000.toInt()
            mode = PaintMode.STROKE
            strokeWidth = 1f
        }
        val watchStrokeAA = Paint().apply {
            color = 0xFF000000.toInt()
            mode = PaintMode.STROKE
            strokeWidth = 1f
        }
        for (x in 0 .. (width - 50) step 50) {
            for (y in 20 .. (height - 50) step 50) {
                val stroke = if (x > width / 2) watchStrokeAA else watchStroke
                canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), watchFill)
                canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), stroke)
                var angle = 0f
                while (angle < 2f * PI) {
                    canvas.drawLine(
                        (x + 25 - 17 * sin(angle)),
                        (y + 25 + 17 * cos(angle)),
                        (x + 25 - 20 * sin(angle)),
                        (y + 25 + 20 * cos(angle)),
                        stroke
                    )
                    angle += (2.0 * PI / 12.0).toFloat()
                }
                val time = (nanoTime / 1E6) % 60000 +
                        (x.toFloat() / width * 5000).toLong() +
                        (y.toFloat() / width * 5000).toLong()

                val angle1 = (time.toFloat() / 5000 * 2f * PI).toFloat()
                canvas.drawLine(x + 25f, y + 25f,
                    x + 25f - 15f * sin(angle1),
                    y + 25f + 15 * cos(angle1),
                    stroke)

                val angle2 = (time / 60000 * 2f * PI).toFloat()
                canvas.drawLine(x + 25f, y + 25f,
                    x + 25f - 10f * sin(angle2),
                    y + 25f + 10f * cos(angle2),
                    stroke)
            }
        }
    }

    @Test
    fun `FPS is near display refresh rate (multiple windows)`() = uiTest {
        assumeTrue(System.getProperty("skiko.test.performance.enabled", "true") == "true")

        val helpers = (1..3).map { index ->
            performanceHelper(scene = emptyScene,
                              width = 40,
                              height = 20,
                              frameCount = 300,
                              deviatedTerminalCount = 20).apply {
                window.toFront()
                window.location = Point((index + 1) * 200, 200)
            }
        }
        delay(1000)
        try {
            helpers.forEach { it.startCollect() }
            awaitFrameCollection(helpers)
            helpers.forEach { it.printInfo(assert = true) }
        } finally {
            helpers.forEach { it.window.dispose() }
        }
    }

    @Test
    fun `FPS is near display refresh rate (multiple windows with clocks)`() = uiTest {
        assumeTrue(System.getProperty("skiko.test.performance.enabled", "true") == "true")
        val helpers = (1..3).map { index ->
            performanceHelper(
                scene = clocksScene,
                width = 300,
                height = 300,
                frameCount = 300,
                deviatedTerminalCount = 20
            ).apply {
                window.toFront()
                window.location = Point(index * 300, 200)
            }
        }
        delay(1000)
        try {
            helpers.forEach { it.startCollect() }
            awaitFrameCollection(helpers)
            helpers.forEach { it.printInfo(assert = false) }
        } finally {
            helpers.forEach { it.window.dispose() }
        }
    }
}