package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal class SoftwareSwingRedrawer(
    skiaSwingLayer: SkiaSwingLayer,
    skikoView: SkikoView,
    analytics: SkiaLayerAnalytics,
    clipComponents: MutableList<ClipRectangle>,
    renderExceptionHandler: (e: RenderException) -> Unit,
) : SwingRedrawerBase(
    skiaSwingLayer,
    skikoView,
    analytics,
    GraphicsApi.SOFTWARE_FAST,
    clipComponents,
    renderExceptionHandler
) {
    init {
        onDeviceChosen("Software")
    }

    override val contextHandler = SoftwareSwingContextHandler(skiaSwingLayer, this::draw).also {
        onContextInit()
    }
}