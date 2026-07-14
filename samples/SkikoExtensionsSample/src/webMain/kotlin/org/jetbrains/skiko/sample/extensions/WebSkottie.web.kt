package org.jetbrains.skiko.sample.extensions

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate

internal fun configureSkottieLayer(layer: SkiaLayer) {
    val player = loadSkottieAnimationPlayer()
    layer.renderDelegate = SkiaLayerRenderDelegate(layer) { canvas, width, height, _ ->
        player.render(canvas, width, height)
    }
}
