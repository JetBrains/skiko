package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.*

internal class LinuxSoftwareRedrawer(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AbstractDirectSoftwareRedrawer(host, analytics, properties) {

    init {
        onDeviceChosen("Software")
        val scale = host.contentScale
        val w = (host.width * scale).toInt().coerceAtLeast(0)
        val h = (host.height * scale).toInt().coerceAtLeast(0)
        host.backedLayer.lockLinuxDrawingSurface {
            device = createDevice(it.display, it.window, w, h).also {
                if (it == 0L) {
                    throw RenderException("Failed to create Software device")
                }
            }
        }
        onContextInit()
    }

    override fun releaseResources() = host.backedLayer.lockLinuxDrawingSurface {
        super.releaseResources()
    }

    override fun draw(scope: LayerDrawScope) = host.backedLayer.lockLinuxDrawingSurface {
        super.draw(scope)
    }

    override fun resize(width: Int, height: Int) = host.backedLayer.lockLinuxDrawingSurface {
        super.resize(width, height)
    }

    override fun finishFrame(surface: Long) = host.backedLayer.lockLinuxDrawingSurface {
        super.finishFrame(surface)
    }

    private external fun createDevice(display: Long, window: Long, width: Int, height: Int): Long
}