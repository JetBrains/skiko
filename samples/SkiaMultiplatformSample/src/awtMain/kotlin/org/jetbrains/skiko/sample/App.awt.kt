package org.jetbrains.skiko.sample

import org.jetbrains.skiko.*
import java.awt.Dimension
import javax.swing.*

fun main() {
    val skiaLayer = SkiaLayer()
    val app = run {
        //EmojiStory()
        AwtClocks(skiaLayer)
    }
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, app)
    SwingUtilities.invokeLater {
        val window = JFrame("Skiko example").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(800, 600)
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
    }
}
