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
    logSkikoStage(stage)
}

@JsFun("(stage) => console.log('skiko-stage: ' + stage)")
private external fun logSkikoStage(stage: String)
