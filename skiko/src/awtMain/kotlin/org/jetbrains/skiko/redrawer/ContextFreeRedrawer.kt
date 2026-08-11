package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics

internal abstract class ContextFreeRedrawer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    graphicsApi: GraphicsApi
) : AwtRedrawer(layer, analytics, graphicsApi) {
    private var isInitialized = false

    override fun initContext(): Boolean {
        if (!isInitialized) {
            isInitialized = true
            onContextInitialized()
        }
        return isInitialized
    }
}