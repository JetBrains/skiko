package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.LinuxOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

internal fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi
): Redrawer = when (renderApi) {
    GraphicsApi.OPENGL -> LinuxOpenGLRedrawer(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}
