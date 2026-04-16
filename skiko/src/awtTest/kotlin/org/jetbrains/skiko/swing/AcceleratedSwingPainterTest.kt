package org.jetbrains.skiko.swing

import com.jetbrains.SharedTextures
import org.jetbrains.skia.Surface
import org.junit.Test
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.Image
import java.awt.image.BufferedImage
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcceleratedSwingPainterTest {

    @Test
    fun `falls back for incompatible GraphicsConfiguration`() {
        val fallback = RecordingSwingPainter()
        val sharedTextures = FakeSharedTextures()
        val painter = AcceleratedSwingPainter(fallback, sharedTextures)

        val image = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB_PRE)
        val g = image.createGraphics()
        val surface = Surface.makeRasterN32Premul(8, 8)

        try {
            painter.paint(g, surface, 42L)

            assertEquals(1, fallback.paintCalls)
            assertEquals(0, sharedTextures.wrapTextureCalls)
        } finally {
            g.dispose()
            surface.close()
        }
    }

    @Test
    fun `fallback path clears accelerated cache`() {
        val fallback = RecordingSwingPainter()
        val sharedTextures = FakeSharedTextures()
        val painter = AcceleratedSwingPainter(fallback, sharedTextures)

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

            assertNull(painter.imageWrapperForTesting)
            assertEquals(0L, painter.texturePtrForTesting)
            assertEquals(1, fallback.paintCalls)
        } finally {
            g.dispose()
            surface.close()
        }
    }

    @Test
    fun `dispose delegates to fallback`() {
        val fallback = RecordingSwingPainter()
        val painter = AcceleratedSwingPainter(fallback, FakeSharedTextures())

        painter.dispose()

        assertEquals(1, fallback.disposeCalls)
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
}
