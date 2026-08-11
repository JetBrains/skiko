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
            device = createDevice(it.display, it.window, w, h).also { device ->
                if (device == 0L) {
                    throw RenderException("Failed to create Software device")
                }
            }
        }
        onContextInit()
    }

    override fun dispose() = layer.backedLayer.lockLinuxDrawingSurface {
        super.dispose()
    }

    override fun drawFrame() = layer.backedLayer.lockLinuxDrawingSurface {
        super.drawFrame()
    }

    override fun renderImmediately() = layer.backedLayer.lockLinuxDrawingSurface {
        super.renderImmediately()
    }

    override fun resize(width: Int, height: Int) = layer.backedLayer.lockLinuxDrawingSurface {
        super.resize(width, height)
    }

    override fun finishFrame(surface: Long) = layer.backedLayer.lockLinuxDrawingSurface {
        super.finishFrame(surface)
    }

    private external fun createDevice(display: Long, window: Long, width: Int, height: Int): Long
}