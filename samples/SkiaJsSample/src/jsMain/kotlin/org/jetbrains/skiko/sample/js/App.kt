package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement


fun main(args: Array<String>) {
    onWasmReady {
        val paint = Paint()
        paint.setColor(200)

        val canvas = document.getElementById("c") as HTMLCanvasElement
        canvas.getContext("webgl")
        
    }
}