package org.jetbrains.skiko

@InternalSkikoApi
actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = currentNanoTimeWindowPerformance().toLong()

private fun currentNanoTimeWindowPerformance(): Double =
    //language=JavaScript
    js("window.performance.now() * 1000000")

annotation class WebImport(val name: String)
