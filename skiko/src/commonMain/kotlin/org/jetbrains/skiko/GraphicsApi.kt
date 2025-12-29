package org.jetbrains.skiko

enum class GraphicsApi {
    UNKNOWN,

    /**
     * Fast software rendering that works in 95% of cases. If it doesn't work, Skiko will fallback to SOFTWARE_COMPAT.
     *
     * For example, on JVM it doesn't work if the system has 16-bit color.
     */
    SOFTWARE_FAST,

    /**
     * Slower software rendering that works in all cases. On JVM uses [java.awt.BufferedImage] as intermediate buffer.
     */
    SOFTWARE_COMPAT,

    OPENGL,
    DIRECT3D,
    ANGLE,
    VULKAN,
    METAL,
    WEBGL
}

enum class GpuPriority(val value: String) {
    Auto("auto"), Integrated("integrated"), Discrete("discrete");

    companion object {
        fun parseOrNull(value: String): GpuPriority? = GpuPriority.entries.find { it.value == value }
    }
}

enum class FrameBuffering {
    DEFAULT,
    DOUBLE,
    TRIPLE
}

fun FrameBuffering.numberOfBuffers(): Int? {
    return when (this) {
        FrameBuffering.DEFAULT -> null
        FrameBuffering.DOUBLE -> 2
        FrameBuffering.TRIPLE -> 3
    }
}