package SkijaInjectSample

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JFrame
import javax.swing.JButton
import javax.swing.JLayeredPane
import javax.swing.JPanel
import javax.swing.event.MouseInputAdapter
import javax.swing.WindowConstants
import org.jetbrains.skiko.SkiaPanel


fun Button(text: String): JButton {
    val btn = JButton(text)
    btn.setPreferredSize(Dimension(100, 100))
    return btn
}

fun SwingSkia() {

    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    window.title = "SwingSkiaWindow"

    val panel = SkiaPanel()

    window.contentPane.add(Button("North"), BorderLayout.NORTH)
    window.contentPane.add(Button("West"), BorderLayout.WEST)
    window.contentPane.add(Button("East"), BorderLayout.EAST)
    window.contentPane.add(Button("South"), BorderLayout.SOUTH)
    window.contentPane.add(panel, BorderLayout.CENTER)

    val btnPanelOK = JPanel()
    btnPanelOK.setLayout(BorderLayout(0, 0))
    btnPanelOK.setBackground(Color.white)
    btnPanelOK.add(JButton("OK"))

    val btnCancel = JButton("Cancel")

    panel.add(btnPanelOK)
    panel.add(btnCancel)

    val state = State()
    state.text = window.title

    var mouseX = 0
    var mouseY = 0

    panel.addComponentListener(object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent) {
            btnPanelOK.setBounds(panel.width - 200, panel.height - 100, 200, 40)
            btnCancel.setBounds(panel.width - 200, panel.height - 50, 200, 40)
            panel.invalidate()
            panel.validate()
            panel.repaint()
        }
    })

    panel.layer.renderer = Renderer(panel.layer) { renderer, w, h, nanoTime -> displayScene(renderer, w, h, nanoTime, mouseX, mouseY, state) }

    panel.layer.addMouseMotionListener(object : MouseMotionAdapter() {
        override fun mouseMoved(event: MouseEvent) {
            mouseX = event.x
            mouseY = event.y
            panel.layer.display()
        }
    })

    window.setSize(800, 600)
    window.setVisible(true)
}
