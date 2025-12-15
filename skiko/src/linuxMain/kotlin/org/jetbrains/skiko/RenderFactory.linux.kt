package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.X11OpenGLRedrawer
import org.jetbrains.skiko.redrawer.X11SoftwareRedrawer
import org.jetbrains.skiko.redrawer.X11VulkanRedrawer

internal fun createNativeRedrawer(
    layer: SkiaLayer,
    renderApi: GraphicsApi
): Redrawer = when (renderApi) {
    GraphicsApi.VULKAN -> {
        try {
            X11VulkanRedrawer(layer)
        } catch (e: Throwable) {
            Logger.warn(e) { "Vulkan renderer init failed; falling back to OpenGL" }
            try {
                layer.renderApi = GraphicsApi.OPENGL
                X11OpenGLRedrawer(layer)
            } catch (_: Throwable) {
                Logger.warn { "OpenGL renderer init failed; falling back to software renderer" }
                layer.renderApi = GraphicsApi.SOFTWARE_FAST
                X11SoftwareRedrawer(layer)
            }
        }
    }
    GraphicsApi.OPENGL -> {
        try {
            X11OpenGLRedrawer(layer)
        } catch (_: Throwable) {
            Logger.warn { "OpenGL renderer init failed; falling back to software renderer" }
            layer.renderApi = GraphicsApi.SOFTWARE_FAST
            X11SoftwareRedrawer(layer)
        }
    }
    GraphicsApi.SOFTWARE_FAST, GraphicsApi.SOFTWARE_COMPAT -> X11SoftwareRedrawer(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}
