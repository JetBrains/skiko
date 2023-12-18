package org.jetbrains.skiko.sample

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

fun main() {
    window.addEventListener("DOMContentLoaded", {
        onWasmReady(::runApp)
    })
}

internal fun runApp() {
    val skiaLayer = SkiaLayer()
    skiaLayer.onContentScaleChanged = { scale -> println(scale) }
    val game = JsClocks(skiaLayer)
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, game)
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    skiaLayer.attachTo(canvas)
    skiaLayer.needRedraw()
}