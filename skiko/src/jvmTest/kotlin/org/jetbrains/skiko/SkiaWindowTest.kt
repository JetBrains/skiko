package org.jetbrains.skiko

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.jetbrains.skiko.util.swingTest
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.random.Random
import kotlin.test.assertTrue

@Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
class SkiaWindowTest {
    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)

    private fun paragraph(size: Float, text: String) =
        ParagraphBuilder(ParagraphStyle(), fontCollection)
            .pushStyle(
                TextStyle().apply {
                    color = Color.RED.rgb
                    fontSize = size
                }
            )
            .addText(text)
            .popStyle()
            .build()

    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun `render single window`() = swingTest {
        val window = SkiaWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val renderer = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderer = renderer
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            renderer.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2")
        } finally {
            window.close()
        }
    }

    @Test
    fun `render single window before window show`() = swingTest {
        val window = SkiaWindow()
        try {
            window.setLocation(200, 200)
            window.preferredSize = Dimension(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val renderer = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderer = renderer
            window.isUndecorated = true
            window.pack()
            window.paint(window.graphics)
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            renderer.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2")
        } finally {
            window.close()
        }
    }

    @Test
    fun `resize window`() = swingTest {
        val window = SkiaWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(40, 20)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.renderer = RectRenderer(window.layer, 20, 10, Color.RED)
            window.isUndecorated = true
            window.isVisible = true
            delay(1000)

            window.setSize(80, 40)
            delay(1000)

            screenshots.assert(window.bounds)
        } finally {
            window.close()
        }
    }

    @Test
    fun `render three windows`() = swingTest {
        fun window(color: Color) = SkiaWindow().apply {
            setLocation(200,200)
            setSize(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderer = RectRenderer(layer, 200, 100, color)
            isUndecorated = true
            isVisible = true
        }

        val window1 = window(Color.RED)
        val window2 = window(Color.GREEN)
        val window3 = window(Color.BLACK)

        try {
            delay(1000)

            window1.toFront()
            delay(1000)
            screenshots.assert(window1.bounds, "window1")

            window2.toFront()
            delay(1000)
            screenshots.assert(window2.bounds, "window2")

            window3.toFront()
            delay(1000)
            screenshots.assert(window3.bounds, "window3")
        } finally {
            window1.close()
            window2.close()
            window3.close()
        }
    }

    @Test
    fun `should call onRender after init, after resize, and only once after needRedraw`() = swingTest {
        var renderCount = 0

        val window = SkiaWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(40, 20)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.renderer = object : SkiaRenderer {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    renderCount++
                }
            }
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            assertTrue(renderCount > 0)
            renderCount = 0

            window.setSize(50, 20)
            delay(1000)
            assertTrue(renderCount > 0)
            renderCount = 0

            window.layer.needRedraw()
            delay(1000)
            assertEquals(1, renderCount)
        } finally {
            window.close()
        }
    }

    @Test(timeout = 60000)
    fun `stress test - open multiple windows`() = swingTest {
        fun window(isAnimated: Boolean) = SkiaWindow().apply {
            setLocation(200,200)
            setSize(40, 20)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderer = if (isAnimated) {
                AnimatedBoxRenderer(layer, pixelsPerSecond = 20.0, size = 20.0)
            } else {
                RectRenderer(layer, 20, 10, Color.RED)
            }
            isUndecorated = true
            isVisible = true
        }

        val random = Random(31415926)
        val openedWindows = mutableListOf<SkiaWindow>()

        repeat(10) {
            val needOpen = random.nextDouble() > 0.5f

            repeat(10) {
                if (needOpen) {
                    val window = window(isAnimated = random.nextDouble() > 0.5f)
                    openedWindows.add(window)
                } else if (openedWindows.size > 0) {
                    val index = (random.nextDouble() * (openedWindows.size - 1)).toInt()
                    openedWindows.removeAt(index).close()
                }
            }

            val delayCount = random.nextLong(5)
            if (delayCount > 0) {
                delay(delayCount * 10)
            }
        }

        openedWindows.forEach(JFrame::close)

        delay(5000)
    }

    @Test(timeout = 60000)
    fun `stress test - resize and paint immediately`() = swingTest {
        fun openWindow() = SkiaWindow(
            properties = SkiaLayerProperties(isVsyncEnabled = false)
        ).apply {
            setLocation(200,200)
            setSize(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderer = AnimatedBoxRenderer(layer, pixelsPerSecond = 20.0, size = 20.0)
            isVisible = true
        }

        val window = openWindow()

        repeat(100) {
            window.size = Dimension(200 + Random.nextInt(200), 200 + Random.nextInt(200))
            window.paint(window.graphics)
            yield()
        }

        window.close()
    }

    @Test(timeout = 60000)
    fun `stress test - open and paint immediately`() = swingTest {
        fun openWindow() = SkiaWindow(
            properties = SkiaLayerProperties(isVsyncEnabled = false)
        ).apply {
            setLocation(200,200)
            setSize(400, 200)
            preferredSize = Dimension(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderer = object : SkiaRenderer {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                }
            }
        }

        repeat(30) {
            delay(100)
            val window = openWindow()
            window.isVisible = true
            window.layer.needRedraw()
            yield()
            window.paint(window.graphics)
            window.close()
        }
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on init context`() = swingTest {
        testFallbackToSoftware(
            object : RenderFactory {
                override fun createContextHandler(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi
                ) = object : ContextHandler(layer) {
                    override fun initContext() = false
                    override fun initCanvas() = Unit
                }

                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    properties: SkiaLayerProperties
                ): Redrawer = object : Redrawer {
                    override fun dispose() = Unit
                    override fun needRedraw() = Unit
                    override fun redrawImmediately() = layer.inDrawScope { layer.draw() }
                }
            }
        )
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on create redrawer`() = swingTest {
        testFallbackToSoftware(
            object : RenderFactory {
                override fun createContextHandler(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi
                ) = object : ContextHandler(layer) {
                    override fun initContext() = true
                    override fun initCanvas() = Unit
                }

                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    properties: SkiaLayerProperties
                ) =  throw RenderException()
            }
        )
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on draw`() = swingTest {
        testFallbackToSoftware(
            object : RenderFactory {
                override fun createContextHandler(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi
                ) = object : ContextHandler(layer) {
                    override fun initContext() = true
                    override fun initCanvas() = Unit
                }

                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    properties: SkiaLayerProperties
                ) = object : Redrawer {
                    override fun dispose() = Unit
                    override fun needRedraw() = Unit
                    override fun redrawImmediately() = layer.inDrawScope {
                        throw RenderException()
                    }
                }
            }
        )
    }

    private suspend fun testFallbackToSoftware(nonSoftwareRenderFactory: RenderFactory) {
        val window = SkiaWindow(
            layerFactory = {
                SkiaLayer(
                    renderFactory = OverrideNonSoftwareRenderFactory(nonSoftwareRenderFactory)
                )
            }
        )
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val renderer = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderer = renderer
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1", "testFallbackToSoftware")

            renderer.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2", "testFallbackToSoftware")

            assertEquals(GraphicsApi.SOFTWARE, window.layer.renderApi)
        } finally {
            window.close()
        }
    }

    private class OverrideNonSoftwareRenderFactory(
        private val nonSoftwareRenderFactory: RenderFactory
    ) : RenderFactory {
        override fun createContextHandler(layer: SkiaLayer, renderApi: GraphicsApi): ContextHandler {
            return if (renderApi == GraphicsApi.SOFTWARE) {
                RenderFactory.Default.createContextHandler(layer, renderApi)
            } else {
                nonSoftwareRenderFactory.createContextHandler(layer, renderApi)
            }
        }

        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer {
            return if (renderApi == GraphicsApi.SOFTWARE) {
                RenderFactory.Default.createRedrawer(layer, renderApi, properties)
            } else {
                nonSoftwareRenderFactory.createRedrawer(layer, renderApi, properties)
            }
        }
    }

    @Test(timeout = 20000)
    fun `render continuously empty content without vsync`() = swingTest {
        val targetDrawCount = 500
        var drawCount = 0
        val onDrawCompleted = CompletableDeferred<Unit>()

        val window = SkiaWindow(
            properties = SkiaLayerProperties(
                isVsyncEnabled = false
            )
        )

        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.renderer = object : SkiaRenderer {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    drawCount++

                    if (drawCount < targetDrawCount) {
                        window.layer.needRedraw()
                    } else {
                        onDrawCompleted.complete(Unit)
                    }
                }
            }
            window.isUndecorated = true
            window.isVisible = true

            onDrawCompleted.await()
        } finally {
            window.close()
        }
    }

    @Test
    fun `render text (Windows)`() {
        testRenderText(OS.Windows)
    }

    @Test
    fun `render text (Linux)`() {
        testRenderText(OS.Linux)
    }

    @Test
    fun `render text (MacOS)`() {
        testRenderText(OS.MacOS)
    }

    private fun testRenderText(os: OS) = swingTest {
        assumeTrue(hostOs == os)

        val window = SkiaWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val paragraph by lazy { paragraph(window.layer.contentScale * 40, "=-+Нп") }

            window.layer.renderer = object : SkiaRenderer {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    paragraph.layout(Float.POSITIVE_INFINITY)
                    paragraph.paint(canvas, 0f, 0f)
                }
            }

            window.isUndecorated = true
            window.isVisible = true
            delay(1000)

            // check the line metrics
            val lineMetrics = paragraph.lineMetrics
            assertTrue(lineMetrics.isNotEmpty())
            assertEquals(0, lineMetrics.first().startIndex)
            assertEquals(5, lineMetrics.first().endIndex)
            assertEquals(5, lineMetrics.first().endExcludingWhitespaces)
            assertEquals(5, lineMetrics.first().endIncludingNewline)
            assertEquals(true, lineMetrics.first().isHardBreak)
            assertEquals(0, lineMetrics.first().lineNumber)

            screenshots.assert(window.bounds)
        } finally {
            window.close()
        }
    }

    private class RectRenderer(
        private val layer: SkiaLayer,
        var rectWidth: Int,
        var rectHeight: Int,
        private val rectColor: Color
    ) : SkiaRenderer {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            val dpi = layer.contentScale
            canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), Paint().apply {
                color = Color.WHITE.rgb
            })
            canvas.drawRect(Rect(0f, 0f, rectWidth * dpi, rectHeight * dpi), Paint().apply {
                color = rectColor.rgb
            })
        }
    }

    private class AnimatedBoxRenderer(
        private val layer: SkiaLayer,
        private val pixelsPerSecond: Double,
        private val size: Double
    ) : SkiaRenderer {
        private var oldNanoTime = Long.MAX_VALUE
        private var x = 0.0

        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.clear(Color.WHITE.rgb)

            val dt = (nanoTime - oldNanoTime).coerceAtLeast(0) / 1E9
            oldNanoTime = nanoTime

            x += dt * pixelsPerSecond
            if (x - size > width) {
                x = 0.0
            }

            canvas.drawRect(Rect(x.toFloat(), 0f, x.toFloat() + size.toFloat(), size.toFloat()), Paint().apply {
                color = Color.RED.rgb
            })

            layer.needRedraw()
        }
    }
}

private fun JFrame.close() = dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
