#!/usr/bin/env kotlin

import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.UUID

enum class Platform(val platformName: String) {
    WEB("web"),
    AWT("awt");

    companion object {
        fun fromString(name: String): Platform =
            entries.find { it.platformName == name }
                ?: throw IllegalArgumentException("Unsupported platform: $name. Supported: ${entries.joinToString { it.platformName }}")
    }
}

val BENCHMARK_SERVER_PORT = 8090
val WEB_BENCHMARK_TIMEOUT_MS = 5 * 60 * 1000L
val projectDir = findBenchmarkProjectDir()
val repoRoot = projectDir.parentFile.parentFile
val gradlew = repoRoot.resolve("gradlew")

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: ./run_benchmarks.main.kts <platform> version=<version> [runs=1] [benchmarks=<name1,name2,...>] [modes=SIMPLE|VSYNC_EMULATION|STARTUP] [any other gradle args]")
        println("Platforms: ${Platform.entries.joinToString { it.platformName }}")
        println("Arguments:")
        println("  runs=<number> (default: 1)")
        println("  version=<skiko-version> (required)")
        println("  benchmarks=<name1,name2,...>")
        println("  modes=SIMPLE,VSYNC_EMULATION,STARTUP (default: SIMPLE)")
        println("  any other gradle args")
        return
    }

    val platform = Platform.fromString(args[0])
    val argMap = args.drop(1).associate {
        val split = it.split("=", limit = 2)
        if (split.size == 2) split[0] to split[1] else it to ""
    }.toMutableMap()

    val runs = argMap.remove("runs")?.toIntOrNull() ?: 1
    val version = argMap.remove("version")
        ?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Missing required argument: version=<skiko-version>")
    val benchmarkName = argMap["benchmarks"]
    val modes = argMap["modes"] ?: "SIMPLE"

    println("Running Skiko benchmarks for platform: ${platform.platformName}")
    println("Skiko version: $version")
    println("Number of runs: $runs")
    println("Modes: $modes")
    println("Run server: ${platform == Platform.WEB}")
    benchmarkName?.let { println("Filtering by benchmark: $it") }

    if (runs <= 0) {
        println("Nothing to run (runs=$runs)")
        return
    }

    repeat(runs) { runIndex ->
        val runNumber = runIndex + 1
        println("\nRun $runNumber/$runs...")
        cleanJsonReports()
        executeBenchmarks(version, runNumber, platform, argMap)
        collectResults(platform.platformName, version, runNumber)
    }
}

fun executeBenchmarks(
    version: String,
    runIndex: Int,
    platform: Platform,
    extraArgs: Map<String, String>
) {
    val gradleArgs = gradleArgsFor(version, extraArgs, platform)
    val runArgs = mutableListOf(
        "versionInfo=${version}_run${runIndex}",
        "saveStatsToJSON=true"
    )

    if (platform == Platform.WEB) {
        runArgs.add("runServer=true")
        runArgs.add("serverToken=${UUID.randomUUID()}")
    }

    extraArgs.forEach { (key, value) ->
        if (key == "saveStatsToJSON" && platform == Platform.WEB) return@forEach
        if (key == "composite" || key.startsWith("skiko.")) return@forEach
        if (value.isNotEmpty()) {
            runArgs.add("$key=$value")
        } else {
            runArgs.add(key)
        }
    }

    when (platform) {
        Platform.WEB -> {
            println("Starting benchmark server in background...")
            val benchmarkServer = LocalBenchmarkServer(
                saveStatsToJSON = true,
                serverToken = runArgs.valueFor("serverToken") ?: error("Missing server token")
            )
            benchmarkServer.start()
            try {
                val browserUrl = browserUrlFor(runArgs)
                val browserIsOpenedByPlaywright = canOpenWithPlaywright()
                executeGradleBenchmark(
                    task = "wasmJsBrowserProductionRun",
                    runArgs = runArgs,
                    serverStopped = benchmarkServer.stopped,
                    extraGradleArgs = gradleArgs + "-Pskiko.benchmark.openBrowser=${!browserIsOpenedByPlaywright}",
                    browserUrl = browserUrl
                )
            } finally {
                println("Stopping benchmark server...")
                benchmarkServer.stop()
            }
        }
        Platform.AWT -> {
            executeGradleBenchmark(
                task = "awtBenchmark",
                runArgs = runArgs,
                serverStopped = null,
                extraGradleArgs = gradleArgs
            )
        }
    }
}

