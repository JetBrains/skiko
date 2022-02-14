package org.jetbrains.skiko

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = (kotlinx.browser.window.performance.now() * 1_000_000).toLong()

actual fun openUri(uri: String) {
    kotlinx.browser.window.open(uri, target = "_blank")
}