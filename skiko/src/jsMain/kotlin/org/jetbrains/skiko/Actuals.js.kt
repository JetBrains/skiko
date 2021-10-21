package org.jetbrains.skiko

internal actual fun makeDefaultSkiaLayerProperties(): SkiaLayerProperties {
    return SkiaLayerProperties(true, true)
}

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = (kotlinx.browser.window.performance.now() * 1_000_000).toLong()