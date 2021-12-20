package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.*

internal interface RenderFactory {
    fun createRedrawer(layer: SkiaLayer, renderApi: GraphicsApi, properties: SkiaLayerProperties): Redrawer

    companion object {
        val Default = makeDefaultRenderFactory()
    }
}

internal expect fun makeDefaultRenderFactory(): RenderFactory
