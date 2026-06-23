package org.jetbrains.skiko

import org.w3c.dom.HTMLElement

internal actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()

actual fun currentNanoTime(): Long = currentNanoTimeWindowPerformance().toLong()

internal actual fun loadOpenGLLibrary() {
   // Nothing to do here
}

internal actual fun loadAngleLibrary() {
    // Nothing to do here
}

private fun currentNanoTimeWindowPerformance() : Double =
    //language=JavaScript
    js("window.performance.now() * 1000000")

annotation class WebImport(val name : String)
