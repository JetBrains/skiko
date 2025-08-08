package org.jetbrains.skiko

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = kotlin.system.getTimeNanos()

internal actual fun loadAngleLibrary() {
    // Nothing to do here
}