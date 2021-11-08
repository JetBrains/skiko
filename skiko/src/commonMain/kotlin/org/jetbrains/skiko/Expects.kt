package org.jetbrains.skiko

internal expect inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R

internal expect fun currentNanoTime(): Long