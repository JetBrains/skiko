package org.jetbrains.skiko

class SkiaLayerProperties(
    val isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
    val isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled
)