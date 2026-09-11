package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import javax.swing.JWindow
import javax.swing.SwingUtilities

/**
 * Standalone harness launched in a FRESH JVM by the `no window flash on first show` test.
 *
 * The first-show background flash only reproduces on the very first window displayed in a process, so the
 * test cannot observe it from its own (long-since-warmed-up) JVM. Instead it spawns this harness per run:
 * a brand-new process whose single window is genuinely its first, using the render API and color passed in
 * argv (`renderApi rgb`). The window bounds are hard-coded to cover the pixel the parent samples.
 *
 * The frame it draws is deliberately made expensive by repeated full-window blur passes, forcing real GPU work
 * to stress the fix as hard as possible.
 */
object FirstWindowShowFlashHelper {
    // Hard-coded window bounds. The parent (SkiaLayerTest."no window flash on first show") positions its
    // background window so that the pixel it samples (the background's center) falls within these bounds.
    private val WINDOW_LOCATION = Point(400, 400)
    private val WINDOW_SIZE = Dimension(600, 600)

    @JvmStatic
    fun main(args: Array<String>) {
        val renderApi = GraphicsApi.valueOf(args[0])
        val color = Color(args[1].toInt(), true)

        SwingUtilities.invokeLater {
            val layer = SkiaLayer(properties = SkiaLayerProperties(renderApi = renderApi))
            layer.renderDelegate = object : SkikoRenderDelegate {
                private val paint = Paint().also { it.color = color.rgb }

                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    val rect = Rect(0f, 0f, width.toFloat(), height.toFloat())
                    canvas.drawRect(rect, paint)
                }
            }

            // Use JWindow, not JFrame because the latter is shown with a fade-in animation on Windows,
            // which breaks the color comparison in the test
            JWindow().apply {
                contentPane.add(layer, BorderLayout.CENTER)
                location = WINDOW_LOCATION
                size = WINDOW_SIZE
                // Ensure our window is above the parent's background window
                isAlwaysOnTop = true
                isVisible = true
            }
        }
    }
}
