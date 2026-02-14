package org.jetbrains.skiko

import org.jetbrains.skiko.internal.fastNone

internal data class NotSupportedAdapter(
    val os: OS,
    val api: GraphicsApi,
    val pattern: String,
    /** If true, the adapter name should match the pattern exactly. If false, the patters should be the start of the adapter name. */
    val exactPattern: Boolean = true
)

private val notSupportedAdapters: List<NotSupportedAdapter> by lazy {
    listOf(
        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = "Intel(R) HD Graphics 520"),
        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = "Intel(R) HD Graphics 530"),
        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = "Intel(R) HD Graphics 4400"),
        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = "Intel(R) HD Graphics 4600"),

        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = "NVIDIA GeForce GTX 750 Ti"),
        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = "NVIDIA GeForce GTX 960M"),
        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = "NVIDIA Quadro M2000M"),

        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.OPENGL, pattern = "Intel(R) HD Graphics 2000"),
        NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.OPENGL, pattern = "Intel(R) HD Graphics 3000"),

        NotSupportedAdapter(os = OS.Linux, api = GraphicsApi.OPENGL, pattern = "llvmpipe", exactPattern = false),
        NotSupportedAdapter(os = OS.Linux, api = GraphicsApi.OPENGL, pattern = "virgl", exactPattern = false)
    )
}

internal fun isVideoCardSupported(api: GraphicsApi, hostOs: OS, name: String): Boolean {
    return notSupportedAdapters.fastNone { adapter ->
        if ((adapter.os != hostOs) || (adapter.api != api)) return@fastNone false

        val matchesPattern = if (adapter.exactPattern) (adapter.pattern == name) else name.startsWith(adapter.pattern)
        matchesPattern
    }
}