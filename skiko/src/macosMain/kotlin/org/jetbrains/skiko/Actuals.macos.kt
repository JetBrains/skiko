package org.jetbrains.skiko

import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.context.MacOSOpenGLContextHandler
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

internal actual fun createNativeContextHandler(
    layer: SkiaLayer, renderApi: GraphicsApi
): ContextHandler = when (renderApi) {
    GraphicsApi.OPENGL -> MacOSOpenGLContextHandler(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}

internal actual fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi,
    properties: SkiaLayerProperties
): Redrawer {
    return MacOsOpenGLRedrawer(layer, properties)
}