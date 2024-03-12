package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import java.awt.Graphics2D

/**
 * Provides an interface for requesting content to be drawn on a [java.awt.Graphics2D].
 *
 * See [org.jetbrains.skiko.redrawer.Redrawer] redrawer for on-screen rendering
 */
internal interface SwingRedrawer {
    /**
     * Should be called when [SwingRedrawer] no longer needed to free native resources
     */
    fun dispose()

    /**
     * Draw content synchronously on given [java.awt.Graphics2D].
     * Content will be drawn off-screen using Skia engine and then passed to [java.awt.Graphics2D]
     */
    fun redraw(g: Graphics2D)
}

/**
 * Creates a [SwingRedrawer] that will draw content provided by [skikoView]
 */
internal fun createSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    renderDelegate: SkikoRenderDelegate,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
): SwingRedrawer {
    if (renderApi == GraphicsApi.SOFTWARE_COMPAT || renderApi == GraphicsApi.SOFTWARE_FAST) {
        return SoftwareSwingRedrawer(
            swingLayerProperties,
            renderDelegate,
            analytics
        )
    }
    return when (hostOs) {
        OS.MacOS -> MetalSwingRedrawer(swingLayerProperties, renderDelegate, analytics)
        OS.Windows -> Direct3DSwingRedrawer(swingLayerProperties, renderDelegate, analytics)
        OS.Linux -> LinuxOpenGLSwingRedrawer(swingLayerProperties, renderDelegate, analytics)
        else -> SoftwareSwingRedrawer(swingLayerProperties, renderDelegate, analytics)
    }
}