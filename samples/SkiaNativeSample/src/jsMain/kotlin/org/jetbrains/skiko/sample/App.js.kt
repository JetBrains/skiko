package org.jetbrains.skiko.sample

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
        BouncingBallsApp(document.getElementById("c") as HTMLCanvasElement, syncNow).draw()
        BouncingBallsApp(document.getElementById("b") as HTMLCanvasElement, syncNow).draw()
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
