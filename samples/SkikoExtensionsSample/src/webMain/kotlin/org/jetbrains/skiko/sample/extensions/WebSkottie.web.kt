package org.jetbrains.skiko.sample.extensions

import kotlinx.browser.document
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.w3c.dom.HTMLCanvasElement

internal fun configureSkottieLayer(layer: SkiaLayer) {
    mark("skottie-before-load-player")
    val player = loadSkottieAnimationPlayer(mark = ::mark)
    mark("skottie-after-load-player")
    layer.renderDelegate = SkiaLayerRenderDelegate(layer) { canvas, width, height, _ ->
        mark("skottie-before-render")
        player.render(canvas, width, height)
        mark("skottie-after-render")
    }
    mark("skottie-render-delegate-assigned")
}

private fun mark(stage: String) {
    skikoTarget()?.setAttribute("data-skiko-stage", stage)
    logSkikoStage(stage)
}

private fun skikoTarget(): HTMLCanvasElement? =
    document.getElementById("SkikoTarget") as? HTMLCanvasElement

@JsFun("(stage) => console.log('skiko-stage: ' + stage)")
private external fun logSkikoStage(stage: String)
