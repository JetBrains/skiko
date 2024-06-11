package org.jetbrains.skiko


/**
 * SkiaLayerProperties is a class that represents the rendering configuration for a SkiaLayer.
 *
 * @property isVsyncEnabled Specifies whether vertical synchronization (VSync) is enabled.
 * Default value is [SkikoProperties.vsyncEnabled]. Setting this to true is a hint toward underlying implementation
 * to synchronize the rendering with the display presentation. It guarantees that the frame is presented without
 * visual artifacts like tearing in exchange for a possible latency increase.
 * @property isVsyncFramelimitFallbackEnabled Specifies whether framelimit fallback is enabled (software renderer).
 * Default value is [SkikoProperties.vsyncFramelimitFallbackEnabled].
 * @property renderApi Specifies the graphics API used for rendering.
 * Default value is [SkikoProperties.renderApi].
 * @property adapterPriority Specifies the GPU that will be selected for rendering.
 * Default value is [SkikoProperties.gpuPriority].
 */
class SkiaLayerProperties(
    val isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
    val isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
    val renderApi: GraphicsApi = SkikoProperties.renderApi,
    val adapterPriority: GpuPriority = SkikoProperties.gpuPriority,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val rhs = other as? SkiaLayerProperties ?: return false

        if (isVsyncEnabled != rhs.isVsyncEnabled) return false
        if (isVsyncFramelimitFallbackEnabled != rhs.isVsyncFramelimitFallbackEnabled) return false
        if (renderApi != rhs.renderApi) return false
        if (adapterPriority != rhs.adapterPriority) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isVsyncEnabled.hashCode()
        result = 31 * result + isVsyncFramelimitFallbackEnabled.hashCode()
        result = 31 * result + renderApi.hashCode()
        result = 31 * result + adapterPriority.hashCode()
        return result
    }
}
