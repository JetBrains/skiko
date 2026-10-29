package org.jetbrains.skiko

import org.jetbrains.skiko.renderer.FrameDriver

internal fun interface RenderFactory {
    fun createFrameDriver(
        layer: SkiaLayer,
        renderApi: GraphicsApi,
        analytics: SkiaLayerAnalytics,
        properties: SkiaLayerProperties
    ): FrameDriver

    companion object {
        val Default = makeDefaultRenderFactory()
    }
}
