package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.X11OpenGLRedrawer
import org.jetbrains.skiko.redrawer.X11SoftwareRedrawer

internal fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi
): Redrawer = when (renderApi) {
    GraphicsApi.OPENGL -> createOpenGlOrSoftwareRedrawer(layer)
    GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT -> X11SoftwareRedrawer(layer)
    else -> createOpenGlOrSoftwareRedrawer(layer).also { layer.renderApi = GraphicsApi.OPENGL }
}

private fun createOpenGlOrSoftwareRedrawer(layer: SkiaLayer): Redrawer =
    try {
        X11OpenGLRedrawer(layer)
    } catch (_: Throwable) {
        Logger.warn { "OpenGL renderer init failed; falling back to software renderer" }
        layer.renderApi = GraphicsApi.SOFTWARE_FAST
        X11SoftwareRedrawer(layer)
    }
