package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import java.awt.Graphics2D

/**
 * Provides an interface for requesting content to be drawn on a [java.awt.Graphics2D].
 *
 * See [org.jetbrains.skiko.renderer.FrameDriver] for on-screen rendering
 */
internal interface SwingRenderer {
    /**
     * Should be called when [SwingRenderer] no longer needed to free native resources
     */
    fun dispose()

    /**
     * Draw content synchronously on given [java.awt.Graphics2D].
     * Content will be drawn off-screen using Skia engine and then passed to [java.awt.Graphics2D]
     */
    fun redraw(g: Graphics2D)
}

/**
 * Creates a [SwingRenderer] that will draw content provided by [renderDelegate]
 */
internal fun createSwingRenderer(
    swingLayerProperties: SwingLayerProperties,
    renderDelegate: SkikoRenderDelegate,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
): SwingRenderer {
    if (renderApi == GraphicsApi.SOFTWARE_COMPAT || renderApi == GraphicsApi.SOFTWARE_FAST) {
        return SoftwareSwingRenderer(
            swingLayerProperties,
            renderDelegate,
            analytics
        )
    }
    return when (hostOs) {
        OS.MacOS -> MetalSwingRenderer(swingLayerProperties, renderDelegate, analytics)
        OS.Windows -> Direct3DSwingRenderer(swingLayerProperties, renderDelegate, analytics)
        OS.Linux -> LinuxOpenGLSwingRenderer(swingLayerProperties, renderDelegate, analytics)
        else -> SoftwareSwingRenderer(swingLayerProperties, renderDelegate, analytics)
    }
}