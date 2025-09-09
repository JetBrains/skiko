package org.jetbrains.skiko.sample.js

import kotlinx.browser.document
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.jetbrains.skiko.SkikoRenderDelegate
import org.w3c.dom.Element

private class DemoApp: SkikoRenderDelegate {
    private val paint = Paint()

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        canvas.drawCircle(200f, 50f, 25f, paint)
        canvas.drawLine(100f, 100f, 200f, 200f, paint)

        canvas.drawRect(Rect(10f, 20f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
    }
}

internal fun runApp() {
    for (index in 1 .. 3) {
        val skiaLayer = SkiaLayer()
        val canvas = findElementById("c$index")!!
        val app = if (index == 3) {
            DemoApp()
        } else {
            BouncingBalls()
        }
        skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, app)
        skiaLayer.attachTo(canvas)
        skiaLayer.needRedraw()
    }
}

fun findElementById(id: String): Element? = document.getElementById(id)