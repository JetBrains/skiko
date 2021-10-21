package org.jetbrains.skiko

internal expect inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R
internal expect fun makeDefaultSkiaLayerProperties(): SkiaLayerProperties

internal expect fun currentNanoTime(): Long