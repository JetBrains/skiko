package org.jetbrains.skiko.sample

import kotlinx.browser.document
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.w3c.dom.HTMLCanvasElement

fun main() {
    runApp()
}

internal fun runApp() {
    val skiaLayer = SkiaLayer()
    val game = JsClocks(skiaLayer)
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, game)
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    skiaLayer.attachTo(canvas)
    skiaLayer.needRedraw()
}
