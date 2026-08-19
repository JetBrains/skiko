package org.jetbrains.skiko.benchmarks

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    val status = document.getElementById("status")
    val output = document.getElementById("output")
    status?.textContent = "Waiting for Skiko..."

    onWasmReady {
        val args = runArgs()
        val config = BenchmarkConfig.fromArgs(args)
        status?.textContent = "Running benchmarks..."
        val report = SkikoBenchmarkSuite.run(
            platform = "web",
            config = config,
        )
        report.prettyPrint()
        val json = report.toJson()
        output?.textContent = json
        if (args.contains("saveStatsToJSON=true")) {
            status?.textContent = "Posting results..."
            val serverUrl = "http://${window.location.hostname}:8090/benchmark"
            val serverToken = args.valueFor("serverToken") ?: ""
            report.results.forEach { result ->
                postBenchmarkResult(
                    serverUrl,
                    result.name,
                    result.toReportJson(platform = "web", versionInfo = config.versionInfo),
                    serverToken
                )
            }
            stopBenchmarkServer(serverUrl, serverToken)
        } else {
            status?.textContent = "Done"
        }
    }
}

private fun runArgs(): List<String> {
    val query = window.location.search.removePrefix("?")
    if (query.isBlank()) return emptyList()

    return query
        .split("&")
        .mapNotNull { part ->
            val keyValue = part.split("=", limit = 2)
            if (keyValue.size == 2 && keyValue[0].startsWith("arg")) {
                decodeUrlComponent(keyValue[1])
            } else {
                null
            }
        }
}

internal expect fun postBenchmarkResult(url: String, name: String, report: String, token: String)

internal expect fun stopBenchmarkServer(url: String, token: String)

internal expect fun decodeUrlComponent(value: String): String
