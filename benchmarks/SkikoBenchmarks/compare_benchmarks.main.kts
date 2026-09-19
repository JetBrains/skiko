#!/usr/bin/env kotlin

@file:Import("benchmark_project.main.kts")

/**
 * Usage:
 *   ./compare_benchmarks.main.kts v1=<version1> [v2=<version2>] [runs=3] [benchmarks=<name1,name2>] [platform=web|jvm] [modes=SIMPLE|STARTUP] [metric=average|median] [skipExisting=true]
 */

val projectDir = findBenchmarkProjectDir()
val CURRENT_CHECKOUT_VERSION_LABEL = "current"

data class BenchmarkTarget(
    val version: String,
    val composite: Boolean = false,
) {
    val description: String
        get() = if (composite) "$version (current checkout)" else version
}

enum class CompareMetric(
    val argumentName: String,
    val jsonFieldName: String,
    val columnName: String,
) {
    AVERAGE("average", "averageMillis", "avg ms"),
    MEDIAN("median", "medianMillis", "median ms");

    companion object {
        fun fromArgument(value: String?): CompareMetric {
            val normalized = value?.trim()?.lowercase().orEmpty()
            return when (normalized) {
                "", "average", "avg", "averagemillis" -> AVERAGE
                "median", "med", "medianmillis" -> MEDIAN
                else -> throw IllegalArgumentException(
                    "Unsupported metric: $value. Supported: average, median"
                )
            }
        }
    }
}

enum class CompareBenchmarkMode(val argumentName: String, val resultSuffix: String?) {
    SIMPLE("SIMPLE", null),
    STARTUP("STARTUP", "startup");

    companion object {
        fun fromArgument(value: String?): Set<CompareBenchmarkMode> {
            if (value.isNullOrBlank()) return setOf(SIMPLE)

            return value
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { mode ->
                    entries.find { it.argumentName.equals(mode, ignoreCase = true) }
                        ?: throw IllegalArgumentException(
                            "Unsupported mode: $mode. Supported: ${entries.joinToString { it.argumentName }}"
                        )
                }
                .toSet()
                .ifEmpty { setOf(SIMPLE) }
        }
    }
}

fun main(args: Array<String>) {
    val argMap = args.associate {
        val split = it.split("=", limit = 2)
        if (split.size == 2) split[0] to split[1] else it to ""
    }
    val positionalArgs = args.filter { !it.contains("=") }

    val v1 = argMap["v1"] ?: positionalArgs.getOrNull(0)
    val v2 = argMap["v2"] ?: positionalArgs.getOrNull(1)

    if (v1 == null) {
        println("Usage: ./compare_benchmarks.main.kts v1=<version1> [v2=<version2>] [runs=3] [benchmarks=<name1,name2>] [platform=web|jvm] [modes=SIMPLE|STARTUP] [metric=average|median] [skipExisting=true]")
        return
    }

    val targetV1 = BenchmarkTarget(version = v1)
    val targetV2 = if (v2 == null) {
        BenchmarkTarget(version = CURRENT_CHECKOUT_VERSION_LABEL, composite = true)
    } else {
        BenchmarkTarget(version = v2)
    }

    val runs = (argMap["runs"] ?: positionalArgs.getOrNull(2))?.toIntOrNull() ?: 3
    val benchmarkName = argMap["benchmarks"]
    val platform = argMap["platform"] ?: "web"
    val modes = CompareBenchmarkMode.fromArgument(argMap["modes"])
    val metric = CompareMetric.fromArgument(argMap["metric"])
    val skipExisting = argMap["skipExisting"]?.toBoolean() ?: false
    val requestedBenchmarks = benchmarkName
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
    val requestedResultNames = (requestedBenchmarks ?: allBenchmarkNames()).toResultNames(modes)

    println("Comparing Skiko versions: ${targetV1.description} and ${targetV2.description}")
    println("Platform: $platform")
    println("Number of runs: $runs")
    println("Modes: ${modes.joinToString { it.argumentName }}")
    println("Metric: ${metric.argumentName} (${metric.jsonFieldName})")
    if (skipExisting) println("Skip existing results: true")
    benchmarkName?.let { println("Filtering by benchmark: $it") }

    val resultsV1 = runBenchmarksForTarget(targetV1, runs, benchmarkName, platform, modes, requestedResultNames, metric, skipExisting)
    val resultsV2 = runBenchmarksForTarget(targetV2, runs, benchmarkName, platform, modes, requestedResultNames, metric, skipExisting)

    compareResults(targetV1.version, resultsV1, targetV2.version, resultsV2, requestedResultNames, metric)
}

