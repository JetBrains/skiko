package org.jetbrains.skiko

import org.jetbrains.skiko.context.*
import org.jetbrains.skiko.redrawer.*

internal actual fun makeDefaultRenderFactory(): RenderFactory {
    return object : RenderFactory {
        override fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
            TODO()
        }

        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer {
            TODO()
        }
    }
}
