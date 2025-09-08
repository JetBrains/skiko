package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.AndroidNativeOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

/**
 * Creates an instance of [Redrawer] using [renderApi].
 */
internal fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi
): Redrawer = when (renderApi) {
    GraphicsApi.OPENGL -> AndroidNativeOpenGLRedrawer(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}
