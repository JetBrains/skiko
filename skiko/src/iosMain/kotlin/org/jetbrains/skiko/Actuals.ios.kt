package org.jetbrains.skiko

import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.redrawer.MetalRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

internal actual fun createNativeContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
    TODO()
}

internal actual fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi,
    properties: SkiaLayerProperties
): Redrawer {
    return MetalRedrawer(layer, properties)
}