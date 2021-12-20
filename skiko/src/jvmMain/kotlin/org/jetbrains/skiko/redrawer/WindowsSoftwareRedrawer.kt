package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.RenderException

internal class WindowsSoftwareRedrawer(
    layer: SkiaLayer,
    properties: SkiaLayerProperties
) : AbstractDirectSoftwareRedrawer(layer, properties) {

    init {
        device = createDevice(layer.contentHandle, layer.transparency).also {
            if (it == 0L) {
                throw RenderException("Failed to create Software device")
            }
        }
    }

    private external fun createDevice(contentHandle: Long, transparency: Boolean): Long
}