package SkijaInjectSample

import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skija.PaintMode
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

fun checkAliasing() = SwingUtilities.invokeLater {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

    //window.setJMenuBar(JMenuBar())

    window.layer.renderer = object : SkiaRenderer {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            displayAliasingGrid(canvas, width, height)
        }
    }

    window.setSize(800, 600)
    window.isVisible = true
}

/**
 * Draw grid. If grid looks wrong (some lines are invisible or have wrong thickness) then there are scaling issues
 */
fun displayAliasingGrid(canvas: Canvas, width: Int, height: Int) {
    // add 0.5f because it is center of 1px line (skia draws lines like that)
    fun drawPixelPerfectLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        val paint = Paint().setColor(0xFF000000.toInt()).setMode(PaintMode.STROKE).setAntiAlias(false).setStrokeWidth(1f)
        canvas.drawLine(
            0.5f + x1.toFloat(),
            0.5f + y1.toFloat(),
            0.5f + x2.toFloat(),
            0.5f + y2.toFloat(),
            paint
        )
    }

    for (y in 0..height step 3) {
        drawPixelPerfectLine(0, y, width, y)
    }

    for (x in 0..width step 3) {
        drawPixelPerfectLine(x, 0, x, height)
    }

    drawPixelPerfectLine(0, 0, width, 0)
    drawPixelPerfectLine(width, 0, width, height)
    drawPixelPerfectLine(width, height, 0, height)
    drawPixelPerfectLine(0, height, 0, 0)

    drawPixelPerfectLine(1, 1, width - 1, 1)
    drawPixelPerfectLine(width - 1, 1, width - 1, height - 1)
    drawPixelPerfectLine(width - 1, height - 1, 1, height - 1)
    drawPixelPerfectLine(1, height - 1, 1, 1)
}