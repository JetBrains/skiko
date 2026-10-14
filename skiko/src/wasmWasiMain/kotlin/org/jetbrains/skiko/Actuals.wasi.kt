package org.jetbrains.skiko

actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R = block()

actual fun currentNanoTime(): Long =
    kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeNanoseconds

annotation class WasiImport(val name: String)
