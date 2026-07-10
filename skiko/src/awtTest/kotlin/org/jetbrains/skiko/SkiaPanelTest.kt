package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeFalse
import org.junit.Test
import java.awt.GraphicsEnvironment
import javax.swing.SwingUtilities

/**
 * Unit tests for the push-only [SkiaPanel] present contract. They exercise the mode-independent present path
 * ([SkiaPanel.present] stores a picture; the panel re-presents that stored picture on demand) and the
 * present-side background + [SkiaPanel.clipComponents] cutouts, by rendering the panel's present body onto an
 * in-memory bitmap and reading pixels back — no window, GPU, or screen capture involved.
 */
@OptIn(ExperimentalSkikoApi::class)
class SkiaPanelTest {

    private class TestPanel(mode: RenderMode) : SkiaPanel(renderMode = mode) {
        // Fixed scale so the present path needs no realized window / graphicsConfiguration.
        override val contentScale: Float get() = 1f

        /** Runs the present-side draw ([drawStored]) onto [canvas] — the damage/expose re-present path. */
        fun renderStored(canvas: Canvas) = drawStored(canvas)
    }

    private val width = 100
    private val height = 100

    /** Renders the panel's stored content onto a fresh transparent bitmap and returns the ARGB at (x, y). */
    private fun TestPanel.pixelAt(x: Int, y: Int): Int {
        val bitmap = Bitmap()
        bitmap.allocN32Pixels(width, height, /* opaque = */ false)
        val canvas = Canvas(bitmap)
        canvas.clear(Color.TRANSPARENT)
        renderStored(canvas)
        return bitmap.getColor(x, y)
    }

    private fun emptyPicture(): Picture {
        val recorder = PictureRecorder()
        recorder.beginRecording(Rect.makeWH(width.toFloat(), height.toFloat()))
        return recorder.finishRecordingAsPicture()
    }

    private fun filledPicture(color: Int, rect: Rect): Picture {
        val recorder = PictureRecorder()
        val canvas = recorder.beginRecording(Rect.makeWH(width.toFloat(), height.toFloat()))
        canvas.drawRect(rect, Paint().apply { this.color = color })
        return recorder.finishRecordingAsPicture()
    }

    private fun onEdt(block: () -> Unit) = SwingUtilities.invokeAndWait(block)

    private fun modes(): List<SkiaPanel.RenderMode> {
        // SwingComposited needs no heavyweight peer; DirectSurface builds a HardwareLayer, so only run it
        // where a display exists.
        val all = SkiaPanel.RenderMode.values().toList()
        return if (GraphicsEnvironment.isHeadless()) {
            all.filter { it == SkiaPanel.RenderMode.SwingComposited }
        } else all
    }

    @Test
    fun `present of a bare picture still gets the configured background in both modes`() = onEdt {
        for (mode in modes()) {
            val panel = TestPanel(mode)
            panel.setSize(width, height)
            panel.background = java.awt.Color.RED

            panel.present(emptyPicture())

            // The bare picture draws nothing; the present path must still clear to the configured background.
            assertEquals("mode=$mode center is background red", 0xFFFF0000.toInt(), panel.pixelAt(50, 50))
        }
    }

    @Test
    fun `present stores and draws the picture over the background in both modes`() = onEdt {
        for (mode in modes()) {
            val panel = TestPanel(mode)
            panel.setSize(width, height)
            panel.background = java.awt.Color.WHITE

            panel.present(filledPicture(Color.BLUE, Rect.makeXYWH(0f, 0f, 20f, 20f)))

            assertEquals("mode=$mode picture content", Color.BLUE, panel.pixelAt(10, 10))
            assertEquals("mode=$mode background", 0xFFFFFFFF.toInt(), panel.pixelAt(50, 50))
        }
    }

    @Test
    fun `damage re-presents the stored picture without a new present`() = onEdt {
        val panel = TestPanel(SkiaPanel.RenderMode.SwingComposited)
        panel.setSize(width, height)
        panel.background = java.awt.Color.WHITE
        panel.present(filledPicture(Color.BLUE, Rect.makeXYWH(0f, 0f, 20f, 20f)))

        val first = panel.pixelAt(10, 10)
        // Second render with NO intervening present(): the stored picture is re-presented (damage/expose).
        val second = panel.pixelAt(10, 10)

        assertEquals(Color.BLUE, first)
        assertEquals("stored picture re-presented identically", first, second)
    }

    @Test
    fun `clipComponents cutout is applied on the present side`() = onEdt {
        val panel = TestPanel(SkiaPanel.RenderMode.SwingComposited)
        panel.setSize(width, height)
        panel.background = java.awt.Color.WHITE
        panel.clipComponents.add(ClipRectangle(0f, 0f, 20f, 20f))

        // A picture that fills the whole area blue; the cutout must survive on the present path.
        panel.present(filledPicture(Color.BLUE, Rect.makeWH(width.toFloat(), height.toFloat())))

        // Inside the cutout: neither the background clear nor the picture is drawn (clip excludes it).
        val cutoutPixel = panel.pixelAt(5, 5)
        assertEquals("cutout region is left untouched (transparent)", 0, cutoutPixel ushr 24 and 0xFF)
        // Outside the cutout: the picture is drawn.
        assertEquals("outside the cutout the picture shows", Color.BLUE, panel.pixelAt(50, 50))
        assertNotEquals(cutoutPixel, panel.pixelAt(50, 50))
    }
}
