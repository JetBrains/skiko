@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko.benchmarks

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.EEXIST
import platform.posix.errno
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.getcwd
import platform.posix.mkdir

fun main(args: Array<String>) {
    if (args.contains("listBenchmarks=true")) {
        println("AVAILABLE_BENCHMARKS_START")
        SkikoBenchmarkSuite.benchmarkNames().forEach(::println)
        println("AVAILABLE_BENCHMARKS_END")
        return
    }

    val config = BenchmarkConfig.fromArgs(args.asIterable())
    val report = SkikoBenchmarkSuite.run(
        platform = "macosArm64",
        config = config,
    )

    report.prettyPrint()
    if (args.contains("saveStatsToJSON=true")) {
        val resultsDir = "build/benchmarks/json-reports"
        createDirectories(resultsDir)
        report.results.forEach { result ->
            val relativePath = "$resultsDir/${result.name}.json"
            writeFile(relativePath, result.toReportJson(platform = "macosArm64", versionInfo = config.versionInfo))
            println("JSON results saved to ${absolutePath(relativePath)}")
        }
    } else {
        println(report.toJson())
    }
}

private fun createDirectories(path: String) {
    var current = ""
    path.split("/").filter { it.isNotEmpty() }.forEach { segment ->
        current = if (current.isEmpty()) segment else "$current/$segment"
        if (mkdir(current, 0x1ED.convert()) != 0 && errno != EEXIST) {
            error("Failed to create directory $current (errno $errno)")
        }
    }
}

private fun writeFile(path: String, content: String) {
    val file = fopen(path, "w") ?: error("Failed to open $path for writing (errno $errno)")
    try {
        fputs(content, file)
    } finally {
        fclose(file)
    }
}

private fun absolutePath(relativePath: String): String {
    val cwd = memScoped {
        val size = 4096
        val buffer = allocArray<ByteVar>(size)
        getcwd(buffer, size.convert())?.toKString() ?: error("getcwd failed (errno $errno)")
    }
    return "$cwd/$relativePath"
}
