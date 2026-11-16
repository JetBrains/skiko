package org.jetbrains.skiko

actual fun currentNanoTime(): Long = currentNanoTimeWindowPerformance().toLong()

@InternalSkikoApi
actual fun loadOpenGLLibrary() {
    // Nothing to do here
}

private fun currentNanoTimeWindowPerformance(): Double =
    //language=JavaScript
    js("window.performance.now() * 1000000")

annotation class WebImport(val name: String)
