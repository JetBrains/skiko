package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.RenderException

internal class WindowsSoftwareRedrawer(
    layer: SkiaLayer,
    properties: SkiaLayerProperties
) : AbstractDirectSoftwareRedrawer(layer, properties) {

    init {
        device = interopScope {
            createDevice(layer.contentHandle, toInterop(SurfaceProps(pixelGeometry = layer.pixelGeometry).packToIntArray()), layer.transparency).also {
                if (it == 0L) {
                    throw RenderException("Failed to create Software device")
                }
            }
        }
    }

    private external fun createDevice(contentHandle: Long, surfacePropsIntArray: InteropPointer, transparency: Boolean): Long
}
