package org.jetbrains.skiko.swing

import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.SkikoProperties
import org.jetbrains.skiko.SkikoRenderDelegate
import java.awt.GraphicsEnvironment
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.SwingUtilities
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Test

/**
 * Covers [SkiaSwingLayer.needRender] itself, as opposed to the pacer behind it: the thread check,
 * and the fallback to a plain repaint when there is no pacer to defer to.
 */
@OptIn(ExperimentalSkikoApi::class)
class SkiaSwingLayerNeedRenderTest {

    @Before
    fun setUp() {
        assumeFalse(GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance)
    }

    @Test
    fun `needRender requires the event dispatch thread`() {
        val layer = onEdtGet { RepaintCountingLayer() }
        try {
            layer.needRender()
            fail("needRender must reject calls from outside the event dispatch thread")
        } catch (_: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `needRender repaints directly when pacing is off`() {
        // The property is off by default and is read when the layer is added to a hierarchy, so a
        // layer built here has no pacer and needRender has nothing to defer to.
        assertFalse(SkikoProperties.swingFramePacingEnabled)

        SwingUtilities.invokeAndWait {
            val layer = RepaintCountingLayer()
            val before = layer.repaintCount.get()
            layer.needRender()
            assertEquals(before + 1, layer.repaintCount.get(), "needRender did not fall back to repaint()")
        }
    }

    private fun <T> onEdtGet(block: () -> T): T {
        var result: T? = null
        SwingUtilities.invokeAndWait { result = block() }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    /** Counts the no-argument [java.awt.Component.repaint] calls the layer issues. */
    private class RepaintCountingLayer : SkiaSwingLayer(SkikoRenderDelegate { _, _, _, _ -> }) {
        val repaintCount = AtomicInteger()

        override fun repaint() {
            // JPanel's constructor calls repaint() before this class's fields are initialized.
            @Suppress("UNNECESSARY_SAFE_CALL", "SENSELESS_COMPARISON")
            repaintCount?.incrementAndGet()
            super.repaint()
        }
    }
}