fun runBenchmarksForTarget(
    target: BenchmarkTarget,
    runs: Int,
    benchmarkName: String?,
    platform: String,
    modes: Set<CompareBenchmarkMode>,
    requestedResultNames: Set<String>,
    metric: CompareMetric,
    skipExisting: Boolean,
): Map<String, List<Double>> {
    println("\n=== Running benchmarks for version: ${target.description} ===")

    if (!skipExisting || !hasExistingResults(platform, target.version, runs, requestedResultNames)) {
        val runArgs = mutableListOf(
            platform,
            "runs=$runs",
            "version=${target.version}",
            "modes=${modes.joinToString(",") { it.argumentName }}",
        )
        if (target.composite) {
            runArgs.add("composite=true")
        }
        benchmarkName?.let { runArgs.add("benchmarks=$it") }

        val process = ProcessBuilder(
            projectDir.resolve("run_benchmarks.main.kts").absolutePath,
            *runArgs.toTypedArray()
        ).directory(projectDir).inheritIO().start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("run_benchmarks.main.kts failed with exit code $exitCode")
        }
    } else {
        println("All requested runs already exist for version ${target.version}, skipping benchmarks execution.")
    }

    val allRunsResults = mutableMapOf<String, MutableList<Double>>()
    for (i in 1..runs) {
        collectResults(platform, target.version, i, requestedResultNames, metric).forEach { (name, value) ->
            allRunsResults.getOrPut(name) { mutableListOf() }.add(value)
        }
    }

    return allRunsResults
}

