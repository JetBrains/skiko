@file:OptIn(ExperimentalSkikoApi::class)

package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skia.*
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.context.JvmContextHandler
import org.jetbrains.skiko.redrawer.MetalRedrawer
import org.jetbrains.skiko.redrawer.MetalVSyncer
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.swing.SkiaSwingLayer
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.jetbrains.skiko.util.UiTestScope
import org.jetbrains.skiko.util.UiTestWindow
import org.jetbrains.skiko.util.uiTest
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.awt.*
import java.awt.Color
import java.awt.Point
import java.awt.event.*
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.Box
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration

@Suppress("SameParameterValue")
class SkiaLayerTest {
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

    @Ignore
    @Test
    fun `metal drawables not lost`() = uiTest {
        val window = UiTestWindow(
            properties = SkiaLayerProperties(
                isVsyncEnabled = true
            )
        )
        val colors = arrayOf(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW
        )

        var counter1 = 0
        var counter2 = 0
        val paint = Paint()

        try {
            window.setLocation(200, 200)
            window.setSize(400, 600)
            window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            window.layer.renderDelegate = object : SkikoRenderDelegate {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    val c1 = counter1
                    val c2 = counter2

                    paint.color = colors[c1.mod(colors.size)].rgb
                    canvas.drawRect(Rect(0f, 0f, width.toFloat(), height / 2f), paint)

                    paint.color = colors[c2.mod(colors.size)].rgb
                    canvas.drawRect(Rect(0f, height / 2f, width.toFloat(), height.toFloat()), paint)
                }
            }
            window.isVisible = true

            window.addKeyListener(object : KeyAdapter() {
                override fun keyTyped(e: KeyEvent?) {
                    launch {
                        val redrawer = window.layer.redrawer as MetalRedrawer
                        redrawer.redrawImmediately()
                        counter1 += 1
                        redrawer.redrawImmediately()
                        counter2 += 1
                        redrawer.redrawImmediately()
                    }
                }
            })

            window.addWindowListener(object : WindowAdapter() {
                override fun windowActivated(e: WindowEvent?) {
                    window.requestFocus()
                }
            })

            delay(Duration.INFINITE)
        } finally {
            window.close()
        }
    }

    @Test
    fun `should not leak native windows`() = uiTest {
        assumeTrue(hostOs.isMacOS)

        suspend fun createAndDisposeWindow() {
            val layer = SkiaLayer()
            val frame = JFrame()
            frame.contentPane.add(layer)
            frame.size = Dimension(200, 200)
            frame.isVisible = true
            delay(30)
            layer.dispose()
            frame.dispose()
        }

        // warm caches
        repeat(8) {
            createAndDisposeWindow()
        }

        delay(1000)
        val initialWindowCount = getApplicationWindowCount()

        repeat(32) {
            createAndDisposeWindow()
        }

        delay(1000)
        val actualWindowCount = getApplicationWindowCount()

        assertTrue(
            initialWindowCount >= actualWindowCount,
            "initialWindowCount=$initialWindowCount, actualWindowCount=$actualWindowCount"
        )
    }

    @Test
    fun `render single window`() = uiTest {
        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderDelegate = app
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            app.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2")

            assertRenderApiFor(window.layer)
        } finally {
            window.close()
        }
    }

    @Test
    fun `render single swing layer`() = uiTest {
        val window = JFrame()
        val app = RectRenderer(window, 200, 100, Color.RED)
        val layer = SkiaSwingLayer(
            app,
            properties = SkiaLayerProperties(renderApi = renderApi)
        )
        window.contentPane.add(layer)
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            layer.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            app.rectWidth = 100
            layer.repaint()
            delay(1000)
            screenshots.assert(window.bounds, "frame2")

            assertRenderApiFor(layer)
        } finally {
            window.close()
        }
    }

    @Test
    fun `render single window before window show`() = uiTest {
        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.preferredSize = Dimension(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderDelegate = app
            window.isUndecorated = true
            window.pack()
            window.paint(window.graphics)
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            app.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2")

            assertRenderApiFor(window.layer)
        } finally {
            window.close()
        }
    }

    @Test
    fun `render empty layer`() = uiTest {
        val window = JFrame()
        val layer = SkiaLayer(
            properties = SkiaLayerProperties(renderApi = renderApi)
        )
        var renderedWidth = -1
        layer.renderDelegate = object : SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                renderedWidth = width
            }
        }
        layer.size = Dimension(0, 0)
        val density = window.graphicsConfiguration.defaultTransform.scaleX
        try {
            val panel = JLayeredPane()
            panel.add(layer)
            window.contentPane.add(panel)
            window.setLocation(200, 200)
            window.size = Dimension(200, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.isUndecorated = true
            window.isVisible = true
            layer.needRedraw()
            delay(1000)
            assertEquals(0, renderedWidth)

            renderedWidth = -1
            layer.needRedraw()
            delay(1000)
            assertEquals(0, renderedWidth)

            renderedWidth = -1
            layer.size = Dimension(30, 40)
            delay(1000)
            assertEquals((30 * density).toInt(), renderedWidth)

            renderedWidth = -1
            layer.size = Dimension(0, 0)
            delay(1000)
            assertEquals(0, renderedWidth)

            renderedWidth = -1
            layer.size = Dimension(40, 40)
            delay(1000)
            assertEquals((40 * density).toInt(), renderedWidth)

            assertRenderApiFor(layer)
        } finally {
            layer.dispose()
            window.close()
        }
    }

    @Test
    fun `move without redrawing`() = uiTest {
        val window = JFrame()
        val layer = SkiaLayer(
            properties = SkiaLayerProperties(renderApi = renderApi)
        )

        layer.renderDelegate = object : SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), Paint().apply {
                    color = Color.RED.rgb
                })
            }
        }
        layer.size = Dimension(100, 100)
        val box = Box.createVerticalBox().apply {
            add(layer)
        }
        box.setBounds(0, 0, 100, 100)

        try {
            val panel = JLayeredPane()
            panel.add(box)
            window.contentPane.add(panel)
            window.setLocation(200, 200)
            window.size = Dimension(200, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            box.setBounds(100, 0, 100, 100)
            delay(1000)
            screenshots.assert(window.bounds, "frame2")

            assertRenderApiFor(layer)
        } finally {
            layer.dispose()
            window.close()
        }
    }

    @Test
    fun `resize window`() = uiTest {
        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(40, 20)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.renderDelegate = RectRenderer(window.layer, 20, 10, Color.RED)
            window.isUndecorated = true
            window.isVisible = true
            delay(1000)

            window.setSize(80, 40)
            delay(1000)

            screenshots.assert(window.bounds)

            assertRenderApiFor(window.layer)
        } finally {
            window.close()
        }
    }

    @Test
    fun `render three windows`() = uiTest {
        fun window(color: Color) = UiTestWindow().apply {
            setLocation(200, 200)
            setSize(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderDelegate = RectRenderer(layer, 200, 100, color)
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

            assertRenderApiFor(window1.layer)
            assertRenderApiFor(window2.layer)
            assertRenderApiFor(window3.layer)
        } finally {
            window1.close()
            window2.close()
            window3.close()
        }
    }

    @Test
    fun `window fullscreen state in componentResized`() = uiTest {
        val window = UiTestWindow()
        try {
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.fullscreen = true
            var stateRemainsFullscreen = true
            window.addComponentListener(object: ComponentAdapter(){
                override fun componentResized(e: ComponentEvent?) {
                    if (!window.layer.fullscreen)
                        stateRemainsFullscreen = false
                }
            })
            window.isVisible = true

            delay(1000)
            assertEquals(true, stateRemainsFullscreen)
            assertRenderApiFor(window.layer)
        } finally {
            window.close()

            // Delay before starting next test to let the window animation to complete, and allow the next window
            // to become fullscreen
            if (hostOs == OS.MacOS) {
                delay(1000)
            }
        }
    }

    @Test
    fun `should call onRender after init, after resize, and only once after needRedraw`() = uiTest {
        var renderCount = 0

        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(40, 20)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.renderDelegate = object : SkikoRenderDelegate {
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
    fun `stress test - open multiple windows`() = uiTest {
        fun window(isAnimated: Boolean) = UiTestWindow().apply {
            setLocation(200, 200)
            setSize(40, 20)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderDelegate = if (isAnimated) {
                AnimatedBoxRenderer(layer, pixelsPerSecond = 20.0, size = 20.0)
            } else {
                RectRenderer(layer, 20, 10, Color.RED)
            }
            isUndecorated = true
            isVisible = true
        }

        val random = Random(31415926)
        val openedWindows = mutableListOf<UiTestWindow>()

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

            openedWindows.forEach {
                assertRenderApiFor(it.layer)
            }
        }

        openedWindows.forEach(JFrame::close)

        delay(5000)
    }

    @Test(timeout = 60000)
    fun `stress test - resize and paint immediately`() = uiTest {
        fun openWindow() = UiTestWindow(
            properties = SkiaLayerProperties(isVsyncEnabled = false, isVsyncFramelimitFallbackEnabled = true)
        ).apply {
            setLocation(200, 200)
            setSize(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderDelegate = AnimatedBoxRenderer(layer, pixelsPerSecond = 20.0, size = 20.0)
            isVisible = true
        }

        val window = openWindow()

        repeat(100) {
            window.size = Dimension(200 + Random.nextInt(200), 200 + Random.nextInt(200))
            window.paint(window.graphics)
            assertRenderApiFor(window.layer)
            yield()
        }

        window.close()
    }

    @Test(timeout = 60000)
    fun `stress test - open and paint immediately`() = uiTest {
        fun openWindow() = UiTestWindow(
            properties = SkiaLayerProperties(isVsyncEnabled = false, isVsyncFramelimitFallbackEnabled = true)
        ).apply {
            setLocation(200, 200)
            setSize(400, 200)
            preferredSize = Dimension(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.renderDelegate = object : SkikoRenderDelegate {
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
            assertRenderApiFor(window.layer)
            window.close()
        }
    }

    private abstract class BaseTestRedrawer(val layer: SkiaLayer): Redrawer {
        private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
            redrawImmediately()
        }
        override fun dispose() = Unit
        override fun needRedraw(throttledToVsync: Boolean) = frameDispatcher.scheduleFrame()
        override fun redrawImmediately() = Unit
        override fun update(nanoTime: Long) = layer.update(nanoTime)

        override val renderInfo: String
            get() = ""
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on init context`() = uiTest {
        testFallbackToSoftware { layer, _, _, _ ->
            object : BaseTestRedrawer(layer) {
                private val contextHandler = object : JvmContextHandler(layer) {
                    override fun initContext() = false
                    override fun initCanvas() = Unit
                }
                override fun redrawImmediately() = layer.inDrawScope(contextHandler::draw)
            }
        }
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on create redrawer`() = uiTest {
        testFallbackToSoftware { _, _, _, _ -> throw RenderException() }
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on draw`() = uiTest {
        testFallbackToSoftware { layer, _, _, _ ->
            object : BaseTestRedrawer(layer) {
                override fun redrawImmediately() = layer.inDrawScope {
                    throw RenderException()
                }
            }
        }
    }

    private suspend fun UiTestScope.testFallbackToSoftware(nonSoftwareRenderFactory: RenderFactory) {
        val window = UiTestWindow(
            renderFactory = OverrideNonSoftwareRenderFactory(nonSoftwareRenderFactory)
        )
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderDelegate = app
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1", "testFallbackToSoftware")

            app.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2", "testFallbackToSoftware")

            assertEquals(GraphicsApi.SOFTWARE_COMPAT, window.layer.renderApi)
        } finally {
            window.close()
        }
    }

    private class OverrideNonSoftwareRenderFactory(
        private val nonSoftwareRenderFactory: RenderFactory
    ) : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            analytics: SkiaLayerAnalytics,
            properties: SkiaLayerProperties
        ): Redrawer {
            return if (renderApi == GraphicsApi.SOFTWARE_COMPAT) {
                RenderFactory.Default.createRedrawer(layer, renderApi, analytics, properties)
            } else {
                nonSoftwareRenderFactory.createRedrawer(layer, renderApi, analytics, properties)
            }
        }
    }

    @Test(timeout = 60000)
    fun `renderApi change callback is invoked on fallback`() = uiTest {
        val window = UiTestWindow(
            renderFactory = OverrideNonSoftwareRenderFactory { layer, _, _, _ ->
                object : BaseTestRedrawer(layer) {
                    override fun redrawImmediately() = layer.inDrawScope {
                        throw RenderException()
                    }
                }
            }
        )
        try {
            var rendererChangedCallbackInvoked = false
            window.layer.onStateChanged(SkiaLayer.PropertyKind.Renderer) {
                rendererChangedCallbackInvoked = true
            }
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.isVisible = true

            delay(1000)

            assertEquals(GraphicsApi.SOFTWARE_COMPAT, window.layer.renderApi)
            assertTrue(rendererChangedCallbackInvoked)
        } finally {
            window.close()
        }
    }

    @Test(timeout = 20000)
    fun `render continuously empty content without vsync`() = uiTest {
        val targetDrawCount = 500
        var drawCount = 0
        val onDrawCompleted = CompletableDeferred<Unit>()

        val window = UiTestWindow(
            properties = SkiaLayerProperties(
                isVsyncEnabled = false,
                isVsyncFramelimitFallbackEnabled = true
            )
        )

        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.renderDelegate = object : SkikoRenderDelegate {
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
            assertRenderApiFor(window.layer)
        } finally {
            window.close()
        }
    }

    @Test
    fun `frame disposed inside of immediate repaint`() = uiTest {
        val onDrawCompleted = CompletableDeferred<Unit>()
        val window = UiTestWindow(
            properties = SkiaLayerProperties(
                isVsyncEnabled = true,
                isVsyncFramelimitFallbackEnabled = true
            )
        )
        try {
            window.setLocation(200, 200)
            window.setSize(400, 400)
            window.layer.renderDelegate = object : SkikoRenderDelegate {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    window.dispose()
                    SwingUtilities.invokeLater {
                        onDrawCompleted.complete(Unit)
                    }
                }
            }
            window.isVisible = true
            onDrawCompleted.await()
        } finally {
            window.dispose()
        }
    }

    @Test
    fun `hiding parent stops drawing layer`() = uiTest {
        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderDelegate = app
            window.isUndecorated = true
            window.isVisible = true

            // Force the layered pane to draw itself with a specific color so that the test doesn't depend on the
            // default window background, which could be different on different platforms.
            window.layeredPane.background = Color.BLUE
            window.layeredPane.isOpaque = true

            delay(1000)
            screenshots.assert(window.bounds, "visible_parent")

            window.contentPane.isVisible = false

            delay(1000)
            screenshots.assert(window.bounds, "hidden_parent")
        } finally {
            window.close()
        }

    }

    @Test
    fun `non zero layer origin`() = uiTest {
        val window = UiTestWindow(setupContent = {
            isUndecorated = true
            setLocation(200, 200)
            setSize(300, 100)

            val panel = JPanel()
            panel.preferredSize = Dimension(100, 100)
            panel.background = Color.GREEN
            contentPane.add(panel, BorderLayout.WEST)

            layer.renderDelegate = RectRenderer(layer, 100, 100, Color.RED)
            contentPane.add(layer, BorderLayout.CENTER)
        })
        try {
            window.isUndecorated = true
            window.isVisible = true
            delay(1000)
            screenshots.assert(window.bounds, "frame")
        } finally {
            window.close()
        }
    }

    @Test
    fun `second frame drawn without delay in metal`() = uiTest(
        // SOFTWARE_COMPAT fails because it's just too slow
        excludeRenderApis = listOf(GraphicsApi.SOFTWARE_COMPAT)
    ) {
        assumeTrue(hostOs == OS.MacOS) // since the test has 'metal' in its name (it is flaky on Windows)

        val renderTimes = mutableListOf<Long>()
        val renderer = object: SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                renderTimes.add(System.currentTimeMillis())
            }
        }
        val window = UiTestWindow {
            layer.renderDelegate = renderer
            contentPane.add(layer, BorderLayout.CENTER)
        }
        try {
            window.size = Dimension(800, 800)
            repeat(10) {
                window.isVisible = true
                delay(16)
                window.layer.needRedraw()
                delay(500)
                window.isVisible = false

                val dt = renderTimes.last() - renderTimes.first()
                assertTrue(
                    actual = dt < 150,
                    message = "2nd frame drawn ${dt}ms after 1st: ${renderTimes.map { it - renderTimes.first() }}}"
                )
                renderTimes.clear()
            }
        } finally {
            window.dispose()
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

    @Test
    fun analytics() = uiTest {
        val analytics = object : SkiaLayerAnalytics {
            val rendererInfo = object {
                var skikoVersion: String? = null
                var os: OS? = null
                var api: GraphicsApi? = null

                var init = 0
                var deviceChosen = 0
            }

            val deviceInfo = object {
                var skikoVersion: String? = null
                var os: OS? = null
                var api: GraphicsApi? = null
                var deviceName: String? = null

                var init = 0
                var contextInit = 0
                var beforeFirstFrameRender = 0
                var afterFirstFrameRender = 0
            }

            @ExperimentalSkikoApi
            override fun renderer(
                skikoVersion: String,
                os: OS,
                api: GraphicsApi
            ) = object : SkiaLayerAnalytics.RendererAnalytics {
                init {
                    rendererInfo.skikoVersion = skikoVersion
                    rendererInfo.os = os
                    rendererInfo.api = api
                    rendererInfo.init = 0
                    rendererInfo.deviceChosen = 0
                }

                override fun init() {
                    rendererInfo.init++
                }

                override fun deviceChosen() {
                    rendererInfo.deviceChosen++
                }
            }

            @ExperimentalSkikoApi
            override fun device(
                skikoVersion: String,
                os: OS,
                api: GraphicsApi,
                deviceName: String?
            ) = object : SkiaLayerAnalytics.DeviceAnalytics {
                init {
                    deviceInfo.skikoVersion = skikoVersion
                    deviceInfo.os = os
                    deviceInfo.api = api
                    deviceInfo.deviceName = deviceName

                    deviceInfo.init = 0
                    deviceInfo.contextInit = 0
                    deviceInfo.beforeFirstFrameRender = 0
                    deviceInfo.afterFirstFrameRender = 0
                }

                override fun init() {
                    deviceInfo.init++
                }

                override fun contextInit() {
                    deviceInfo.contextInit++
                }

                override fun beforeFirstFrameRender() {
                    deviceInfo.beforeFirstFrameRender++
                }

                override fun afterFirstFrameRender() {
                    deviceInfo.afterFirstFrameRender++
                }
            }
        }

        val window = UiTestWindow(analytics = analytics)
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.renderDelegate = app
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            assertEquals(Version.skiko, analytics.rendererInfo.skikoVersion)
            assertEquals(hostOs, analytics.rendererInfo.os)
            assertNotNull(analytics.rendererInfo.api)
            assertEquals(1, analytics.rendererInfo.init)
            assertEquals(1, analytics.rendererInfo.deviceChosen)

            assertEquals(Version.skiko, analytics.deviceInfo.skikoVersion)
            assertNotNull(analytics.deviceInfo.api)
            assertNotNull(analytics.deviceInfo.deviceName)
            assertEquals(1, analytics.deviceInfo.init)
            assertEquals(1, analytics.deviceInfo.contextInit)
            assertEquals(1, analytics.deviceInfo.beforeFirstFrameRender)
            assertEquals(1, analytics.deviceInfo.afterFirstFrameRender)
        } finally {
            window.close()
        }
    }

    private fun testRenderText(os: OS) = uiTest {
        assumeTrue(hostOs == os)

        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val paragraph by lazy { paragraph(window.layer.contentScale * 40, "=-+Нп") }

            window.layer.renderDelegate = object : SkikoRenderDelegate {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    canvas.clear(Color.WHITE.rgb)
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
            assertRenderApiFor(window.layer)
        } finally {
            window.close()
        }
    }

    @Test
    fun `content not relaid out on window move`() = uiTest {
        var layoutCount = 0

        val window = UiTestWindow {
            contentPane.layout = object: BorderLayout() {
                override fun layoutContainer(parent: Container?) {
                    super.layoutContainer(parent)
                    layoutCount++
                }
            }
            contentPane.add(layer)
        }
        try {
            window.size = Dimension(400, 400)
            window.isVisible = true

            repeat(20) {
                window.location = window.location.let {
                    Point(it.x + 10, it.y + 10)
                }
                delay(50)
            }

            // Ideally, layoutCount would be just 1, but Swing appears to call layout one extra time, so it ends up being 2.
            // Compare to 3 just to avoid a false-failure if there's another layout for whatever reason.
            // What we're interested to validate is that there's no layout occurring on every window move.
            assert(layoutCount <= 3) {
                "Layout count: $layoutCount"
            }
        } finally {
            window.dispose()
        }
    }

    @Test
    fun `no window flash on hide or dispose while animation is running`() = uiTest {
        assumeTrue(hostOs.isMacOS)
        // Until the issue is fixed in other redrawers
        // Don't use assumeTrue, as uiTest iterates over multiple renderers,
        // and if one of them skipped, the whole test is skipped
        if (renderApi != GraphicsApi.METAL) return@uiTest

        // Put up a large green window, and then repeatedly show and hide/dispose
        // a smaller black window on top of it while screenshotting the pixel at the center,
        // and making sure that pixel is always either black or green.

        // We can't compare colors exactly because java.awt.Robot can return a slightly different color due to
        // system color profile
        fun Color.closeTo(other: Color): Boolean {
            val diffLimit = 10
            return (red - other.red).absoluteValue < diffLimit
                    && (green - other.green).absoluteValue < diffLimit
                    && (blue - other.blue).absoluteValue < diffLimit
        }

        val bgColor = Color.GREEN
        val fgColor = Color.BLACK
        val backgroundWindow = JFrame().also {
            it.size = Dimension(1000, 1000)
            it.location = Point(200, 200)
            it.contentPane.background = bgColor
            it.isVisible = true
        }

        lateinit var renderDelegate: SolidColorRenderer
        val window = UiTestWindow {
            size = Dimension(600, 600)
            location = Point(400, 400)
            renderDelegate = SolidColorRenderer(
                layer = layer,
                color = fgColor,
                continuousRedraw = true  // Continuously redraw to simulate a running animation
            )
            layer.renderDelegate = renderDelegate
            contentPane.add(layer, BorderLayout.CENTER)
        }

        window.isVisible = true
        delay(500)
        val pixelLocation = window.bounds.let {
            Point(it.x + it.width/2, it.y + it.height/2)
        }

        var nonBlackPixelDetected: Color? = null
        val stopThread = AtomicBoolean(false)
        // This semaphore ensures that screenshots are only taken when the window is becoming hidden/disposed.
        // It's necessary because the window can (and does, with SOFTWARE_COMPAT) also flash when becoming visible.
        val semaphore = Semaphore(1, true)
        val t = thread {
            val robot = Robot()
            while(!stopThread.get()) {
                semaphore.acquire()
                val pixel = robot.getPixelColor(pixelLocation.x, pixelLocation.y)
                semaphore.release()
                if (!pixel.closeTo(fgColor) && !pixel.closeTo(bgColor)) {
                    nonBlackPixelDetected = pixel
                    return@thread
                }
            }
        }

        try {
            // Check with `window.isVisible = false`
            repeat(20) {
                delay(200)
                window.isVisible = false
                delay(300)
                assertNull(nonBlackPixelDetected, "Detected a non-black pixel when hiding window")
                // Acquire the semaphore while making the window visible, to disable screenshotting
                semaphore.acquire()
                window.isVisible = true
                delay(500)
                semaphore.release()
            }

            // Check with `window.dispose()`
            repeat(20) {
                delay(200)
                window.dispose()
                delay(300)
                assertNull(nonBlackPixelDetected, "Detected a non-black pixel when disposing window")
                // Acquire the semaphore while making the window visible, to disable screenshotting
                semaphore.acquire()
                window.isVisible = true
                delay(500)
                semaphore.release()
            }
        } finally {
            stopThread.getAndSet(true)
            t.join()

            window.dispose()
            backgroundWindow.dispose()
        }
    }

    @Test
    fun `temporary change is not visible with needRedraw(throttledToVsync = false)`() = uiTest {
        assumeTrue(hostOs.isMacOS)
        // The separation between update and draw is only implemented in MetalRedrawer at the moment
        // Don't use assumeTrue, as uiTest iterates over multiple renderers,
        // and if one of them skipped, the whole test is skipped
        if (renderApi != GraphicsApi.METAL) return@uiTest

        val color = Color.BLACK
        val tempColor = Color.WHITE
        lateinit var renderDelegate: SolidColorRenderer
        val renderChannel = Channel<Unit>(Channel.CONFLATED)
        val window = UiTestWindow {
            size = Dimension(600, 600)
            location = Point(400, 400)
            renderDelegate = object: SolidColorRenderer(
                layer = layer,
                color = color,
                continuousRedraw = true
            ) {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    super.onRender(canvas, width, height, nanoTime)
                    assertTrue(renderChannel.trySend(Unit).isSuccess)
                }
            }
            layer.renderDelegate = renderDelegate
            contentPane.add(layer, BorderLayout.CENTER)
        }

        window.isVisible = true
        delay(1500)
        val pixelLocation = window.bounds.let {
            Point(it.x + it.width/2, it.y + it.height/2)
        }
        val robot = Robot()

        var tempColorVisibleCount = 0
        try {
            repeat(50) {
                // Wait for just after the next vsync, so we have plenty of time until the one after it
                val vSyncer = MetalVSyncer(window.layer.windowHandle)
                vSyncer.waitForVSync()

                // Set the color to temp, then immediately back to normal
                renderDelegate.color = tempColor
                renderDelegate.layer.needRedraw(throttledToVsync = false)
                renderChannel.receive()  // Wait until render is actually called
                renderDelegate.color = color
                renderDelegate.layer.needRedraw(throttledToVsync = false)

                // Check whether the temp color was visible
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < 400) {
                    val pixel = robot.getPixelColor(pixelLocation.x, pixelLocation.y)
                    if (pixel != color) {
                        tempColorVisibleCount++
                    }
                }
            }

            // Because the temp color can theoretically be visible if the JVM hiccups and a vsync happens before the
            // color is reverted, we allow a small percentage of the tries to fail. This way the flakiness of the test
            // is reduced.
            // Note that in practice, however, this test had never failed on an M1 Ultra machine with a 60Hz monitor.
            assertTrue(tempColorVisibleCount < 5)
        } finally {
            window.dispose()
        }
    }

    @Test
    fun `needRedraw throttled and regular calls render and draw once`() = uiTest {
        // Check that calling both needRedraw(true) and needRedraw(false) causes only one render and one draw call
        var renderCalls = 0
        val renderChannel = Channel<Unit>(Channel.CONFLATED)

        var drawCalls = 0
        val deviceAnalytics = object : SkiaLayerAnalytics.DeviceAnalytics {
            override fun beforeFrameRender() {
                drawCalls++
            }
        }
        val analytics = object : SkiaLayerAnalytics {
            @ExperimentalSkikoApi
            override fun device(
                skikoVersion: String,
                os: OS,
                api: GraphicsApi,
                deviceName: String?
            ): SkiaLayerAnalytics.DeviceAnalytics {
                return deviceAnalytics
            }
        }
        val window = UiTestWindow(analytics = analytics) {
            size = Dimension(600, 600)
            location = Point(400, 400)
            layer.renderDelegate = object: SkikoRenderDelegate {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    renderCalls++
                    renderChannel.trySend(Unit)
                }
            }
            contentPane.add(layer, BorderLayout.CENTER)
        }
        window.isVisible = true

        // Wait for things to settle down, specifically the workaround for JBR-5259, which moves
        // the backed layer when graphicsContextScaleTransform changes
        delay(100)

        try {
            renderChannel.receive()
            renderCalls = 0
            drawCalls = 0
            withContext(MainUIDispatcher) {
                window.layer.needRedraw(true)
                window.layer.needRedraw(false)
            }
            delay(100)
            assertEquals("Render was called more than once on needRedraw(true), needRedraw(false)", 1, renderCalls)
            assertEquals("Draw was called more than once on needRedraw(true), needRedraw(false)", 1, drawCalls)

            renderCalls = 0
            drawCalls = 0
            withContext(MainUIDispatcher) {
                window.layer.needRedraw(false)
                window.layer.needRedraw(true)
            }
            delay(100)
            assertEquals("Render was called more than once on needRedraw(false), needRedraw(true)", 1, renderCalls)
            assertEquals("Draw was called more than once on needRedraw(true), needRedraw(true)", 1, drawCalls)
        } finally {
            window.dispose()
        }
    }

    private class RectRenderer(
        private val getContentScale: () -> Float,
        var rectWidth: Int,
        var rectHeight: Int,
        private val rectColor: Color
    ) : SkikoRenderDelegate {
        constructor(
            layer: SkiaLayer,
            rectWidth: Int,
            rectHeight: Int,
            rectColor: Color
        ) : this(
            { layer.contentScale }, rectWidth, rectHeight, rectColor
        )

        constructor(
            layer: JFrame,
            rectWidth: Int,
            rectHeight: Int,
            rectColor: Color
        ) : this(
            { layer.graphicsConfiguration.defaultTransform.scaleX.toFloat() }, rectWidth, rectHeight, rectColor
        )

        private val contentScale get() = getContentScale()

        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), Paint().apply {
                color = Color.WHITE.rgb
            })
            canvas.drawRect(Rect(0f, 0f, rectWidth * contentScale, rectHeight * contentScale), Paint().apply {
                color = rectColor.rgb
            })
        }
    }



    private class AnimatedBoxRenderer(
        private val layer: SkiaLayer,
        private val pixelsPerSecond: Double,
        private val size: Double
    ) : SkikoRenderDelegate {
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

    private open class SolidColorRenderer(
        val layer: SkiaLayer,
        color: Color,
        continuousRedraw: Boolean = false
    ) : SkikoRenderDelegate {

        var continuousRedraw = continuousRedraw
            set(value) {
                if (value)
                    layer.needRedraw(throttledToVsync = true)
                field = value
            }

        var color = color
            set(value) {
                Logger.debug { "Color set to $value" }
                field = value
                paint.color = color.rgb
            }

        private var paint = Paint().also { it.color = color.rgb }

        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), paint)
            if (continuousRedraw) {
                layer.needRedraw()
            }
        }
    }

}

private fun JFrame.close() = dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
