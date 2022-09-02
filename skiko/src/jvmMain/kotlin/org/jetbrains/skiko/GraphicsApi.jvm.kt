package org.jetbrains.skiko

private val notSupportedAdapters: Set<NotSupportedAdapter> by lazy {
    val resource = SkiaLayer::class.java.getResource("/not-supported-adapter.list")?.readText().orEmpty()
    resource.splitToSequence(";").map { it.trim() }.mapNotNullTo(mutableSetOf()) {
        val splits = it.split(":")
        val api = when (splits.getOrNull(0)) {
            "directx" -> GraphicsApi.DIRECT3D
            "opengl" -> GraphicsApi.OPENGL
            else -> return@mapNotNullTo null
        }
        val name = splits.getOrNull(1) ?: return@mapNotNullTo null
        NotSupportedAdapter(api, name)
    }
}

internal fun isVideoCardSupported(
    api: GraphicsApi,
    name: String?
): Boolean = name == null || !notSupportedAdapters.contains(NotSupportedAdapter(api, name))

private data class NotSupportedAdapter(
    val api: GraphicsApi,
    val name: String
)