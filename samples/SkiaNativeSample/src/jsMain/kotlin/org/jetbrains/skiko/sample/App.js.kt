package org.jetbrains.skiko.sample

import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.wasm.api.CanvasRenderer
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement

private data class Circle(var x: Float, var y: Float, var r: Float)

private fun Canvas.draw(circle: Circle, paint: Paint) {
    drawCircle(circle.x, circle.y, circle.r, paint)
}

private class DemoApp(htmlCanvas: HTMLCanvasElement) : CanvasRenderer(htmlCanvas) {
    private val paint = Paint()

    override fun drawFrame(currentTimestamp: Double) {
        canvas.draw(Circle(200f, 50f, 25f), paint)
        canvas.drawLine(100f, 100f, 200f, 200f, paint)

        canvas.drawRect(Rect(10f, 20f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
    }
}

fun main() {
    onWasmReady {
        val syncNow = window.performance.now()
        DemoApp(document.getElementById("a") as HTMLCanvasElement).draw()
    }
}

/*
fun createWindow() {
    val layer = SkiaLayer()
    layer.renderer = GenericRenderer(layer) {
            canvas, w, h, nanoTime -> displayScene(canvas, nanoTime)
    }
    val window = SkiaWindow(layer)
    window.nsWindow.orderFrontRegardless()
} */
