package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.*

internal actual fun makeDefaultRenderFactory(): RenderFactory {
    return object : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer {
            TODO()
        }
    }
}
