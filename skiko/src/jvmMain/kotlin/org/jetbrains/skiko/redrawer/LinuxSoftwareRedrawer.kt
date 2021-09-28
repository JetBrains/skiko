package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.Surface
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.*

internal class LinuxSoftwareRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : AbstractDirectSoftwareRedrawer(layer, properties) {

    init {
        layer.backedLayer.lockLinuxDrawingSurface {
            device = createDevice(it.display, it.window).also {
                if (it == 0L) {
                    throw IllegalArgumentException("Failed to create Software device.")
                }
            }
        }
    }

    override fun redrawImmediately() = layer.backedLayer.lockLinuxDrawingSurface {
        super.redrawImmediately()
    }

    override fun resize(width: Int, height: Int) = layer.backedLayer.lockLinuxDrawingSurface {
        super.resize(width, height)
    }

    override fun finishFrame() = layer.backedLayer.lockLinuxDrawingSurface {
        super.finishFrame()
    }

    private external fun createDevice(display: Long, window: Long): Long
}