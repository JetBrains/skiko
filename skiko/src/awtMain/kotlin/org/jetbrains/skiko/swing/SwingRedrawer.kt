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
    skikoView: SkikoView,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties,
    clipComponents: MutableList<ClipRectangle>,
    renderExceptionsHandler: (e: RenderException) -> Unit
): SwingRedrawer {
    return when (hostOs) {
        OS.MacOS -> when (renderApi) {
            GraphicsApi.SOFTWARE_COMPAT, GraphicsApi.SOFTWARE_FAST -> SoftwareSwingRedrawer(
                layer,
                skikoView,
                analytics,
                clipComponents,
                renderExceptionsHandler
            )

            else -> MetalSwingRedrawer(layer, skikoView, analytics, properties, clipComponents, renderExceptionsHandler)
        }

        else -> SoftwareSwingRedrawer(layer, skikoView, analytics, clipComponents, renderExceptionsHandler)
    }
}