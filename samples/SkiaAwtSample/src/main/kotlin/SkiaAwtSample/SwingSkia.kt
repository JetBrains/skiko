package SkiaAwtSample

import org.jetbrains.skiko.SkiaLayerRenderDelegate
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*

fun makeButton(
    text: String,
    action: (() -> Unit)? = null
): JButton {
    val button = JButton(text)
    button.toolTipText = "Tooltip for $text button."
    button.preferredSize = Dimension(100, 100)
    button.addActionListener { action?.invoke() }

    return button
}

fun swingSkia() = SwingUtilities.invokeLater {
    val window = JFrame()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = "SwingSkiaWindow"

    var panel = getSkiaPanel()
    
    window.contentPane.add(
        makeButton(
            text = "Fullscreen",
            action = {
                panel.layer.fullscreen = !panel.layer.fullscreen
            }
        ),
        BorderLayout.NORTH
    )
    window.contentPane.add(
        makeButton(
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
        makeButton(
            text = "Remove SP",
            action = {
                window.contentPane.remove(panel)
            }
        ),
        BorderLayout.EAST
    )
    val southButtonsPanel = JPanel(BorderLayout())
    southButtonsPanel.add(
        makeButton(
            text = "New Window",
            action = {
                createWindow("ComposeWindow", false)
            }
        ),
        BorderLayout.SOUTH
    )
    southButtonsPanel.add(
        makeButton(
            text = "Resize test window",
            action = {
                swingSkiaResize()
            }
        ),
        BorderLayout.CENTER
    )
    window.contentPane.add(southButtonsPanel, BorderLayout.SOUTH)
    window.contentPane.add(panel, BorderLayout.CENTER)

    window.setSize(800, 600)
    window.isVisible = true
}

private fun getSkiaPanel(): SkiaPanel {
    return SkiaPanel().apply {
        val clocks = ClocksAwt(layer)
        layer.renderDelegate = SkiaLayerRenderDelegate(layer, clocks)
        layer.addMouseMotionListener(clocks)
        val btnPanelOK = JPanel()
        btnPanelOK.layout = BorderLayout(0, 0)
        btnPanelOK.background = Color.white
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
