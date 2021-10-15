package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.GenericRenderer
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

private class DemoApp: SkiaRenderer {
    private val paint = Paint()

    override fun onRender(canvas: Canvas, width: Int, height: Int, currentTimestamp: Long) {
        canvas.drawCircle(200f, 50f, 25f, paint)
        canvas.drawLine(100f, 100f, 200f, 200f, paint)

        canvas.drawRect(Rect(10f, 20f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
    }
}

fun main() {
    onWasmReady {
        for (index in 1 .. 3) {
            val skiaLayer = SkiaLayer()
            val canvas = document.getElementById("c$index") as HTMLCanvasElement
            val app = if (index == 3) {
                DemoApp()
            } else {
                BouncingBalls()
            }
            skiaLayer.renderer = GenericRenderer(skiaLayer, app)
            skiaLayer.setCanvas(canvas)
            skiaLayer.draw()
        }
    }
}
