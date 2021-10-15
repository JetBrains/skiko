package org.jetbrains.skiko

internal actual fun makeDefaultSkiaLayerProperties(): SkiaLayerProperties {
    return SkiaLayerProperties(true, true)
}

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()