fun gradleArgsFor(version: String, extraArgs: Map<String, String>, platform: Platform): List<String> {
    val args = mutableListOf("-Pskiko.version=$version")
    if (extraArgs["composite"] == "true") {
        args += "-Pskiko.composite.build=1"
        when (platform) {
            Platform.WEB -> {
                args += "-Pskiko.wasm.enabled=true"
                args += "-Pskiko.awt.enabled=false"
            }
            Platform.AWT -> {
                args += "-Pskiko.wasm.enabled=false"
                args += "-Pskiko.awt.enabled=true"
            }
        }
    }

    extraArgs.forEach { (key, value) ->
        if (key.startsWith("skiko.")) {
            args += if (value.isEmpty()) "-P$key" else "-P$key=$value"
        }
    }

    return args.distinct()
}

fun executeGradleBenchmark(
    task: String,
    runArgs: List<String>,
    serverStopped: AtomicBoolean?,
    extraGradleArgs: List<String> = emptyList(),
    browserUrl: String? = null
) {
    println("Running via Gradle task $task...")
    val processBuilder = ProcessBuilder(
        gradlew.absolutePath,
        "-p", projectDir.absolutePath,
        task,
        "-PrunArguments=${runArgs.joinToString(" ")}",
        *extraGradleArgs.toTypedArray(),
    ).directory(projectDir).inheritIO()

    val process = processBuilder.start()

    val exitCode = if (serverStopped != null) {
        browserUrl?.let { openWithPlaywrightIfAvailable(it) }

        println("Waiting for benchmarks to complete (timeout 5m)...")
        val startTime = System.currentTimeMillis()
        var completed = false
        var gradleExitCode: Int? = null

        while (System.currentTimeMillis() - startTime < WEB_BENCHMARK_TIMEOUT_MS) {
            if (serverStopped.get()) {
                println("Server stopped signal received. Benchmarks should be finished.")
                completed = true
                break
            }

            if (gradleExitCode == null && process.waitFor(1, TimeUnit.SECONDS)) {
                gradleExitCode = process.exitValue()
                println("Gradle task $task exited with code $gradleExitCode; waiting for benchmark results from the browser...")
            } else {
                Thread.sleep(1_000)
            }
        }

        if (!completed) {
            println("Timeout reached. Stopping benchmark task...")
            gradleExitCode?.takeIf { it != 0 } ?: 1
        } else {
            println("Stopping benchmark task...")
            0
        }.also {
            if (process.isAlive) {
                process.destroy()
                process.destroyForcibly()
            }
        }
    } else {
        process.waitFor()
    }

    if (exitCode != 0) {
        throw RuntimeException("Benchmark task $task failed with exit code $exitCode")
    }
}

fun browserUrlFor(runArgs: List<String>): String {
    val query = runArgs
        .mapIndexed { index, arg -> "arg$index=${arg.replace(" ", "%20")}" }
        .joinToString("&")
    return "http://localhost:8080?$query"
}

fun openWithPlaywrightIfAvailable(url: String) {
    if (!canOpenWithPlaywright()) {
        return
    }

    if (!waitForHttp(url, timeoutMs = WEB_BENCHMARK_TIMEOUT_MS)) {
        println("Warning: webpack dev server did not become reachable at $url")
        return
    }

    val script = projectDir.resolve("build/benchmarks/tmp/open-with-playwright.cjs")
    script.parentFile.mkdirs()
    script.writeText(
        """
        const { chromium } = require('playwright');

        (async () => {
            const url = process.argv[2];
            const browser = await chromium.launch({ headless: true });
            const page = await browser.newPage();
            page.on('console', message => console.log(message.text()));
            await page.goto(url);
            await page.waitForFunction(
                () => document.getElementById('status')?.textContent === 'Done',
                null,
                { timeout: 300000 }
            );
            await browser.close();
        })();
        """.trimIndent()
    )

    println("Opening benchmark page with Playwright: $url")
    val process = ProcessBuilder("node", script.absolutePath, url)
        .directory(projectDir)
        .inheritIO()
        .start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw RuntimeException("Playwright benchmark browser failed with exit code $exitCode")
    }
}

