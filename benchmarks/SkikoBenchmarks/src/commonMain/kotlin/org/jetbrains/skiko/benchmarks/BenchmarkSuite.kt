package org.jetbrains.skiko.benchmarks

import org.jetbrains.skiko.benchmarks.cases.drawing.clipTransformDrawBenchmark
import org.jetbrains.skiko.benchmarks.cases.drawing.rectGridDrawBenchmark
import org.jetbrains.skiko.benchmarks.cases.image.imageScaleDrawBenchmark
import org.jetbrains.skiko.benchmarks.cases.image.imageScaleDrawGpuBenchmark
import org.jetbrains.skiko.benchmarks.cases.image.imageSnapshotEncodeBenchmark
import org.jetbrains.skiko.benchmarks.cases.path.pathBooleanOpsBenchmark
import org.jetbrains.skiko.benchmarks.cases.path.pathParseAndDrawBenchmark
import org.jetbrains.skiko.benchmarks.cases.readback.surfaceReadPixelsBenchmark
import org.jetbrains.skiko.benchmarks.cases.surface.surfaceAllocationBenchmark
import org.jetbrains.skiko.benchmarks.cases.text.textBlobDrawBenchmark
import kotlin.math.roundToLong
import kotlin.time.TimeSource

data class BenchmarkReport(
    val platform: String,
    val versionInfo: String?,
    val results: List<BenchmarkResult>,
) {
    fun toJson(): String = buildString {
        append("{\n")
        append("  \"platform\": \"").append(platform.escapeJson()).append("\",\n")
        versionInfo?.let {
            append("  \"versionInfo\": \"").append(it.escapeJson()).append("\",\n")
        }
        append("  \"results\": [\n")
        results.forEachIndexed { index, result ->
            append(result.toJson("    "))
            if (index != results.lastIndex) append(",")
            append("\n")
        }
        append("  ]\n")
        append("}")
    }

    fun prettyPrint() {
        println()
        println("=== Skiko Benchmark Results ===")
        println("Platform: $platform")
        versionInfo?.let { println("Version: $it") }
        println("Benchmark                         | mode            | avg ms   | min ms   | max ms   | checksum")
        println("-".repeat(104))
        results.forEach { result ->
            println(
                "${result.name.padEnd(33)} | " +
                        "${result.mode.argumentName.padEnd(15)} | " +
                        "${result.averageMillis.formatMillis().padStart(8)} | " +
                        "${result.minMillis.formatMillis().padStart(8)} | " +
                        "${result.maxMillis.formatMillis().padStart(8)} | " +
                        result.checksum
            )
        }
        println()
    }
}

data class BenchmarkResult(
    val name: String,
    val mode: BenchmarkMode,
    val warmups: Int,
    val iterations: Int,
    val averageMillis: Double,
    val medianMillis: Double,
    val minMillis: Double,
    val maxMillis: Double,
    val checksum: Long,
) {
    fun toJson(indent: String): String = buildString {
        append(indent).append("{\n")
        append(indent).append("  \"name\": \"").append(name.escapeJson()).append("\",\n")
        append(indent).append("  \"mode\": \"").append(mode.argumentName).append("\",\n")
        append(indent).append("  \"warmups\": ").append(warmups).append(",\n")
        append(indent).append("  \"iterations\": ").append(iterations).append(",\n")
        append(indent).append("  \"averageMillis\": ").append(averageMillis.formatMillis()).append(",\n")
        append(indent).append("  \"medianMillis\": ").append(medianMillis.formatMillis()).append(",\n")
        append(indent).append("  \"minMillis\": ").append(minMillis.formatMillis()).append(",\n")
        append(indent).append("  \"maxMillis\": ").append(maxMillis.formatMillis()).append(",\n")
        append(indent).append("  \"checksum\": ").append(checksum).append("\n")
        append(indent).append("}")
    }

    fun toReportJson(platform: String, versionInfo: String?): String =
        BenchmarkReport(platform, versionInfo, listOf(this)).toJson()
}

class BenchmarkCase(
    val name: String,
    val warmups: Int = 5,
    val iterations: Int = 25,
    val isSupported: () -> Boolean = { true },
    val tearDown: () -> Unit = {},
    val operation: () -> Long,
)

object SkikoBenchmarkSuite {
    fun benchmarkNames(): List<String> = cases.map { it.name }

