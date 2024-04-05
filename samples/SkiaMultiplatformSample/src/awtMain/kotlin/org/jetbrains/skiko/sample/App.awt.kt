package org.jetbrains.skiko.sample

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.*
import java.awt.Dimension
import javax.swing.*

fun createWindow(title: String, skiaLayer: SkiaLayer) {
    val window = JFrame(title).apply {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        preferredSize = Dimension(800, 600)
    }
    skiaLayer.attachTo(window.contentPane)
    skiaLayer.needRedraw()
    window.pack()
    window.isVisible = true
}

fun main() {
    SwingUtilities.invokeLater {
        val skiaLayer = SkiaLayer()
        val clocks = AwtClocks(skiaLayer)
        skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, clocks)

        createWindow("Skiko example", skiaLayer)
    }

    SwingUtilities.invokeLater {
        val skiaLayer = SkiaLayer()
        val image = ImageInterpolationExample()

        skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, object : SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                image.draw(canvas, Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat()))
            }
        })

        createWindow("Interpolation example", skiaLayer)
    }
}
