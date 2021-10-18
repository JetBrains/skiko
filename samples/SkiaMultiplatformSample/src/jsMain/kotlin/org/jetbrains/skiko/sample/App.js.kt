package org.jetbrains.skiko.sample

import kotlinx.browser.document
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

fun main() {
    onWasmReady {
        val skiaLayer = SkiaLayer()
        val game = BouncingBalls()
        skiaLayer.skikoView = GenericSkikoView(skiaLayer, game)
        val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
        canvas.setAttribute("tabindex", "0")
        skiaLayer.setCanvas(canvas)
        skiaLayer.needRedraw()
    }
}
