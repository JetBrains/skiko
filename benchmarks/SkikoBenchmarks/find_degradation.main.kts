#!/usr/bin/env kotlin

@file:Import("benchmark_project.main.kts")

import java.io.File

/**
 * Usage:
 *   ./find_degradation.main.kts benchmarks=<benchmarkName> versions=<versionsFile> [platform=web|jvm]
 */

val projectDir = findBenchmarkProjectDir()

fun main(args: Array<String>) {
    val argMap = args.associate {
        val split = it.split("=", limit = 2)
        if (split.size == 2) split[0] to split[1] else it to ""
    }

    val benchmarkName = argMap["benchmarks"] ?: args.getOrNull(0)
    val versionsFileName = argMap["versions"] ?: args.getOrNull(1)
    val platform = argMap["platform"] ?: "web"

    if (benchmarkName == null || versionsFileName == null) {
        println("Usage: ./find_degradation.main.kts benchmarks=<benchmarkName> versions=<versionsFile> [platform=web|jvm]")
        return
    }

    val versionsFile = File(versionsFileName)
    if (!versionsFile.exists()) {
        println("Versions file not found: ${versionsFile.absolutePath}")
        return
    }

    val versions = versionsFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }
    if (versions.size < 2) {
        println("Need at least 2 versions in the file to find a degradation.")
        return
    }

    println("Finding degradation for benchmark: $benchmarkName")
    println("Versions to check: ${versions.joinToString(", ")}")
    println("Platform: $platform")
    println("Baseline version: ${versions.first()}")

    var low = 1
    var high = versions.lastIndex
    var firstDegradedIndex = -1

    while (low <= high) {
        val mid = (low + high) / 2
        val targetVersion = versions[mid]

        println("\n--- Checking version: $targetVersion (index $mid) ---")
        val isDegraded = checkDegradation(versions.first(), targetVersion, benchmarkName, platform)

        if (isDegraded) {
            println("Degradation FOUND in $targetVersion")
            firstDegradedIndex = mid
            high = mid - 1
        } else {
            println("No degradation in $targetVersion")
            low = mid + 1
        }
    }

    println("\n=== RESULT ===")
    if (firstDegradedIndex != -1) {
        println("Degradation was introduced in version: ${versions[firstDegradedIndex]}")
        println("Previous stable version: ${versions[firstDegradedIndex - 1]}")
    } else {
        println("No degradation found in any provided version compared to ${versions.first()}")
    }
}

fun checkDegradation(v1: String, v2: String, benchmarkName: String, platform: String): Boolean {
    val process = ProcessBuilder(
        projectDir.resolve("compare_benchmarks.main.kts").absolutePath,
        "v1=$v1",
        "v2=$v2",
        "runs=3",
        "benchmarks=$benchmarkName",
        "platform=$platform",
        "skipExisting=true"
    ).directory(projectDir).redirectErrorStream(true).start()

    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()

    println(output)

    val reportStartIndex = output.lines().indexOfFirst { it.contains("Comparison Report") }
    if (reportStartIndex == -1) return false

    return output.lines()
        .drop(reportStartIndex)
        .any { line -> line.contains(benchmarkName) && line.contains("SLOWER") }
}

main(args)
