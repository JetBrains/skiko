package org.jetbrains.skiko.swing

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayerAnalytics
import java.awt.Graphics2D

internal class SoftwareSwingRedrawer(
    skiaSwingLayer: SkiaSwingLayer,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(skiaSwingLayer, analytics, GraphicsApi.SOFTWARE_FAST) {
    init {
        onDeviceChosen("Software")
    }

    private val contextHandler = SoftwareSwingContextHandler(skiaSwingLayer).also {
        onContextInit()
    }

    override fun dispose() {
        contextHandler.dispose()
        super.dispose()
    }

    override fun redraw(g: Graphics2D) {
        update(System.nanoTime())
        inDrawScope {
            contextHandler.draw(g)
        }
    }
}