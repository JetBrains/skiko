package org.jetbrains.skiko

import kotlin.system.measureNanoTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

internal data class NotSupportedAdapter(
    val os: OS,
    val api: GraphicsApi,
    val pattern: Regex
)

private val notSupportedAdapters: List<NotSupportedAdapter> by lazy {
    listOf(
            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = Regex("Intel(R) HD Graphics 520")),
            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = Regex("Intel(R) HD Graphics 530")),
            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = Regex("Intel(R) HD Graphics 4400")),

            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = Regex("NVIDIA GeForce GTX 750 Ti")),
            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = Regex("NVIDIA GeForce GTX 960M")),
            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = Regex("NVIDIA Quadro M2000M")),

            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.OPENGL, pattern = Regex("Intel(R) HD Graphics 2000")),
            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.OPENGL, pattern = Regex("Intel(R) HD Graphics 3000")),

            NotSupportedAdapter(os = OS.Linux, api = GraphicsApi.OPENGL, pattern = Regex("llvmpipe.*")),
            NotSupportedAdapter(os = OS.Linux, api = GraphicsApi.OPENGL, pattern = Regex("virgl.*"))
    )
}

internal fun isVideoCardSupported(api: GraphicsApi, hostOs: OS, name: String): Boolean {
    return notSupportedAdapters.any { entry ->
        entry.os == hostOs && entry.api == api && entry.pattern.matches(name)
    }.not()
}