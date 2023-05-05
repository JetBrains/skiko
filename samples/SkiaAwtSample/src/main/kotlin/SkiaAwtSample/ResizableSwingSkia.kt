package SkiaAwtSample

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkikoView
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*

fun swingSkiaResize(
    isStatic: Boolean,
    offScreenRendering: Boolean
) = SwingUtilities.invokeLater {
    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = "Swing Window with Compose"

    val skiaPanel = SkiaPanel(offScreenRendering).apply {
        if (isStatic) {
            layer.addView(GenericSkikoView(layer, object : SkikoView {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    val paint = Paint().apply { color = Color.GRAY.rgb }
                    canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), paint)
                }
            }))
        } else {
            layer.addView(GenericSkikoView(layer, ClocksAwt(layer)))
        }
    }

    val leftPanel = JPanel().apply {
        background = Color.CYAN
    }
    val rightPanel = JPanel(BorderLayout()).apply {
        add(skiaPanel, BorderLayout.CENTER)
    }

    val splitter = JSplitPane(JSplitPane.HORIZONTAL_SPLIT).apply {
        isContinuousLayout = true
        leftComponent = leftPanel
        rightComponent = rightPanel
    }

    window.add(splitter)
    window.setSize(350, 200)
    window.setLocationRelativeTo(null)
    window.isVisible = true
}