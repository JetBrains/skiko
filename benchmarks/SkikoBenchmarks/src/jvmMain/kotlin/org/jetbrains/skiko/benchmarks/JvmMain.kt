package org.jetbrains.skiko.benchmarks

import java.io.File

fun main(args: Array<String>) {
    if (args.contains("listBenchmarks=true")) {
        println("AVAILABLE_BENCHMARKS_START")
        SkikoBenchmarkSuite.benchmarkNames().forEach(::println)
        println("AVAILABLE_BENCHMARKS_END")
        return
    }

    val config = BenchmarkConfig.fromArgs(args.asIterable())
    val report = SkikoBenchmarkSuite.run(
        platform = "jvm",
        config = config,
    )

    report.prettyPrint()
    if (args.contains("saveStatsToJSON=true")) {
        val resultsDir = File("build/benchmarks/json-reports")
        resultsDir.mkdirs()
        report.results.forEach { result ->
            val file = resultsDir.resolve("${result.name}.json")
            file.writeText(result.toReportJson(platform = "jvm", versionInfo = config.versionInfo))
            println("JSON results saved to ${file.absolutePath}")
        }
    } else {
        println(report.toJson())
    }
}
