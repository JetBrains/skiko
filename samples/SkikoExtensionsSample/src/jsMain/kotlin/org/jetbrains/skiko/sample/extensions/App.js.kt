package org.jetbrains.skiko.sample.extensions

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.console

fun main() {
    window.addEventListener("DOMContentLoaded", {
        onWasmReady(::runApp)
    })
}

internal fun runApp() {
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    mark(canvas, "canvas")
    canvas.setAttribute("tabindex", "0")
    mark(canvas, "before-layer")
    val skiaLayer = SkiaLayer()
    mark(canvas, "before-attach")
    skiaLayer.attachTo(canvas)
    mark(canvas, "before-skottie")
    configureSkottieLayer(skiaLayer)
    mark(canvas, "before-render")
    skiaLayer.needRender()
    mark(canvas, "ready")
    canvas.setAttribute("data-skiko-ready", "true")
}

private fun mark(canvas: HTMLCanvasElement, stage: String) {
    canvas.setAttribute("data-skiko-stage", stage)
    console.log("skiko-stage: $stage")
}
