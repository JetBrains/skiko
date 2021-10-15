package org.jetbrains.skiko.sample

import kotlinx.browser.document
import org.jetbrains.skiko.GenericRenderer
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

fun main() {
    onWasmReady {
        val skiaLayer = SkiaLayer()
        skiaLayer.renderer = GenericRenderer(skiaLayer, RotatingSquare())
        val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
        skiaLayer.setCanvas(canvas)
        skiaLayer.draw()
    }
}
