package org.jetbrains.skiko.sample

import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaWindow

import java.awt.Dimension
import javax.swing.*

fun main() {
    val skiaLayer = SkiaLayer()
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, BouncingBalls())
    SwingUtilities.invokeLater {
        val window = SkiaWindow()
        window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        window.title = "Skiko example"
        window.preferredSize = Dimension(800, 600)
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
    }
}