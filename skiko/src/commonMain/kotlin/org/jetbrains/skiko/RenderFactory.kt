package org.jetbrains.skiko


import org.jetbrains.skiko.context.*
import org.jetbrains.skiko.redrawer.*

internal interface RenderFactory {
    fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler
    fun createRedrawer(layer: SkiaLayer, renderApi: GraphicsApi, properties: SkiaLayerProperties): Redrawer

    companion object {
        val Default = makeDefaultRenderFactory()
    }
}

internal expect fun makeDefaultRenderFactory(): RenderFactory