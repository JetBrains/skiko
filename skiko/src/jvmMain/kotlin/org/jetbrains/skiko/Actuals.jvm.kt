package org.jetbrains.skiko

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    synchronized(lock, block)

actual fun currentNanoTime(): Long = System.nanoTime()