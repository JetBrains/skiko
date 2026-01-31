package org.jetbrains.skiko

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
            NotSupportedAdapter(os = OS.Windows, api = GraphicsApi.DIRECT3D, pattern = Regex("Intel(R) HD Graphics 4600")),

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
    for (index in notSupportedAdapters.indices) {
        val adapter = notSupportedAdapters[index]
        if (adapter.os == hostOs && adapter.api == api && adapter.pattern.matches(name)) return false
    }
    return true
}