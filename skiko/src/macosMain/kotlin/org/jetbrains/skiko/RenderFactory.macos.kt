package org.jetbrains.skiko

import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.MacOsMetalRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

internal fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi
): Redrawer = when (renderApi) {
    GraphicsApi.OPENGL -> MacOsOpenGLRedrawer(layer)
    GraphicsApi.METAL -> MacOsMetalRedrawer(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}