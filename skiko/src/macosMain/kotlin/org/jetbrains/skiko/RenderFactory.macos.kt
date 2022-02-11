package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.MacOsMetalRedrawer
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

/**
 * Creates an instance of [Redrawer] using [renderApi].
 * Valid values for [renderApi] are: [GraphicsApi.OPENGL], [GraphicsApi.METAL].
 * If [renderApi] is not one of the valid, then throws IAE.
 */
internal fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi
): Redrawer = when (renderApi) {
    GraphicsApi.OPENGL -> MacOsOpenGLRedrawer(layer)
    GraphicsApi.METAL -> MacOsMetalRedrawer(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}
