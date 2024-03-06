package org.jetbrains.skiko

import kotlinx.coroutines.delay
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.swing.SkiaSwingLayer
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.jetbrains.skiko.util.uiTest
import org.junit.Rule
import java.awt.*
import javax.swing.*
import javax.swing.JLayeredPane.DEFAULT_LAYER
import javax.swing.JLayeredPane.POPUP_LAYER
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

        singleWindowTest(skiaLayer) { contentPane ->
            val popup = object : JComponent() {
                init {
                    isOpaque = false
                    setBounds(50, 50, 50, 50)
                }

                override fun paintComponent(g: Graphics?) {
                    val scratchGraphics = g?.create() as? Graphics2D ?: return
                    try {
                        scratchGraphics.color = Color(0, 0, 0, 50)
                        scratchGraphics.fillRect(0, 0, 50, 50)

                        scratchGraphics.color = Color.GREEN
                        scratchGraphics.fillRoundRect(5, 5, 40, 40, 16, 16)
                    } finally {
                        scratchGraphics.dispose()
                    }
                }
            }

            val layeredPane = JLayeredPane()
            layeredPane.add(skiaLayer)
            layeredPane.add(popup)

            layeredPane.setLayer(skiaLayer, DEFAULT_LAYER)
            layeredPane.setLayer(popup, POPUP_LAYER)

            contentPane.add(layeredPane)

            delay(1000)
            checkScreenshot()
        }
    }

    @Test
    fun `multiple layers`() = uiTest {
        val skikoView1 = fillSkikoView(color = Color.CYAN)
        val skiaLayer1 = SkiaSwingLayer(skikoView1, SkiaLayerProperties().copy(renderApi = renderApi)).apply {
            setBounds(0, 0, 200, 200)
        }
        val skikoView2 = fillSkikoView(color = Color.GREEN)
        val skiaLayer2 = SkiaSwingLayer(skikoView2, SkiaLayerProperties().copy(renderApi = renderApi)).apply {
            setBounds(50, 50, 200, 200)
        }

        val skikoView3 = fillSkikoView(color = Color.RED)
        val skiaLayer3 = SkiaSwingLayer(skikoView3, SkiaLayerProperties().copy(renderApi = renderApi)).apply {
            setBounds(100, 100, 25, 25)
        }

        singleWindowTest(
            dispose = {
                skiaLayer1.dispose()
                skiaLayer2.dispose()
                skiaLayer3.dispose()
            }
        ) { contentPane ->
            val layeredPane = JLayeredPane()

            layeredPane.add(skiaLayer1)
            layeredPane.add(skiaLayer2)
            layeredPane.add(skiaLayer3)

            layeredPane.setLayer(skiaLayer1, 0)
            layeredPane.setLayer(skiaLayer2, 1)
            layeredPane.setLayer(skiaLayer3, 2)

            contentPane.add(layeredPane)

            delay(1000)
            checkScreenshot()
        }
    }

    @Test
    fun `layer resize`() = uiTest {
        val skikoView = fillSkikoView()
        val skiaLayer = SkiaSwingLayer(skikoView, SkiaLayerProperties().copy(renderApi = renderApi))

        singleWindowTest(skiaLayer) { contentPane ->
            val splitPane = JSplitPane()
            splitPane.leftComponent = skiaLayer
            splitPane.rightComponent = JPanel()

            splitPane.dividerLocation = 50

            contentPane.add(splitPane)

            delay(1000)
            checkScreenshot("position1")

            splitPane.dividerLocation = 100
            delay(1000)
            checkScreenshot("position2")
        }
    }

    @Test
    fun `overlapped skia layer`() = uiTest {
        val skikoView = object : SkikoView {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                val rect = Rect(0f, 0f, width.toFloat() - 10f, height.toFloat() - 10f)
                canvas.drawRect(rect, Paint().apply {
                    this.color = Color.CYAN.rgb
                })

                canvas.drawRectShadow(
                    rect,
                    dx = 3f, dy = 3f,
                    blur = 0.5f,
                    Color(0, 0, 0, 50).rgb
                )
            }
        }
        val skiaLayer = SkiaSwingLayer(skikoView, SkiaLayerProperties().copy(renderApi = renderApi)).apply {
            setBounds(50, 50, 50, 50)
        }

        singleWindowTest(skiaLayer) { contentPane ->
            val panel = JPanel().apply {
                background = Color.GREEN
                setBounds(0, 0, 200, 200)
            }

            val layeredPane = JLayeredPane()
            layeredPane.add(panel)
            layeredPane.add(skiaLayer)

            layeredPane.setLayer(panel, DEFAULT_LAYER)
            layeredPane.setLayer(skiaLayer, POPUP_LAYER)

            contentPane.add(layeredPane)

            delay(1000)
            checkScreenshot()
        }
    }

    private inline fun singleWindowTest(
        layer: SkiaSwingLayer,
        block: SingleWindowTestScope.(contentPane: Container) -> Unit
    ) {
        singleWindowTest(
            dispose = {
                layer.dispose()
            },
            block
        )
    }

    private inline fun singleWindowTest(
        crossinline dispose: () -> Unit,
        block: SingleWindowTestScope.(contentPane: Container) -> Unit
    ) {
        val window = object : JFrame() {
            override fun dispose() {
                dispose()
                super.dispose()
            }
        }

        try {
            window.isUndecorated = true
            window.size = Dimension(400, 400)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.isVisible = true
            val scope = object : SingleWindowTestScope {
                override fun checkScreenshot(id: String) {
                    screenshots.assert(window.bounds, id)
                }
            }
            scope.block(window.contentPane)
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

    private interface SingleWindowTestScope {
        fun checkScreenshot(id: String = "")
    }
}