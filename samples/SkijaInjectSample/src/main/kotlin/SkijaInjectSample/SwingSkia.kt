package SkijaInjectSample

import java.awt.Dimension
import java.awt.BorderLayout
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.event.MouseInputAdapter
import javax.swing.WindowConstants
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.SkiaLayer

fun Button(text: String): JButton {
    val btn = JButton(text)
    btn.setPreferredSize(Dimension(100, 100))
    return btn
}

fun SwingSkia() {
    Library.load("/", "skiko")

    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    window.title = "SwingSkiaWindow"

    val layer = SkiaLayer()

    window.contentPane.add(Button("North"), BorderLayout.NORTH)
    window.contentPane.add(Button("West"), BorderLayout.WEST)
    window.contentPane.add(Button("East"), BorderLayout.EAST)
    window.contentPane.add(Button("South"), BorderLayout.SOUTH)
    window.contentPane.add(layer, BorderLayout.CENTER)

    val state = State()
    state.text = window.title

    var mouseX = 0
    var mouseY = 0

    layer.renderer = Renderer { renderer, w, h -> displayScene(renderer, w, h, mouseX, mouseY, state) }

    layer.addMouseMotionListener(object : MouseMotionAdapter() {
        override fun mouseMoved(event: MouseEvent) {
            mouseX = event.x
            mouseY = event.y
            layer.display()
        }
    })

    window.setSize(800, 600)
    window.setVisible(true)
}
