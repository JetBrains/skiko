package org.jetbrains.skiko.native.redrawer

import org.jetbrains.skiko.native.*
import org.jetbrains.skiko.redrawer.Redrawer

internal class IosOpenGLRedrawer(
    private val layer: HardwareLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    override fun dispose() {
        TODO()
    }

    override fun syncSize() {
        TODO()
    }

    override fun needRedraw() {
        TODO()
    }

    override fun redrawImmediately() {
        TODO()
    }
}
