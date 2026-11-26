package org.jetbrains.skiko

actual fun currentNanoTime(): Long = currentNanoTimePerformance().toLong()

private fun currentNanoTimePerformance(): Double =
    //language=JavaScript
    js("globalThis.performance.now() * 1000000")

annotation class WebImport(val name: String)
