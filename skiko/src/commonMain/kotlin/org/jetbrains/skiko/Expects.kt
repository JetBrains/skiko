package org.jetbrains.skiko

@InternalSkikoApi
expect inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R

expect fun currentNanoTime(): Long
