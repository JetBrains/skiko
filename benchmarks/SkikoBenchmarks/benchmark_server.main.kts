#!/usr/bin/env kotlin

@file:DependsOn("io.ktor:ktor-server-core-jvm:3.3.3")
@file:DependsOn("io.ktor:ktor-server-netty-jvm:3.3.3")
@file:DependsOn("io.ktor:ktor-server-cors-jvm:3.3.3")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.9.0")

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val BENCHMARK_SERVER_PORT = 8090

data class BenchmarkResultFromClient(
    val name: String,
    val stats: String,
    val token: String?
)

class BenchmarksSaveServer {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null

    fun start(
        saveStatsToJSON: Boolean,
        serverToken: String,
        port: Int = BENCHMARK_SERVER_PORT
    ) {
        if (server != null) {
            println("Benchmark server is already running")
            return
        }

        server = embeddedServer(Netty, port = port) {
            install(CORS) {
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowHeader(HttpHeaders.ContentType)
                anyHost()
            }
            routing {
                get("/") {
                    call.respondText("Benchmark server is running", ContentType.Text.Plain)
                }
                post("/benchmark") {
                    val result = parseBenchmarkResult(call.receiveText())
                    if (result == null) {
                        call.respondText("Invalid benchmark result", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                        return@post
                    }

                    if (result.token != serverToken) {
                        println("Ignoring stale benchmark request for: ${result.name.ifEmpty { "<stop>" }}")
                        call.respondText("Stale benchmark client", ContentType.Text.Plain, HttpStatusCode.OK)
                        return@post
                    }

                    if (result.name.isEmpty()) {
                        println("Stopping server! Received empty name from client")
                        call.respondText("Server stopped.", ContentType.Text.Plain, HttpStatusCode.OK)
                        Thread { stop() }.start()
                        return@post
                    }

                    println("Received benchmark result for: ${result.name}")
                    printBenchmarkSummary(result.name, result.stats)
                    if (saveStatsToJSON) {
                        saveJson(result.name, result.stats)
                    }

                    call.respondText("Benchmark result saved", ContentType.Text.Plain, HttpStatusCode.OK)
                }
            }
        }.start(wait = false)
        println("Benchmark server is available at http://localhost:$port/")

        while (server != null) {
            Thread.sleep(1_000)
        }
    }

    fun stop() {
        server?.stop(1_000, 2_000)
        server = null
        println("Benchmark server stopped")
    }
}

fun parseBenchmarkResult(json: String): BenchmarkResultFromClient? =
    runCatching {
        val obj = Json.parseToJsonElement(json).jsonObject
        BenchmarkResultFromClient(
            name = obj["name"]?.jsonPrimitive?.content ?: return null,
            stats = obj["stats"]?.jsonPrimitive?.content ?: return null,
            token = obj["token"]?.jsonPrimitive?.content
        )
    }.getOrNull()

fun saveJson(name: String, stats: String) {
    val file = File("build/benchmarks/json-reports/$name.json")
    file.parentFile.mkdirs()
    file.writeText(stats)
    println("JSON results saved to ${file.absolutePath}")
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

fun List<String>.valueFor(key: String): String? =
    firstOrNull { it.startsWith("$key=") }?.substringAfter("=")

val argMap = args.associate {
    val split = it.split("=", limit = 2)
    if (split.size == 2) split[0] to split[1] else it to ""
}

BenchmarksSaveServer().start(
    saveStatsToJSON = argMap["saveStatsToJSON"]?.toBooleanStrictOrNull() ?: true,
    serverToken = argMap["serverToken"] ?: error("Missing required argument: serverToken=<token>"),
    port = argMap["port"]?.toIntOrNull() ?: BENCHMARK_SERVER_PORT
)
