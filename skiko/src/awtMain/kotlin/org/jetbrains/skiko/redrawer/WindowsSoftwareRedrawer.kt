package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayerAnalytics

internal class WindowsSoftwareRedrawer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AbstractDirectSoftwareRedrawer(layer, analytics, properties) {

    init {
        onDeviceChosen("Software")
        device = createDevice(layer.contentHandle, layer.transparency).also {
            if (it == 0L) {
                throw RenderException("Failed to create Software device")
            }
        }
        onContextInit()
    }

    private external fun createDevice(contentHandle: Long, transparency: Boolean): Long
}