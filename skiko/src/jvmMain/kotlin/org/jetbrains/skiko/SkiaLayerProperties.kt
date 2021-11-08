package org.jetbrains.skiko

internal data class SkiaLayerProperties(
    val isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
    val isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
    val renderApi: GraphicsApi = SkikoProperties.renderApi,
)
