package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.SurfaceProps
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
        device = interopScope {
            createDevice(layer.contentHandle, toInterop(SurfaceProps(pixelGeometry = layer.pixelGeometry).packToIntArray()), layer.transparency).also {
                if (it == 0L) {
                    throw RenderException("Failed to create Software device")
                }
            }
        }
        onContextInit()
    }

    private external fun createDevice(contentHandle: Long, surfacePropsIntArray: InteropPointer, transparency: Boolean): Long
}