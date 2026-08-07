package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import kotlin.time.TimeSource

private val initialTime = TimeSource.Monotonic.markNow()
internal fun renderTime() = initialTime.elapsedNow().inWholeNanoseconds

internal interface Redrawer {
    fun dispose()
    fun needRender(throttledToVsync: Boolean)
    fun renderImmediately()
    fun syncBoundsFromPlatformComponent() = Unit
    fun update(nanoTime: Long = renderTime())
    fun setVisible(isVisible: Boolean) = Unit
    val renderInfo: String
    fun isTransparentBackgroundSupported(): Boolean
    /**
     * Invoked by AWT [SkiaLayer] when the underlying Swing component is resized. Unused in other source-sets.
     */
    fun onLayerComponentResized() = Unit
}

internal fun defaultIsTransparentBackgroundSupported(layer: SkiaLayer): Boolean {
    if (hostOs == OS.MacOS) {
        // macOS transparency is always supported
        return true
    }

    // for non-macOS in fullscreen transparency is not supported
    return !layer.fullscreen
}