package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.MetalOffScreenRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

/**
 * [RenderFactory] provides [Redrawer] that uses off-screen rendering,
 * so content is drawn in memory and then passed to [java.awt.Graphics]
 * instead of drawing directly on display.
 *
 * For now, only MacOs is supported, other OSes fallbacked to [RenderFactory.Default]
 */
@ExperimentalSkikoApi
internal val DefaultOffScreenRendererFactory: RenderFactory = makeOffScreenRendererFactory()

@ExperimentalSkikoApi
private fun makeOffScreenRendererFactory(): RenderFactory {
    val defaultRenderFactory = RenderFactory.Default
    return object : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            analytics: SkiaLayerAnalytics,
            properties: SkiaLayerProperties
        ): Redrawer {
            return if (hostOs == OS.MacOS && renderApi != GraphicsApi.SOFTWARE_COMPAT && renderApi != GraphicsApi.SOFTWARE_FAST) {
                MetalOffScreenRedrawer(layer, analytics, properties)
            } else {
                defaultRenderFactory.createRedrawer(layer, renderApi, analytics, properties)
            }
        }
    }
}