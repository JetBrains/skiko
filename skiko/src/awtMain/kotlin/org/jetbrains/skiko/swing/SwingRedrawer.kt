package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal interface SwingRedrawer {
    fun dispose()

    fun redraw(g: Graphics2D)
}

@OptIn(ExperimentalSkikoApi::class)
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

        else -> SoftwareSwingRedrawer(swingLayerProperties, skikoView, analytics)
    }
}