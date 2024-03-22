package org.jetbrains.skiko.sample

import kotlinx.browser.document
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.w3c.dom.HTMLCanvasElement

fun main() {
    runApp()
}

internal fun runApp() {
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    val skiaLayer = SkiaLayer()
    val clocks = WebClocks(skiaLayer, canvas)
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, clocks)
    skiaLayer.needRedraw()
}