    fun run(
        platform: String,
        config: BenchmarkConfig = BenchmarkConfig(),
    ): BenchmarkReport {
        val requestedCases = if (config.benchmarks.isNullOrEmpty()) {
            cases
        } else {
            val normalized = config.benchmarks.map { it.lowercase() }.toSet()
            cases.filter { it.name.lowercase() in normalized }
        }

        require(requestedCases.isNotEmpty()) {
            "No Skiko benchmarks matched ${config.benchmarks.orEmpty().joinToString(",")}"
        }

        val selectedCases = requestedCases.filter { it.isSupported() }
        require(selectedCases.isNotEmpty()) {
            "Requested Skiko benchmarks are not supported on this platform: " +
                    requestedCases.joinToString(", ") { it.name }
        }

        return BenchmarkReport(
            platform = platform,
            versionInfo = config.versionInfo,
            results = selectedCases.flatMap { case ->
                try {
                    config.modes.sortedBy { it.executionOrder }.map { mode ->
                        case.run(mode, config)
                    }
                } finally {
                    case.tearDown()
                }
            }
        )
    }

    private val cases = listOf(
        surfaceAllocationBenchmark,
        rectGridDrawBenchmark,
        pathParseAndDrawBenchmark,
        imageSnapshotEncodeBenchmark,
        imageScaleDrawBenchmark,
        imageScaleDrawGpuBenchmark,
        clipTransformDrawBenchmark,
        pathBooleanOpsBenchmark,
        surfaceReadPixelsBenchmark,
        textBlobDrawBenchmark,
    )

    private fun BenchmarkCase.run(mode: BenchmarkMode, config: BenchmarkConfig): BenchmarkResult =
        when (mode) {
            BenchmarkMode.SIMPLE -> runRepeated(BenchmarkMode.SIMPLE, config)
            BenchmarkMode.STARTUP -> runStartup()
        }

    private fun BenchmarkCase.runRepeated(mode: BenchmarkMode, config: BenchmarkConfig): BenchmarkResult {
        val warmups = config.warmupsOverride ?: warmups
        val iterations = config.iterationsOverride ?: iterations

        repeat(warmups) { operation() }

        var checksum = 0L
        val samples = DoubleArray(iterations)
        repeat(iterations) { index ->
            val mark = TimeSource.Monotonic.markNow()
            checksum += operation()
            samples[index] = mark.elapsedNow().inWholeNanoseconds / 1_000_000.0
        }

        return BenchmarkResult(
            name = modeName(mode),
            mode = mode,
            warmups = warmups,
            iterations = iterations,
            averageMillis = samples.average(),
            medianMillis = samples.median(),
            minMillis = samples.minOrNull() ?: 0.0,
            maxMillis = samples.maxOrNull() ?: 0.0,
            checksum = checksum,
        )
    }

    private fun BenchmarkCase.runStartup(): BenchmarkResult {
        val mark = TimeSource.Monotonic.markNow()
        val checksum = operation()
        val elapsedMillis = mark.elapsedNow().inWholeNanoseconds / 1_000_000.0

        return BenchmarkResult(
            name = modeName(BenchmarkMode.STARTUP),
            mode = BenchmarkMode.STARTUP,
            warmups = 0,
            iterations = 1,
            averageMillis = elapsedMillis,
            medianMillis = elapsedMillis,
            minMillis = elapsedMillis,
            maxMillis = elapsedMillis,
            checksum = checksum,
        )
    }

    private fun BenchmarkCase.modeName(mode: BenchmarkMode): String =
        mode.resultSuffix?.let { "${name}_$it" } ?: name
}

private val BenchmarkMode.executionOrder: Int
    get() = when (this) {
        BenchmarkMode.STARTUP -> 0
        BenchmarkMode.SIMPLE -> 1
    }

private fun DoubleArray.median(): Double {
    require(isNotEmpty()) { "Cannot calculate median of an empty array" }

    val sorted = sortedArray()
    val middle = sorted.size / 2

    return if (sorted.size % 2 == 0) {
        (sorted[middle - 1] + sorted[middle]) / 2
    } else {
        sorted[middle]
    }
}

private fun String.escapeJson(): String =
    replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

private fun Double.formatMillis(): String =
    (this * 1000.0).roundToLong().let { rounded ->
        val whole = rounded / 1000
        val fraction = (rounded % 1000).toString().padStart(3, '0')
        "$whole.$fraction"
    }
