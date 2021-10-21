package org.jetbrains.skiko

enum class GraphicsApi {
    UNKNOWN, SOFTWARE, AWT_SOFTWARE, OPENGL, DIRECT3D, VULKAN, METAL, WEBGL
}

enum class GpuPriority(val value: String) {
    Auto("auto"), Integrated("integrated"), Discrete("discrete");

    companion object {
        fun parse(value: String?): GpuPriority? = GpuPriority.values().find { it.value == value }
    }
}
