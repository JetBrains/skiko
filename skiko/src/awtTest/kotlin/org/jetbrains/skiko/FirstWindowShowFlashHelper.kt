package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
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
    private const val X = 400
    private const val Y = 400
    private const val WIDTH = 600
    private const val HEIGHT = 600

    @JvmStatic
    fun main(args: Array<String>) {
        val renderApi = GraphicsApi.valueOf(args[0])
        val color = Color(args[1].toInt())

        SwingUtilities.invokeLater {
            val layer = SkiaLayer(properties = SkiaLayerProperties(renderApi = renderApi))
            layer.renderDelegate = object : SkikoRenderDelegate {
                private val paint = Paint().also { it.color = color.rgb }
                private val loadFill = Paint().also { it.color = Color.GRAY.rgb }

                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    val rect = Rect(0f, 0f, width.toFloat(), height.toFloat())
                    repeat(1000) {
                        val layerPaint = Paint().also {
                            it.imageFilter = ImageFilter.makeBlur(30f, 30f, FilterTileMode.CLAMP)
                        }
                        canvas.saveLayer(rect, layerPaint)
                        canvas.drawRect(rect, loadFill)
                        canvas.restore()
                    }
                    canvas.drawRect(rect, paint)
                }
            }

            JFrame().apply {
                contentPane.add(layer, BorderLayout.CENTER)
                setLocation(X, Y)
                size = Dimension(WIDTH, HEIGHT)
                // Ensure our window lands above the parent's background window at these coordinates.
                isAlwaysOnTop = true
                isVisible = true
            }
        }
    }
}
