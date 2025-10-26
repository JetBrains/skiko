@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko.sample

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Minimal stub to keep LinuxClocks working. With KGFW-driven App, this is unused.
 */
class LinuxWindow(
    title: String,
    width: UInt,
    height: UInt
) {
    var mouseX: Double = 0.0
        private set

    var mouseY: Double = 0.0
        private set

    val shouldClose: Boolean
        get() = false

    fun pollEvents() { /* no-op */ }

    fun getWindowHandle(): ULong = 0u

    fun destroy() { /* no-op */ }
}
