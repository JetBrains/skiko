package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.Surface
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.FrameLimiter
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties

internal class WindowsSoftwareRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : AbstractDirectSoftwareRedrawer(layer, properties) {

    init {
        device = createDevice(layer.contentHandle).also {
            if (it == 0L) {
                throw IllegalArgumentException("Failed to create Software device.")
            }
        }
    }

    private external fun createDevice(contentHandle: Long): Long
}