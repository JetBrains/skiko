package org.jetbrains.skiko

import org.jetbrains.skiko.renderer.*

internal fun interface RenderFactory {
    fun createRenderer(
        layer: SkiaLayer,
        renderApi: GraphicsApi,
        analytics: SkiaLayerAnalytics,
        properties: SkiaLayerProperties
    ): Renderer

    companion object {
        val Default = makeDefaultRenderFactory()
    }
}

internal expect fun makeDefaultRenderFactory(): RenderFactory
