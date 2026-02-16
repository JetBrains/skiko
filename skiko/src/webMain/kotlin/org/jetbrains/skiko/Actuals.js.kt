package org.jetbrains.skiko

import org.jetbrains.skiko.w3c.window

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = (window.performance.now() * 1_000_000).toLong()

internal actual fun loadOpenGLLibrary() {
   // Nothing to do here
}

internal actual fun loadAngleLibrary() {
    // Nothing to do here
}