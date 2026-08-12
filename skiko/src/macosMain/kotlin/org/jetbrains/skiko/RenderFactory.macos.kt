package org.jetbrains.skiko

import org.jetbrains.skiko.renderer.MacOsMetalRenderer
import org.jetbrains.skiko.renderer.MacOsOpenGLRenderer
import org.jetbrains.skiko.renderer.Renderer

/**
 * Creates an instance of [Renderer] using [renderApi].
 * Valid values for [renderApi] are: [GraphicsApi.OPENGL], [GraphicsApi.METAL].
 * If [renderApi] is not one of the valid, then throws IllegalArgumentException.
 */
internal fun createNativeRenderer(
    layer: SkiaLayer,
    renderApi: GraphicsApi
): Renderer = when (renderApi) {
    GraphicsApi.OPENGL -> MacOsOpenGLRenderer(layer)
    GraphicsApi.METAL -> MacOsMetalRenderer(layer)
    else -> throw IllegalArgumentException("Unsupported API $renderApi")
}
