package org.jetbrains.skiko

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.util.uiTest
import org.junit.Test
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension

class WindowResizeTest {

    @Test(timeout = 1_000_000)
    fun `white borders on resize`() = uiTest {
        val window = UiTestWindow {
            size = Dimension(800, 800)
            layer.renderDelegate = SolidColorRenderer(layer, Color.BLACK, continuousRedraw = false)
//            layer.background = Color(0, 0, 0, 0)
//            layer.transparency = true
//            background = null
//            rootPane.background = null
//            contentPane.background = null
            contentPane.add(layer, BorderLayout.CENTER)
        }

        window.isVisible = true
//        window.contentPane.background = Color.RED
        delay(2000)

//        while (true) {
//            repeat(100) {
//                window.size = Dimension(805 + (it * 5), 800)
//                delay(32)
//            }
//
//            repeat(100) {
//                window.size = Dimension(1295 - (it * 5), 800)
//                delay(32)
//            }
//        }

        awaitCancellation()
    }

}


class SolidColorRenderer(
    val layer: SkiaLayer,
    color: Color,
    continuousRedraw: Boolean = false
) : SkikoRenderDelegate {

    var continuousRedraw = continuousRedraw
        set(value) {
            if (value)
                layer.needRedraw()
            field = value
        }

    val paint = Paint().also { it.color = color.rgb }

    var spread = 0
    var direction: Int = 1

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        Logger.debug { "Rendering at size $width x $height" }
        canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()).inflate(spread.toFloat()), paint)
        if (continuousRedraw) {
            if (spread == 0)
                direction = -1
            else if (spread == -100)
                direction = 1
            spread += direction
            layer.needRedraw()
        }
    }
}