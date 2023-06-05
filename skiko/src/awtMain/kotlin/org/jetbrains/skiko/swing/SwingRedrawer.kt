package org.jetbrains.skiko.swing

import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.SkiaLayerAnalytics
import org.jetbrains.skiko.SkiaLayerProperties
import java.awt.Graphics2D

internal interface SwingRedrawer {
    fun dispose()

    fun redraw(g: Graphics2D)
}

@OptIn(ExperimentalSkikoApi::class)
internal fun createDefaultSwingRedrawer(
    layer: SkiaSwingLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
): SwingRedrawer = MetalSwingRedrawer(layer, analytics, properties)