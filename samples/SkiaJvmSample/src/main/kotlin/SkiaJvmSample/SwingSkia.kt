package SkiaJvmSample

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.*

private val state = State()
private var mouseX = 0
private var mouseY = 0

fun Button(
    text: String,
    action: (() -> Unit)? = null
): JButton {
    val button = JButton(text)
    button.setToolTipText("Tooltip for $text button.")
    button.setPreferredSize(Dimension(100, 100))
    button.addActionListener(object : ActionListener {
        public override fun actionPerformed(e: ActionEvent) {
            action?.invoke()
        }
    })

    return button
}

fun SwingSkia() = SwingUtilities.invokeLater {
    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = "SwingSkiaWindow"

    state.text = window.title

    var panel = getSkiaPanel()
    
    window.contentPane.add(
        Button(
            text = "Fullscreen",
            action = {
                panel.layer.fullscreen = !panel.layer.fullscreen
            }
        ),
        BorderLayout.NORTH
    )
    window.contentPane.add(
        Button(
            text = "Add SP",
            action = {
                window.contentPane.remove(panel) 
                panel = getSkiaPanel()
                window.contentPane.add(panel, BorderLayout.CENTER)
                window.setSize(window.width + 1, window.height)
            }
        ),
        BorderLayout.WEST
    )
    window.contentPane.add(
        Button(
            text = "Remove SP",
            action = {
                window.contentPane.remove(panel)
            }
        ),
        BorderLayout.EAST
    )
    window.contentPane.add(
        Button(
            text = "New Window",
            action = {
                createWindow("ComposeWindow", false)
            }
        ),
        BorderLayout.SOUTH
    )
    window.contentPane.add(panel, BorderLayout.CENTER)

    window.setSize(800, 600)
    window.setVisible(true)
}

private fun getSkiaPanel(): SkiaPanel {
    val panel = SkiaPanel()
    val btnPanelOK = JPanel()
    btnPanelOK.setLayout(BorderLayout(0, 0))
    btnPanelOK.setBackground(Color.white)
    btnPanelOK.add(JButton("OK"))
    val btnCancel = JButton("Cancel")
    panel.add(btnPanelOK)
    panel.add(btnCancel)
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
        }
    })
    return panel
}
