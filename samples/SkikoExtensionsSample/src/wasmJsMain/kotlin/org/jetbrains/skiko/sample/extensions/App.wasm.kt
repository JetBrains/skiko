package org.jetbrains.skiko.sample.extensions

import kotlinx.browser.document
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

fun main() {
    onWasmReady {
        runApp()
    }
}

internal fun runApp() {
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    val skiaLayer = SkiaLayer()
    skiaLayer.attachTo(canvas)
    configureSkottieLayer(skiaLayer)
    skiaLayer.needRender()
    canvas.setAttribute("data-skiko-ready", "true")
}
