package org.jetbrains.skiko

enum class GraphicsApi {
    UNKNOWN, SOFTWARE, OPENGL, DIRECT3D, VULKAN, METAL
}

enum class GpuPriority(val value: String) {
    Auto("auto"), Integrated("integrated"), Discrete("discrete");

    companion object {
        fun parse(value: String?): GpuPriority? = GpuPriority.values().find { it.value == value }
    }
}