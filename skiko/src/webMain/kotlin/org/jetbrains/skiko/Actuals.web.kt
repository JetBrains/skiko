package org.jetbrains.skiko

actual fun currentNanoTime(): Long = currentNanoTimeWindowPerformance().toLong()

private fun currentNanoTimeWindowPerformance(): Double =
    //language=JavaScript
    js("window.performance.now() * 1000000")

annotation class WebImport(val name: String)
