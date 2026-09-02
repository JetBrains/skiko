package org.jetbrains.skiko.swing

import org.jetbrains.skiko.Library
import java.awt.GraphicsEnvironment
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Not a correctness test: measures tick-interval statistics of the Skiko pacing clocks — the
 * platform-native clock via [SkikoFramePacingService] and, for comparison, a raw [TimerClock] at
 * the same period — and writes them to `build/reports/pacing-probe.txt`.
 *
 * Opt-in via the `SKIKO_PACING_PROBE=true` environment variable so normal test runs skip it.
 */
class SkikoFramePacingJitterProbe {

    @Test
    fun `measure tick interval statistics`() {
        assumeTrue(System.getenv("SKIKO_PACING_PROBE") == "true")
        assumeFalse(GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance)
        Library.load()

        val service = SkikoFramePacingService.instance
        assertNotNull(service)

        val lines = mutableListOf<String>()
        // Every connected display, so mixed-refresh setups get per-display numbers.
        for (device in GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices) {
            val gc = device.defaultConfiguration ?: continue
            val displayId = service.displayId(gc)
            if (displayId == -1L) continue
            val periodNanos = service.refreshPeriodNanos(displayId)

            lines += "display id=$displayId (${device.iDstring}) nominal=" +
                    "${"%.3f".format(periodNanos / 1e6)}ms " +
                    "(${if (periodNanos > 0) "%.2f".format(1e9 / periodNanos) else "?"} Hz)"

            lines += measure("  native") { onTick ->
                val subscription = service.subscribe(displayId) { _, t -> onTick(t) }
                assertNotNull(subscription)
                AutoCloseable { subscription.close() }
            }

            lines += measure("  timer ") { onTick ->
                val clock = TimerClock(displayId, if (periodNanos > 0) periodNanos else 16_666_667L)
                val listener: (Long, Long) -> Unit = { _, t -> onTick(t) }
                clock.add(listener)
                AutoCloseable { clock.remove(listener) }
            }
        }

        val report = File("build/reports/pacing-probe.txt")
        report.parentFile.mkdirs()
        report.writeText(lines.joinToString("\n") + "\n")
        lines.forEach(::println)
    }

    private fun measure(label: String, subscribe: ((Long) -> Unit) -> AutoCloseable): String {
        val samples = 600
        val stamps = LongArray(samples)
        var index = 0
        val done = CountDownLatch(1)

        val subscription = subscribe { timeNanos ->
            if (index < samples) {
                stamps[index++] = timeNanos
                if (index == samples) done.countDown()
            }
        }
        try {
            assertTrue(done.await(30, TimeUnit.SECONDS), "$label probe timed out")
        } finally {
            subscription.close()
        }

        val intervals = DoubleArray(samples - 1) { (stamps[it + 1] - stamps[it]) / 1e6 }
        intervals.sort()
        val mean = intervals.average()
        val stddev = sqrt(intervals.map { (it - mean) * (it - mean) }.average())
        val p99 = intervals[((samples - 1) * 99) / 100]
        return "$label mean=${"%.3f".format(mean)}ms (${"%.2f".format(1000.0 / mean)} Hz) " +
                "stddev=${"%.3f".format(stddev)}ms p99=${"%.3f".format(p99)}ms over ${samples - 1} intervals"
    }
}
