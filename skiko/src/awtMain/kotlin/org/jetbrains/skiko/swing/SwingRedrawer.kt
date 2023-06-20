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
    skikoView: SkikoView,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
): SwingRedrawer {
    return when (hostOs) {
        OS.MacOS -> when (renderApi) {
            GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareSwingRedrawer(
                swingLayerProperties,
                skikoView,
                analytics
            )

            else -> MetalSwingRedrawer(swingLayerProperties, skikoView, analytics)
        }
        OS.Linux -> when (renderApi) {
            GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareSwingRedrawer(
                swingLayerProperties,
                skikoView,
                analytics
            )
            else -> LinuxOpenGLSwingRedrawer(swingLayerProperties, skikoView, analytics)
        }

        else -> SoftwareSwingRedrawer(swingLayerProperties, skikoView, analytics)
    }
}