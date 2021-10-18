package org.jetbrains.skiko.sample

import kotlinx.browser.document
import org.jetbrains.skiko.GenericSkikoApp
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

fun main() {
    onWasmReady {
        val skiaLayer = SkiaLayer()
        val game = BouncingBalls()
        val app = GenericSkikoApp(skiaLayer, game)
        val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
        skiaLayer.setCanvas(canvas)
        skiaLayer.draw()
    }
}
