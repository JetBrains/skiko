package org.jetbrains.skiko

private val linePattern = Regex("(.*):(.*):(.*)")

internal data class NotSupportedAdapter(
    val os: OS,
    val api: GraphicsApi,
    val pattern: Regex
)

internal fun parseNotSupportedAdapter(line: String): NotSupportedAdapter? {
    val match = linePattern.matchEntire(line) ?: return null
    val groups = match.groups
    val platform = groups[1]?.value?.let { platformName ->
        when (platformName) {
            "windows" -> OS.Windows
            "linux" -> OS.Linux
            "macos" -> OS.MacOS
            else -> null
        }
    } ?: return null
    val api = when (groups[2]?.value) {
        "directx" -> GraphicsApi.DIRECT3D
        "opengl" -> GraphicsApi.OPENGL
        else -> return null
    }
    val pattern = groups[3]?.value ?: return null
    return NotSupportedAdapter(platform, api, Regex(pattern))
}

private val notSupportedAdapters: Set<NotSupportedAdapter> by lazy {
    val resource = SkiaLayer::class.java.getResourceAsStream("/not-supported-adapter.list")?.bufferedReader()?.lineSequence().orEmpty()
    resource.map { it.trim() }.mapNotNullTo(mutableSetOf()) { line ->
        parseNotSupportedAdapter(line)
    }
}

internal fun isVideoCardSupported(api: GraphicsApi, hostOs: OS, name: String): Boolean {
    return notSupportedAdapters.any { entry ->
        entry.os == hostOs && entry.api == api && entry.pattern.matches(name)
    }.not()
}