package org.jetbrains.skiko

import org.jetbrains.skiko.context.*
import org.jetbrains.skiko.redrawer.*

internal expect fun createNativeContextHandler(layer: SkiaLayer): ContextHandler

internal expect fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi,
    properties: SkiaLayerProperties
): Redrawer

internal actual fun makeDefaultRenderFactory(): RenderFactory {
    return object : RenderFactory {
        override fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
            return createNativeContextHandler(layer)
        }

        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer {
            return createNativeRedrawer(layer, renderApi, properties)
        }
    }
}
