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
    val frameBuffering: FrameBuffering = SkikoProperties.frameBuffering,
    val renderApi: GraphicsApi = SkikoProperties.renderApi,
    val adapterPriority: GpuPriority = SkikoProperties.gpuPriority,
    val gpuResourceCacheLimit: Long = SkikoProperties.gpuResourceCacheLimit
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val rhs = other as? SkiaLayerProperties ?: return false

        if (isVsyncEnabled != rhs.isVsyncEnabled) return false
        if (isVsyncFramelimitFallbackEnabled != rhs.isVsyncFramelimitFallbackEnabled) return false
        if (frameBuffering != rhs.frameBuffering) return false
        if (renderApi != rhs.renderApi) return false
        if (adapterPriority != rhs.adapterPriority) return false
        if (gpuResourceCacheLimit != rhs.gpuResourceCacheLimit) return false

        return true
    }

    fun copy(
        isVsyncEnabled: Boolean = this.isVsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = this.isVsyncFramelimitFallbackEnabled,
        frameBuffering: FrameBuffering = this.frameBuffering,
        renderApi: GraphicsApi = this.renderApi,
        adapterPriority: GpuPriority = this.adapterPriority,
        gpuResourceCacheLimit: Long = this.gpuResourceCacheLimit,
    ): SkiaLayerProperties {
        return SkiaLayerProperties(
            isVsyncEnabled = isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled = isVsyncFramelimitFallbackEnabled,
            frameBuffering = frameBuffering,
            renderApi = renderApi,
            adapterPriority = adapterPriority,
            gpuResourceCacheLimit = gpuResourceCacheLimit,
        )
    }

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
