package org.jetbrains.skiko

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import org.jetbrains.skia.PixelGeometry

/** Rendering configuration for a Kotlin/Native Linux [SkiaLayer]. */
class SkiaLayerProperties(
    val isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
    val isVsyncFramelimitFallbackEnabled: Boolean =
        SkikoProperties.vsyncFramelimitFallbackEnabled,
    val frameBuffering: FrameBuffering = SkikoProperties.frameBuffering,
    val renderApi: GraphicsApi = SkikoProperties.renderApi,
    val adapterPriority: GpuPriority = SkikoProperties.gpuPriority,
    val gpuResourceCacheLimit: Long = SkikoProperties.gpuResourceCacheLimit,
) {
    init {
        require(renderApi in SupportedLinuxRenderApis) {
            "Kotlin/Native Linux does not support $renderApi rendering"
        }
    }

    fun copy(
        isVsyncEnabled: Boolean = this.isVsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = this.isVsyncFramelimitFallbackEnabled,
        frameBuffering: FrameBuffering = this.frameBuffering,
        renderApi: GraphicsApi = this.renderApi,
        adapterPriority: GpuPriority = this.adapterPriority,
        gpuResourceCacheLimit: Long = this.gpuResourceCacheLimit,
    ): SkiaLayerProperties =
        SkiaLayerProperties(
            isVsyncEnabled = isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled = isVsyncFramelimitFallbackEnabled,
            frameBuffering = frameBuffering,
            renderApi = renderApi,
            adapterPriority = adapterPriority,
            gpuResourceCacheLimit = gpuResourceCacheLimit,
        )

    override fun equals(other: Any?): Boolean =
        other is SkiaLayerProperties &&
            isVsyncEnabled == other.isVsyncEnabled &&
            isVsyncFramelimitFallbackEnabled == other.isVsyncFramelimitFallbackEnabled &&
            frameBuffering == other.frameBuffering &&
            renderApi == other.renderApi &&
            adapterPriority == other.adapterPriority &&
            gpuResourceCacheLimit == other.gpuResourceCacheLimit

    override fun hashCode(): Int {
        var result = isVsyncEnabled.hashCode()
        result = 31 * result + isVsyncFramelimitFallbackEnabled.hashCode()
        result = 31 * result + frameBuffering.hashCode()
        result = 31 * result + renderApi.hashCode()
        result = 31 * result + adapterPriority.hashCode()
        result = 31 * result + gpuResourceCacheLimit.hashCode()
        return result
    }
}

/** Environment-backed rendering defaults for Kotlin/Native Linux. */
object SkikoProperties {
    val vsyncEnabled: Boolean
        get() = environmentBoolean("SKIKO_VSYNC_ENABLED", default = true)

    val vsyncFramelimitFallbackEnabled: Boolean
        get() = environmentBoolean("SKIKO_VSYNC_FRAMELIMIT_FALLBACK_ENABLED", default = true)

    val frameBuffering: FrameBuffering
        get() =
            environment("SKIKO_FRAME_BUFFERING")
                ?.uppercase()
                ?.let { value -> FrameBuffering.entries.firstOrNull { it.name == value } }
                ?: FrameBuffering.DEFAULT

    val renderApi: GraphicsApi
        get() = parseRenderApi(environment("SKIKO_RENDER_API"))

    val gpuPriority: GpuPriority
        get() = environment("SKIKO_GPU_PRIORITY")?.lowercase()?.let(GpuPriority::parseOrNull)
            ?: GpuPriority.Auto

    val gpuResourceCacheLimit: Long
        get() = parseSize(environment("SKIKO_GPU_RESOURCE_CACHE_LIMIT"))

    val pixelGeometry: PixelGeometry
        get() =
            environment("SKIKO_PIXEL_GEOMETRY")
                ?.uppercase()
                ?.let { value -> PixelGeometry.entries.firstOrNull { it.name == value } }
                ?: PixelGeometry.UNKNOWN

    val fpsEnabled: Boolean
        get() = environmentBoolean("SKIKO_FPS_ENABLED", default = false)

    val fpsPeriodSeconds: Double
        get() = environment("SKIKO_FPS_PERIOD_SECONDS")?.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 2.0

    val fpsLongFramesShow: Boolean
        get() = environmentBoolean("SKIKO_FPS_LONG_FRAMES_SHOW", default = false)

    val fpsLongFramesMillis: Double?
        get() = environment("SKIKO_FPS_LONG_FRAMES_MILLIS")?.toDoubleOrNull()?.takeIf { it > 0.0 }

    internal val allowSoftwareOpenGlAdapter: Boolean
        get() = environmentBoolean("SKIKO_OPENGL_ALLOW_SOFTWARE", default = false)

    internal fun fallbackRenderApiQueue(initialApi: GraphicsApi): List<GraphicsApi> {
        val all = listOf(GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)
        val initialIndex = all.indexOf(initialApi)
        require(initialIndex >= 0) { "Kotlin/Native Linux does not support $initialApi rendering" }
        return all.drop(initialIndex)
    }

    internal fun parseRenderApi(value: String?): GraphicsApi =
        when (value?.uppercase()) {
            null, "", "OPENGL" -> GraphicsApi.OPENGL
            "SOFTWARE", "SOFTWARE_FAST", "DIRECT_SOFTWARE" -> GraphicsApi.SOFTWARE_FAST
            "SOFTWARE_COMPAT" -> GraphicsApi.SOFTWARE_COMPAT
            else -> throw IllegalArgumentException("Kotlin/Native Linux does not support $value rendering")
        }

    internal fun parseSize(value: String?): Long {
        if (value == null) return -1L
        val normalized = value.trim().uppercase()
        val multiplier =
            when {
                normalized.endsWith("K") -> 1024L
                normalized.endsWith("M") -> 1024L * 1024L
                normalized.endsWith("G") -> 1024L * 1024L * 1024L
                else -> 1L
            }
        val number = if (multiplier == 1L) normalized else normalized.dropLast(1)
        return number.toLongOrNull()?.times(multiplier)
            ?: throw IllegalArgumentException("Invalid size format: $value")
    }
}

internal val SupportedLinuxRenderApis =
    setOf(GraphicsApi.OPENGL, GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT)

@OptIn(ExperimentalForeignApi::class)
private fun environment(name: String): String? = getenv(name)?.toKString()

private fun environmentBoolean(name: String, default: Boolean): Boolean =
    environment(name)?.toBooleanStrictOrNull() ?: default
