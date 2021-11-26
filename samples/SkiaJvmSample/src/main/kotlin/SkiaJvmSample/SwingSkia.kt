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
import org.jetbrains.skiko.GenericSkikoView

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
    return SkiaPanel().apply {
        layer.addView(GenericSkikoView(layer, ClocksJvm(layer)))
        val btnPanelOK = JPanel()
        btnPanelOK.setLayout(BorderLayout(0, 0))
        btnPanelOK.setBackground(Color.white)
        btnPanelOK.add(JButton("OK"))
        val btnCancel = JButton("Cancel")
        add(btnPanelOK)
        add(btnCancel)
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                btnPanelOK.setBounds(width - 200, height - 100, 200, 40)
                btnCancel.setBounds(width - 200, height - 50, 200, 40)
                invalidate()
                validate()
                repaint()
            }
        })
    }
}
