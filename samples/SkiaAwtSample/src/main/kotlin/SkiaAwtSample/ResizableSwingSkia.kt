package SkiaAwtSample

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.FPSCounter
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*

fun swingSkiaResize() = SwingUtilities.invokeLater {
    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = "Swing Window with Compose"

    var skiaPanel: SkiaSwingPanel? = null
    val fpsCounter = FPSCounter(logOnTick = true)

    val skikoView = object : ClocksAwt({ skiaPanel!!.graphicsConfiguration.defaultTransform.scaleX.toFloat() }) {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            fpsCounter.tick()
            super.onRender(canvas, width, height, nanoTime)
            skiaPanel!!.repaint()
        }
    }

    skiaPanel = SkiaSwingPanel(skikoView)
    skiaPanel.addMouseMotionListener(skikoView)

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