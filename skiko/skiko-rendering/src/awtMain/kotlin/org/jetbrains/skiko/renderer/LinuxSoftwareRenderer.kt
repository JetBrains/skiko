package org.jetbrains.skiko.renderer

import org.jetbrains.skiko.*

internal class LinuxSoftwareRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : AbstractDirectSoftwareRenderer(layer, analytics, properties) {

    init {
        onDeviceChosen("Software")
        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)
        layer.backedLayer.lockLinuxDrawingSurface {
            device = createDevice(it.display, it.window, w, h).also {
                if (it == 0L) {
                    throw RenderException("Failed to create Software device")
                }
            }
        }
        onContextInit()
    }

    override fun releaseResources() = layer.backedLayer.lockLinuxDrawingSurface {
        super.releaseResources()
    }

    override fun LayerDrawScope.draw() = layer.backedLayer.lockLinuxDrawingSurface {
        performDraw()
    }

    override fun resize(width: Int, height: Int) = layer.backedLayer.lockLinuxDrawingSurface {
        super.resize(width, height)
    }

    override fun finishFrame(surface: Long) = layer.backedLayer.lockLinuxDrawingSurface {
        super.finishFrame(surface)
    }

    private external fun createDevice(display: Long, window: Long, width: Int, height: Int): Long
}