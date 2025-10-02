package org.jetbrains.skiko

import kotlin.time.TimeSource

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

private val markNow = TimeSource.Monotonic.markNow()
actual fun currentNanoTime(): Long = markNow.elapsedNow().inWholeNanoseconds

internal actual fun loadAngleLibrary() {
    // Nothing to do here
}
