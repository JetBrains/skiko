package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.*

internal class LinuxSoftwareRedrawer(
    private val layer: SkiaLayer,
    properties: SkiaLayerProperties
) : AbstractDirectSoftwareRedrawer(layer, properties) {

    init {
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
    }

    override fun dispose() = layer.backedLayer.lockLinuxDrawingSurface {
        super.dispose()
    }

    override fun draw(scope: LayerDrawScope) = layer.backedLayer.lockLinuxDrawingSurface {
        super.draw(scope)
    }

    override fun resize(width: Int, height: Int) = layer.backedLayer.lockLinuxDrawingSurface {
        super.resize(width, height)
    }

    override fun finishFrame(surface: Long) = layer.backedLayer.lockLinuxDrawingSurface {
        super.finishFrame(surface)
    }

    private external fun createDevice(display: Long, window: Long, width: Int, height: Int): Long
}
