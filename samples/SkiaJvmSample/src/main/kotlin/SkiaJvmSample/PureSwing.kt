package SkiaJvmSample

import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.WindowConstants

fun pureSwing() {
    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.setSize(800, 600)

    window.contentPane.add(Button("Skiko").apply {
        addActionListener {
            createWindow("Skiko", false)
        }
    }, BorderLayout.NORTH)
    window.contentPane.add(Button("Swing").apply {
        addActionListener {
            pureSwing()
        }
    }, BorderLayout.WEST)
    window.contentPane.add(Button("Swing+Skiko").apply {
        addActionListener {
            SwingSkia()
        }
    }, BorderLayout.EAST)
    window.setVisible(true)
}