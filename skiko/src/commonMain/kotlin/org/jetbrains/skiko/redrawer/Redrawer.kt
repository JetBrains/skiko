package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import kotlin.time.TimeSource

private val initialTime = TimeSource.Monotonic.markNow()

internal interface Redrawer {
    fun dispose()
    fun needRender(throttledToVsync: Boolean)
    fun renderImmediately()
    fun syncBounds() = Unit

    /**
     * Whether the platform is driving an interactive window resize itself (e.g., the macOS/Metal
     * live resize). While set, [SkiaLayer] suppresses the reshape-driven [syncBounds]/[needRender]
     * so they don't race that path.
     */
    val isAutoResizing: Boolean get() = false
    fun update(nanoTime: Long = initialTime.elapsedNow().inWholeNanoseconds)
    fun setVisible(isVisible: Boolean) = Unit
    val renderInfo: String
    fun isTransparentBackgroundSupported(): Boolean
}

internal fun defaultIsTransparentBackgroundSupported(layer: SkiaLayer): Boolean {
    if (hostOs == OS.MacOS) {
        // macOS transparency is always supported
        return true
    }

    // for non-macOS in fullscreen transparency is not supported
    return !layer.fullscreen
}