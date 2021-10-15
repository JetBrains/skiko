package org.jetbrains.skiko

internal actual fun makeDefaultSkiaLayerProperties(): SkiaLayerProperties {
    return SkiaLayerProperties(SkikoProperties.vsyncEnabled, SkikoProperties.vsyncFramelimitFallbackEnabled)
}

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    synchronized(lock, block)