package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal interface SwingRedrawer {
    fun dispose()

    fun redraw(g: Graphics2D)
}

@OptIn(ExperimentalSkikoApi::class)
internal fun createDefaultSwingRedrawer(
    layer: SkiaSwingLayer,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
): SwingRedrawer {
    return when (hostOs) {
        OS.MacOS -> when (renderApi) {
            GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareSwingRedrawer(layer, analytics)
            else -> MetalSwingRedrawer(layer, analytics, properties)
        }

        else -> SoftwareSwingRedrawer(layer, analytics)
    }
}