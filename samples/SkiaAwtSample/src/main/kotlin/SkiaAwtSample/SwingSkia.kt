@file:OptIn(ExperimentalSkikoApi::class)

package SkiaAwtSample

import org.jetbrains.skiko.ClipComponent
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.SkiaPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*

/**
 * The heavyweight/lightweight interop demo: overlay Swing components on top of a heavyweight skiko surface,
 * cutting holes in the surface so they show through.
 *
 * This thin [JLayeredPane] hosts the real [org.jetbrains.skiko.SkiaPanel] on top (layer 10), keeps overlay
 * children below (layer 0), and registers each child as a clip cutout via [SkiaPanel.clipComponents]. Frames
 * are pushed by a [DirectSurfaceDriver].
 */
class SwingInteropPane : JLayeredPane() {
    val skiaPanel = SkiaPanel()
    private val clocks = ClocksAwt({ skiaPanel.contentScale })
    private var driver: DirectSurfaceDriver? = null

    init {
        layout = null
        background = Color.white
        skiaPanel.addMouseMotionListener(clocks)
    }

    override fun add(component: Component): Component {
        skiaPanel.clipComponents.add(ClipComponent(component))
        return super.add(component, Integer.valueOf(0))
    }

    override fun doLayout() {
        skiaPanel.setBounds(0, 0, width, height)
    }

    override fun addNotify() {
        super.addNotify()
        super.add(skiaPanel, Integer.valueOf(10))
        // Start the vsync-driven present loop once the panel's heavyweight surface is realized.
        val window = SwingUtilities.getWindowAncestor(this)
        SwingUtilities.invokeLater {
            if (driver == null && window != null) {
                driver = DirectSurfaceDriver(skiaPanel, clocks, window).also { it.start() }
            }
        }
    }

    override fun removeNotify() {
        driver?.dispose()
        driver = null
        skiaPanel.dispose()
        super.removeNotify()
    }
}

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
                panel.skiaPanel.fullscreen = !panel.skiaPanel.fullscreen
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

private fun getSkiaPanel(): SwingInteropPane {
    return SwingInteropPane().apply {
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
