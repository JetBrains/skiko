package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.ISize
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import kotlin.time.TimeSource

private val initialTime = TimeSource.Monotonic.markNow()

internal interface Redrawer {
    fun dispose()
    fun needRender(throttledToVsync: Boolean)
    fun renderImmediately()
    fun syncBoundsFromPlatformComponent() = Unit
    fun update(nanoTime: Long = initialTime.elapsedNow().inWholeNanoseconds)
    fun setVisible(isVisible: Boolean) = Unit
    val renderInfo: String
    fun isTransparentBackgroundSupported(): Boolean

    /**
     * When the redrawer itself is handling and driving the resizing of the layer, this is the current real size of
     * the layer, in pixels.
     */
    val layerSizeWhileHandlingSizing: ISize? get() = null
}

/**
 * Whether the redrawer itself is handling and driving the resizing of the layer.
 *
 * When this returns `true`, [SkiaLayer] should not respond to resize events from the underlying platform component.
 * Note that this value is not necessarily fixed over time.
 */
internal val Redrawer?.isHandlingSizing: Boolean
    get() = this?.layerSizeWhileHandlingSizing != null


internal fun defaultIsTransparentBackgroundSupported(layer: SkiaLayer): Boolean {
    if (hostOs == OS.MacOS) {
        // macOS transparency is always supported
        return true
    }

    // for non-macOS in fullscreen transparency is not supported
    return !layer.fullscreen
}