package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerProperties

internal class MetalRedrawer(
    private val layer: SkiaLayer,
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
