package org.jetbrains.skiko

internal actual fun makeDefaultSkiaLayerProperties(): SkiaLayerProperties {
    return SkiaLayerProperties(SkikoProperties.vsyncEnabled, SkikoProperties.vsyncFramelimitFallbackEnabled)
}