package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.context.ContextHandler

internal actual fun createNativeContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
    TODO()
}

internal actual fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi,
    properties: SkiaLayerProperties
): Redrawer {
   TODO()
}
