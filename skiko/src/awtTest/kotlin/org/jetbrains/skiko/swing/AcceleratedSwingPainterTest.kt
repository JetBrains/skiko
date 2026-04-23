package org.jetbrains.skiko.swing

import com.jetbrains.SharedTextures
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.toImage
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import java.awt.Color
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.JFrame
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
                delay(1000)

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
                delay(1000)

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