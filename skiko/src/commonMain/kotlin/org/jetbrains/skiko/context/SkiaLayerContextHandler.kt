package org.jetbrains.skiko.context

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs

internal abstract class SkiaLayerContextHandler(
    protected val layer: SkiaLayer,
    drawContent: Canvas.() -> Unit
) : ContextHandler(drawContent) {
    override val renderApi: GraphicsApi
        get() = layer.renderApi

    override fun isTransparentBackground(): Boolean {
        if (hostOs == OS.MacOS) {
            // MacOS transparency is always supported
            return true
        }
        if (layer.fullscreen) {
            // for non-MacOS in fullscreen transparency is not supported
            return false
        }
        // for non-MacOS in non-fullscreen transparency provided by [layer]
        return layer.transparency
    }
}