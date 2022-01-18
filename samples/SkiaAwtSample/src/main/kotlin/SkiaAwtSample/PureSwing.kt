package SkiaAwtSample

import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.WindowConstants

fun pureSwing() {
    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.setSize(800, 600)

    window.contentPane.add(makeButton("Skiko").apply {
        addActionListener {
            createWindow("Skiko", false)
        }
    }, BorderLayout.NORTH)
    window.contentPane.add(makeButton("Swing").apply {
        addActionListener {
            pureSwing()
        }
    }, BorderLayout.WEST)
    window.contentPane.add(makeButton("Swing+Skiko").apply {
        addActionListener {
            swingSkia()
        }
    }, BorderLayout.EAST)
    window.isVisible = true
}