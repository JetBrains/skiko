package org.jetbrains.skiko

import kotlinx.coroutines.delay
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.swing.SkiaSwingLayer
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.jetbrains.skiko.util.uiTest
import org.junit.Rule
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.WindowConstants
import kotlin.test.Test

@OptIn(ExperimentalSkikoApi::class)
class SkiaSwingLayerTest {

    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun `overlapped popup`() = uiTest {
        val skikoView = fillSkikoView()
        val skiaLayer = SkiaSwingLayer(skikoView, SkiaLayerProperties().copy(renderApi = renderApi)).apply {
            setBounds(0, 0, 200, 200)
        }

        val window = object : JFrame() {
            override fun dispose() {
                skiaLayer.dispose()
                super.dispose()
            }
        }
        try {
            val popup = object : JComponent() {
                init {
                    isOpaque = false
                    setBounds(50, 50, 50, 50)
                }

                override fun paintComponent(g: Graphics?) {
                    val scratchGraphics = g?.create() as? Graphics2D ?: return
                    try {
                        scratchGraphics.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                        );
                        scratchGraphics.color = Color.GREEN
                        scratchGraphics.fillRoundRect(5, 5, 40, 40, 16, 16)
                    } finally {
                        scratchGraphics.dispose()
                    }
                }
            }

            val layeredPane = JLayeredPane()
            layeredPane.add(popup, Integer.valueOf(1))
            layeredPane.add(skiaLayer, Integer.valueOf(2))

            window.isUndecorated = true
            window.size = Dimension(400, 400)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.contentPane.add(layeredPane)
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds)
        } finally {
            window.close()
        }
    }

    private fun fillSkikoView(color: Color = Color.RED): SkikoView = object : SkikoView {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), Paint().apply {
                this.color = color.rgb
            })
        }
    }
}