fun canOpenWithPlaywright(): Boolean =
    !System.getenv("NODE_PATH").isNullOrBlank() || !System.getenv("PLAYWRIGHT_BIN").isNullOrBlank()

fun waitForHttp(url: String, timeoutMs: Long): Boolean {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
        try {
            val connection = URI(url).toURL().openConnection()
            connection.connectTimeout = 1_000
            connection.readTimeout = 1_000
            connection.getInputStream().use { return true }
        } catch (_: Throwable) {
            Thread.sleep(500)
        }
    }
    return false
}

class LocalBenchmarkServer(
    private val saveStatsToJSON: Boolean,
    private val serverToken: String,
    private val port: Int = BENCHMARK_SERVER_PORT
) {
    val stopped = AtomicBoolean(false)
    private val started = AtomicBoolean(false)

    private var process: Process? = null
    private var outputThread: Thread? = null

    fun start() {
        if (process != null) {
            println("Benchmark server is already running")
            return
        }

        process = ProcessBuilder(
            gradlew.absolutePath,
            "-p", projectDir.absolutePath,
            "--no-daemon",
            "runBenchmarkServer",
            "-PbenchmarkServer.arguments=saveStatsToJSON=$saveStatsToJSON serverToken=$serverToken port=$port"
        )
            .directory(projectDir)
            .redirectErrorStream(true)
            .start()

        outputThread = Thread {
            process?.inputStream?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    println("[SERVER] $line")
                    if (line.contains("Benchmark server is available at")) {
                        started.set(true)
                    }
                    if (line.contains("Benchmark server stopped")) {
                        stopped.set(true)
                    }
                }
            }
        }.also {
            it.isDaemon = true
            it.start()
        }

        if (!waitForBenchmarkServerStart()) {
            throw RuntimeException("Benchmark server did not become reachable at http://localhost:$port/")
        }
    }

    fun stop() {
        val serverProcess = process ?: return
        if (stopped.compareAndSet(false, true)) {
            serverProcess.destroy()
            serverProcess.destroyForcibly()
            println("Benchmark server stopped")
        }
        outputThread?.interrupt()
        process = null
        outputThread = null
    }

    private fun waitForBenchmarkServerStart(): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < WEB_BENCHMARK_TIMEOUT_MS) {
            if (started.get()) return true
            if (process?.isAlive == false) return false
            Thread.sleep(500)
        }
        return false
    }
}

fun collectResults(platform: String, version: String, runIndex: Int) {
    val resultsDir = projectDir.resolve("build/benchmarks/json-reports")
    if (!resultsDir.exists()) return

    resultsDir.listFiles { file -> file.extension == "json" }?.forEach { file ->
        val archiveDir = projectDir.resolve("build/benchmarks/archive/$platform/${version}_run$runIndex")
        archiveDir.mkdirs()
        val targetFile = archiveDir.resolve(file.name)
        if (file.renameTo(targetFile)) {
            println("Archived ${file.name} to ${targetFile.path}")
        }
    }
}

fun cleanJsonReports() {
    val resultsDir = projectDir.resolve("build/benchmarks/json-reports")
    if (resultsDir.exists()) {
        resultsDir.listFiles { file -> file.extension == "json" }?.forEach { it.delete() }
    }
}

fun List<String>.valueFor(key: String): String? =
    firstOrNull { it.startsWith("$key=") }?.substringAfter("=")

fun findBenchmarkProjectDir(): File {
    val current = File(".").canonicalFile
    if (current.resolve("build.gradle.kts").exists() && current.name == "SkikoBenchmarks") {
        return current
    }

    val fromRepoRoot = current.resolve("benchmarks/SkikoBenchmarks")
    if (fromRepoRoot.resolve("build.gradle.kts").exists()) {
        return fromRepoRoot.canonicalFile
    }

    val fromScriptParent = File("benchmarks/SkikoBenchmarks").canonicalFile
    if (fromScriptParent.resolve("build.gradle.kts").exists()) {
        return fromScriptParent
    }

    throw IllegalStateException("Cannot locate benchmarks/SkikoBenchmarks project")
}

main(args)
