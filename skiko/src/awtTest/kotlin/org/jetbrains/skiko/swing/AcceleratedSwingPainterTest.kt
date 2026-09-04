package org.jetbrains.skiko.swing

import com.jetbrains.SharedTextures
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.GpuPriority
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.graphicapi.DirectXOffscreenContext
import org.jetbrains.skiko.toImage
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import java.awt.Color
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.GridLayout
import java.awt.image.BufferedImage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class AcceleratedSwingPainterTest {
    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun `falls back for incompatible GraphicsConfiguration`() {
        val fallbackPainter = RecordingSwingPainter()
        val fallbackPainterCreator = { fallbackPainter }
        val sharedTextures = FakeSharedTextures()
        val painter = AcceleratedSwingPainter(sharedTextures, fallbackPainterCreator)

        val image = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB_PRE)
        val g = image.createGraphics()
        val surface = Surface.makeRasterN32Premul(8, 8)

        try {
            painter.paint(g, surface, 42L)

            assertEquals(1, fallbackPainter.paintCalls)
            assertEquals(0, sharedTextures.wrapTextureCalls)
        } finally {
            g.dispose()
            surface.close()
        }
    }

    @Test
    fun `fallback path clears accelerated cache`() {
        val fallbackPainter = RecordingSwingPainter()
        val fallbackPainterCreator = { fallbackPainter }
        val sharedTextures = FakeSharedTextures()
        val painter = AcceleratedSwingPainter(sharedTextures, fallbackPainterCreator)

        val image = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB_PRE)
        val g = image.createGraphics()
        val surface = Surface.makeRasterN32Premul(8, 8)

        try {
            painter.setCachedStateForTesting(
                imageWrapper = BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB_PRE),
                texturePtr = 777L,
                gc = g.deviceConfiguration
            )

            painter.paint(g, surface, 42L)

            assertNull(painter.imageWrapper)
            assertEquals(0L, painter.texturePtr)
            assertEquals(1, fallbackPainter.paintCalls)
        } finally {
            g.dispose()
            surface.close()
        }
    }

    @Test
    fun `does not crash when painting to sw bitmap before initialization`() {
        runBlocking(MainUIDispatcher) {
            val window = JFrame()
            try {
                val layer = SkiaSwingLayer(FakeRenderer(window, 100, 100, Color.RED))
                window.contentPane.add(layer)
                window.setSize(100, 100)
                delay(1.seconds)

                val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)
                val g2d = image.createGraphics()
                window.contentPane.paint(g2d)
                g2d.dispose()
            } finally {
                window.dispose()
            }
        }
    }

    @Test
    fun `can paint to sw bitmap after initialization`() {
        runBlocking(MainUIDispatcher) {
            val window = JFrame()
            try {
                val layer = SkiaSwingLayer(FakeRenderer(window, 100, 100, Color.RED))
                window.contentPane.add(layer)
                window.setSize(100, 100)
                window.isUndecorated = true
                window.isVisible = true
                delay(1.seconds)

                val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)
                val g2d = image.createGraphics()
                layer.paint(g2d)
                g2d.dispose()

                screenshots.assert(image.toImage(), "windowBitmap")
            } finally {
                window.dispose()
            }
        }
    }

    /**
     * Manual desktop reproducer for SKIKO-1116. Device creation is intentionally done
     * from EDT because this is the production path that was observed to freeze.
     *
     * Run on a physical Windows machine with:
     * ```
     * ./gradlew :skiko:awtTest \
     *   --tests "org.jetbrains.skiko.swing.AcceleratedSwingPainterTest.stress Direct3D Swing device creation in lifecycle states" \
     *   -Dskiko.test.direct3d.stress.enabled=true -Dskiko.test.direct3d.stress.iterations=100
     * ```
     *
     * While it runs, trigger a display-driver reset with Win+Ctrl+Shift+B, then repeat
     * around sleep/resume, monitor reconnect, and RDP connect/disconnect. A brief display
     * flicker is expected. The reproducer is successful only if an "attach" measurement is
     * slow; a quick "Failed to create DirectX12 device" followed by fallback is not this bug.
     * Do not change TDR registry settings or run a deliberately hanging GPU workload.
     */
    @Test
    @OptIn(ExperimentalSkikoApi::class)
    fun `stress Direct3D Swing device creation in lifecycle states`() {
        assumeTrue(hostOs == OS.Windows)
        assumeFalse(GraphicsEnvironment.isHeadless())
        assumeTrue(System.getProperty("skiko.test.direct3d.stress.enabled") == "true")

        val iterations = System.getProperty("skiko.test.direct3d.stress.iterations", "5").toInt()
        val maxDurationMs = System.getProperty("skiko.test.direct3d.stress.maxDurationMs", "10000").toLong()
        val textureSize = System.getProperty("skiko.test.direct3d.stress.textureSize", "256").toInt()
        val maxResidentWindows = System.getProperty("skiko.test.direct3d.stress.maxResidentWindows", "12").toInt()

        runBlocking(MainUIDispatcher) {
            val windows = mutableListOf<JFrame>()
            try {
                GpuPriority.entries.forEach { adapterPriority ->
                    repeat(iterations) { iteration ->
                    // The layer is added before its window is displayable; addNotify occurs on show.
                    JFrame().also { window ->
                        windows += window
                        window.setSize(64, 64)
                        window.contentPane.add(newDirect3DLayer(adapterPriority))
                        measureDeviceCreation("$adapterPriority / show invisible window", iteration, maxDurationMs) {
                            window.isVisible = true
                        }
                    }

                    // The layer is added to an already displayable hierarchy, as in tool-window opening.
                    JFrame().also { window ->
                        windows += window
                        window.setSize(64, 64)
                        window.isVisible = true
                        measureDeviceCreation("$adapterPriority / attach to visible window", iteration, maxDurationMs) {
                            window.contentPane.add(newDirect3DLayer(adapterPriority))
                        }
                    }

                    // Tool-window contents can be attached before layout assigns a non-zero size.
                    JFrame().also { window ->
                        windows += window
                        window.setSize(64, 64)
                        window.isVisible = true
                        val layer = newDirect3DLayer(adapterPriority).apply { setSize(0, 0) }
                        measureDeviceCreation("$adapterPriority / attach zero-sized layer", iteration, maxDurationMs) {
                            window.contentPane.add(layer)
                        }
                    }

                    // A hidden tab is displayable but not showing, matching deferred tool-window content.
                    JFrame().also { window ->
                        windows += window
                        val tabs = JTabbedPane().also { window.contentPane.add(it) }
                        tabs.addTab("visible", JPanel())
                        window.setSize(64, 64)
                        window.isVisible = true
                        measureDeviceCreation("$adapterPriority / attach in hidden tab", iteration, maxDurationMs) {
                            tabs.addTab("hidden", newDirect3DLayer(adapterPriority))
                        }
                    }

                    // Create and retain real GPU textures before creating another device.
                    val residentLayers = mutableListOf<SkiaSwingLayer>()
                    // Keep earlier devices alive, then create several more in one EDT action.
                    JFrame().also { window ->
                        windows += window
                        window.setSize(256, 256)
                        window.isVisible = true
                        val panel = JPanel(GridLayout(2, 2))
                        measureDeviceCreation("$adapterPriority / attach four layers", iteration, maxDurationMs) {
                            repeat(4) {
                                newDirect3DLayer(adapterPriority).also {
                                    residentLayers += it
                                    panel.add(it)
                                }
                            }
                            window.contentPane.add(panel)
                        }
                        residentLayers.forEach { renderIntoBitmap(it, textureSize) }
                    }

                    // Reattaching the same layer disposes and creates its device again.
                    JFrame().also { window ->
                        windows += window
                        val layer = newDirect3DLayer(adapterPriority)
                        window.contentPane.add(layer)
                        window.setSize(64, 64)
                        window.isVisible = true
                        measureDeviceCreation("$adapterPriority / reattach layer", iteration, maxDurationMs) {
                            window.contentPane.remove(layer)
                            window.contentPane.add(layer)
                        }
                    }

                    // The EDT normally serializes Swing attachment, but other code can create
                    // Direct3D offscreen contexts in parallel. Exercise that race explicitly.
                    val creationStarted = CountDownLatch(1)
                    val executor = Executors.newSingleThreadExecutor()
                    val offscreenContext = executor.submit<DirectXOffscreenContext> {
                        creationStarted.countDown()
                        DirectXOffscreenContext()
                    }
                    try {
                        creationStarted.await()
                        JFrame().also { window ->
                            windows += window
                            window.setSize(64, 64)
                            window.isVisible = true
                            measureDeviceCreation("$adapterPriority / parallel offscreen device", iteration, maxDurationMs) {
                                window.contentPane.add(newDirect3DLayer(adapterPriority))
                            }
                        }
                    } finally {
                        offscreenContext.get().close()
                        executor.shutdownNow()
                    }

                    while (windows.size > maxResidentWindows) {
                        windows.removeAt(0).dispose()
                    }
                    }
                }
            } finally {
                windows.forEach(JFrame::dispose)
            }
        }
    }

    private fun newDirect3DLayer(adapterPriority: GpuPriority) = SkiaSwingLayer(
        renderDelegate = SkikoRenderDelegate { _, _, _, _ -> },
        properties = SkiaLayerProperties(
            renderApi = GraphicsApi.DIRECT3D,
            adapterPriority = adapterPriority
        )
    )

    private fun renderIntoBitmap(layer: SkiaSwingLayer, size: Int) {
        layer.setSize(size, size)
        val graphics = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE).createGraphics()
        try {
            layer.paint(graphics)
        } finally {
            graphics.dispose()
        }
    }

    private inline fun measureDeviceCreation(name: String, iteration: Int, maxDurationMs: Long, block: () -> Unit) {
        val startedAt = System.nanoTime()
        block()
        val durationMs = (System.nanoTime() - startedAt) / 1_000_000
        println("Direct3D Swing device creation [$name #$iteration]: ${durationMs}ms")
        check(durationMs < maxDurationMs) {
            "Direct3D Swing device creation [$name #$iteration] took ${durationMs}ms"
        }
    }

    private class RecordingSwingPainter : SwingPainter {
        var paintCalls = 0
        var disposeCalls = 0

        override fun paint(g: Graphics2D, surface: Surface, texture: Long) {
            paintCalls++
        }

        override fun dispose() {
            disposeCalls++
        }
    }

    private class FakeSharedTextures : SharedTexturesAdapter {
        var wrapTextureCalls = 0

        override val textureType: Int = SharedTextures.METAL_TEXTURE_TYPE

        override fun wrapTexture(gc: GraphicsConfiguration, texturePtr: Long): Image {
            wrapTextureCalls++
            return BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB_PRE)
        }
    }

    private class FakeRenderer(
        private val getContentScale: () -> Float,
        var rectWidth: Int,
        var rectHeight: Int,
        private val rectColor: Color
    ) : SkikoRenderDelegate {

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
}