fun hasExistingResults(
    platform: String,
    version: String,
    runs: Int,
    requestedResultNames: Set<String>,
): Boolean {
    for (i in 1..runs) {
        val archiveDir = projectDir.resolve("build/benchmarks/archive/$platform/${version}_run$i")
        if (!archiveDir.exists()) return false
        val existingBenchmarks = archiveDir.listFiles { file -> file.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?.toSet()
            .orEmpty()
        if (!existingBenchmarks.containsAll(requestedResultNames)) return false
    }
    return true
}

fun collectResults(
    platform: String,
    version: String,
    runIndex: Int,
    requestedResultNames: Set<String>,
    metric: CompareMetric,
): Map<String, Double> {
    val archiveDir = projectDir.resolve("build/benchmarks/archive/$platform/${version}_run$runIndex")
    val resultMap = mutableMapOf<String, Double>()

    if (!archiveDir.exists()) {
        println("Warning: Results directory not found: ${archiveDir.absolutePath}")
        return resultMap
    }

    archiveDir.listFiles { file -> file.extension == "json" && file.nameWithoutExtension in requestedResultNames }?.forEach { file ->
        val metricValue = parseJsonNumberValue(file.readText(), metric.jsonFieldName)
        if (metricValue != null) {
            resultMap[file.nameWithoutExtension] = metricValue
        } else {
            println("Warning: ${metric.jsonFieldName} not found in ${file.absolutePath}")
        }
    }

    return resultMap
}

fun compareResults(
    v1: String,
    res1: Map<String, List<Double>>,
    v2: String,
    res2: Map<String, List<Double>>,
    requestedBenchmarks: Set<String>?,
    metric: CompareMetric,
) {
    println("\n=== Comparison Report ===")
    val allResultNames = (res1.keys + res2.keys)
        .filter { requestedBenchmarks == null || it in requestedBenchmarks }
        .sorted()

    val rows = allResultNames
        .map { name ->
            val displayName = DisplayBenchmarkName.fromResultName(name)
            val times1 = res1[name]
            val times2 = res2[name]

            if (times1 == null || times2 == null) {
                ComparisonRow(
                    benchmark = displayName.benchmark,
                    mode = displayName.mode,
                    value1 = times1?.averageOrZero()?.format3() ?: "N/A",
                    value2 = times2?.averageOrZero()?.format3() ?: "N/A",
                    diff = "N/A",
                    status = "Missing",
                )
            } else {
                val avg1 = times1.averageOrZero()
                val avg2 = times2.averageOrZero()
                val diff = if (avg1 != 0.0) (avg2 - avg1) / avg1 * 100.0 else 0.0
                val status = when {
                    diff > 5.0 -> "SLOWER"
                    diff < -5.0 -> "FASTER"
                    else -> "OK"
                }

                ComparisonRow(
                    benchmark = displayName.benchmark,
                    mode = displayName.mode,
                    value1 = avg1.format3(),
                    value2 = avg2.format3(),
                    diff = "%+.2f%%".format(diff),
                    status = status,
                )
            }
        }
        .sortedWith(compareBy<ComparisonRow> { it.benchmark }.thenBy { it.modeOrder })

    val header1 = "$v1 ${metric.columnName}"
    val header2 = "$v2 ${metric.columnName}"
    val benchmarkWidth = maxOf("Benchmark".length, rows.maxOfOrNull { it.benchmark.length } ?: 0)
    val modeWidth = maxOf("Mode".length, rows.maxOfOrNull { it.mode.length } ?: 0)
    val value1Width = maxOf(header1.length, rows.maxOfOrNull { it.value1.length } ?: 0)
    val value2Width = maxOf(header2.length, rows.maxOfOrNull { it.value2.length } ?: 0)
    val diffWidth = maxOf("Diff %".length, rows.maxOfOrNull { it.diff.length } ?: 0)
    val statusWidth = maxOf("Status".length, rows.maxOfOrNull { it.status.length } ?: 0)

    fun line(
        benchmark: String,
        mode: String,
        value1: String,
        value2: String,
        diff: String,
        status: String,
    ): String =
        benchmark.padEnd(benchmarkWidth) + " | " +
            mode.padEnd(modeWidth) + " | " +
            value1.padStart(value1Width) + " | " +
            value2.padStart(value2Width) + " | " +
            diff.padStart(diffWidth) + " | " +
            status.padEnd(statusWidth)

    println(line("Benchmark", "Mode", header1, header2, "Diff %", "Status"))
    println("-".repeat(benchmarkWidth + modeWidth + value1Width + value2Width + diffWidth + statusWidth + 15))
    rows.forEach { row ->
        println(line(row.benchmark, row.mode, row.value1, row.value2, row.diff, row.status))
    }
}

data class DisplayBenchmarkName(
    val benchmark: String,
    val mode: String,
) {
    companion object {
        fun fromResultName(name: String): DisplayBenchmarkName {
            CompareBenchmarkMode.entries
                .filter { it.resultSuffix != null }
                .forEach { mode ->
                    val suffix = "_${mode.resultSuffix}"
                    if (name.endsWith(suffix)) {
                        return DisplayBenchmarkName(
                            benchmark = name.removeSuffix(suffix),
                            mode = mode.argumentName,
                        )
                    }
                }

            return DisplayBenchmarkName(name, CompareBenchmarkMode.SIMPLE.argumentName)
        }
    }
}

data class ComparisonRow(
    val benchmark: String,
    val mode: String,
    val value1: String,
    val value2: String,
    val diff: String,
    val status: String,
) {
    val modeOrder: Int
        get() = CompareBenchmarkMode.entries.indexOfFirst { it.argumentName == mode }.let {
            if (it == -1) Int.MAX_VALUE else it
        }
}

fun parseJsonNumberValue(json: String, key: String): Double? {
    val match = Regex(""""$key"\s*:\s*(-?\d+(?:\.\d+)?)""").find(json) ?: return null
    return match.groupValues[1].toDoubleOrNull()
}

fun List<Double>.averageOrZero(): Double = if (isEmpty()) 0.0 else sum() / size

fun Double.format3(): String = String.format("%.3f", this)

fun Set<String>.toResultNames(modes: Set<CompareBenchmarkMode>): Set<String> =
    flatMap { benchmark ->
        modes.map { mode ->
            mode.resultSuffix?.let { "${benchmark}_$it" } ?: benchmark
        }
    }.toSet()

fun allBenchmarkNames(): Set<String> {
    val sourceDir = projectDir.resolve("src/commonMain/kotlin/org/jetbrains/skiko/benchmarks/cases")
    val benchmarkCaseRegex = Regex("""BenchmarkCase\("([^"]+)"""")
    val names = sourceDir
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .flatMap { file ->
            benchmarkCaseRegex.findAll(file.readText()).map { it.groupValues[1] }
        }
        .toSet()

    check(names.isNotEmpty()) {
        "Could not discover benchmark names under ${sourceDir.absolutePath}"
    }

    return names
}

main(args)
