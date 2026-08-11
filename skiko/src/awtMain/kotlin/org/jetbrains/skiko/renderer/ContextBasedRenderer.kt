package org.jetbrains.skiko.renderer

import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics

internal abstract class ContextBasedRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    graphicsApi: GraphicsApi,
    val name: String
) : AwtRenderer(layer, analytics, graphicsApi) {

    protected abstract fun makeContext(): DirectContext

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = makeContext()
                onContextInitialized()
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia $name context!" }
            return false
        }
        return true
    }
}