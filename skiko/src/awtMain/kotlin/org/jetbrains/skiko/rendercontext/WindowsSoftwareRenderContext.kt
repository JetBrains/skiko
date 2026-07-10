package org.jetbrains.skiko.rendercontext

import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayerAnalytics

internal class WindowsSoftwareRenderContext(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AbstractDirectSoftwareRenderContext(host, analytics, properties) {

    init {
        onDeviceChosen("Software")
        device = interopScope {
            createDevice(host.contentHandle, toInterop(SurfaceProps(pixelGeometry = host.pixelGeometry).packToIntArray()), host.transparency).also {
                if (it == 0L) {
                    throw RenderException("Failed to create Software device")
                }
            }
        }
        onContextInit()
    }

    private external fun createDevice(contentHandle: Long, surfacePropsIntArray: InteropPointer, transparency: Boolean): Long
}