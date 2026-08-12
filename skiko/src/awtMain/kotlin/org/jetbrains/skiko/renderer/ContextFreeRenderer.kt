package org.jetbrains.skiko.renderer

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics

internal abstract class ContextFreeRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    graphicsApi: GraphicsApi
) : AwtRenderer(layer, analytics, graphicsApi) {
    private var isInitialized = false

    override fun initContext(): Boolean {
        if (!isInitialized) {
            isInitialized = true
            onContextInitialized()
        }
        return isInitialized
    }
}