#!/usr/bin/env kotlin

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.Executors
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
        var reportedGradleExit = false

        while (System.currentTimeMillis() - startTime < WEB_BENCHMARK_TIMEOUT_MS) {
            if (serverStopped.get()) {
                println("Server stopped signal received. Benchmarks should be finished.")
                completed = true
                break
            }

            if (gradleExitCode == null && process.waitFor(1, TimeUnit.SECONDS)) {
                gradleExitCode = process.exitValue()
                if (!reportedGradleExit) {
                    println("Gradle task $task exited with code $gradleExitCode; waiting for benchmark results from the browser...")
                    reportedGradleExit = true
                }
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

    private val server = HttpServer.create(InetSocketAddress(port), 0)
    private val executor = Executors.newSingleThreadExecutor()

    fun start() {
        server.executor = executor
        server.createContext("/") { exchange ->
            when {
                exchange.requestMethod.equals("GET", ignoreCase = true) -> {
                    exchange.respond(200, "Benchmark server is running")
                }
                exchange.requestMethod.equals("OPTIONS", ignoreCase = true) -> {
                    exchange.respond(200, "")
                }
                else -> {
                    exchange.respond(404, "Not found")
                }
            }
        }
        server.createContext("/benchmark") { exchange ->
            handleBenchmark(exchange)
        }
        server.start()
        println("Benchmark server is available at http://localhost:$port/")
    }

    fun stop() {
        if (stopped.compareAndSet(false, true)) {
            server.stop(0)
            println("Benchmark server stopped")
        }
        executor.shutdownNow()
    }

    private fun handleBenchmark(exchange: HttpExchange) {
        if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
            exchange.respond(200, "")
            return
        }

        if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
            exchange.respond(405, "Method not allowed")
            return
        }

        val body = exchange.requestBody.bufferedReader().use { it.readText() }
        val name = parseJsonStringValue(body, "name")
        val stats = parseJsonStringValue(body, "stats")
        val token = parseJsonStringValue(body, "token")

        if (name == null || stats == null) {
            exchange.respond(400, "Invalid benchmark result")
            return
        }

        if (token != serverToken) {
            println("Ignoring stale benchmark request for: ${name.ifEmpty { "<stop>" }}")
            exchange.respond(409, "Stale benchmark client")
            return
        }

        if (name.isEmpty()) {
            println("Stopping server! Received empty name from client")
            exchange.respond(200, "Server stopped.")
            Thread { stop() }.start()
            return
        }

        println("Received benchmark result for: $name")
        printBenchmarkSummary(name, stats)
        if (saveStatsToJSON) {
            val file = projectDir.resolve("build/benchmarks/json-reports/$name.json")
            file.parentFile.mkdirs()
            file.writeText(stats)
            println("JSON results saved to ${file.absolutePath}")
        }

        exchange.respond(200, "Benchmark result saved")
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

fun HttpExchange.respond(statusCode: Int, text: String) {
    responseHeaders.add("Access-Control-Allow-Origin", "*")
    responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
    responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
    val bytes = text.encodeToByteArray()
    sendResponseHeaders(statusCode, bytes.size.toLong())
    responseBody.use { it.write(bytes) }
}

fun parseJsonStringValue(json: String, key: String): String? {
    val keyToken = "\"$key\""
    var index = json.indexOf(keyToken)
    if (index == -1) return null

    index += keyToken.length
    while (index < json.length && json[index].isWhitespace()) index++
    if (index >= json.length || json[index] != ':') return null
    index++
    while (index < json.length && json[index].isWhitespace()) index++
    if (index >= json.length || json[index] != '"') return null
    index++

    val result = StringBuilder()
    while (index < json.length) {
        val c = json[index++]
        if (c == '"') return result.toString()
        if (c != '\\') {
            result.append(c)
            continue
        }

        if (index >= json.length) return null
        when (val escaped = json[index++]) {
            '"', '\\', '/' -> result.append(escaped)
            'b' -> result.append('\b')
            'f' -> result.append('\u000C')
            'n' -> result.append('\n')
            'r' -> result.append('\r')
            't' -> result.append('\t')
            'u' -> {
                if (index + 4 > json.length) return null
                val code = json.substring(index, index + 4).toIntOrNull(16) ?: return null
                result.append(code.toChar())
                index += 4
            }
            else -> return null
        }
    }

    return null
}

fun printBenchmarkSummary(name: String, stats: String) {
    val average = parseJsonNumberValue(stats, "averageMillis")?.let { String.format("%.3f ms", it) } ?: "N/A"
    val min = parseJsonNumberValue(stats, "minMillis")?.let { String.format("%.3f ms", it) } ?: "N/A"
    val max = parseJsonNumberValue(stats, "maxMillis")?.let { String.format("%.3f ms", it) } ?: "N/A"
    println("${name.padEnd(25)} | avg $average | min $min | max $max")
}

fun parseJsonNumberValue(json: String, key: String): Double? {
    val match = Regex(""""$key"\s*:\s*(-?\d+(?:\.\d+)?)""").find(json) ?: return null
    return match.groupValues[1].toDoubleOrNull()
}

main(args)
