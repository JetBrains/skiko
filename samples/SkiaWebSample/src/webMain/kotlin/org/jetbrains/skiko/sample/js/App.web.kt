package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.jetbrains.skiko.SkikoRenderDelegate
import kotlinx.browser.document

private class DemoApp: SkikoRenderDelegate {
    private val paint = Paint()

    private val textPaint = Paint().apply {
        color = Color.RED
        setStroke(false)
    }

    private val typeface: Typeface? = FontMgr.default.matchFamilyStyle( "Roboto", FontStyle.NORMAL)

    private val font: Font? = typeface?.let { Font(it, 36f) }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        canvas.drawCircle(200f, 50f, 25f, paint)
        canvas.drawLine(100f, 100f, 200f, 200f, paint)

        canvas.drawRect(Rect(10f, 20f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)

        // https://youtrack.jetbrains.com/issue/CMP-7439
        font?.let { f ->
            TextLine.make("Hello, drawTextLine!", f).use { line ->
                canvas.drawTextLine(line, 10f, 300f, textPaint)
            }
        }
    }
}

internal fun runApp() {
    for (index in 1 .. 3) {
        val skiaLayer = SkiaLayer()
        val canvas = document.getElementById("c$index")!!
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