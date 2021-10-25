package SkiaJvmSample

import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.*
import javax.swing.*
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

fun main() {
    val windows = 1
    repeat(windows) {
        createWindow("window $it", windows == 1)
    }
}

fun createWindow(title: String, exitOnClose: Boolean) = SwingUtilities.invokeLater {
    val skiaLayer = SkiaLayer()
    val clocks = ClocksJvm(skiaLayer)

    val window = JFrame(title)
    window.defaultCloseOperation =
        if (exitOnClose) WindowConstants.EXIT_ON_CLOSE else WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    skiaLayer.attachTo(window.contentPane)

    // Create menu.
    val menuBar = JMenuBar()
    val fileMenu = JMenu("File")
    menuBar.add(fileMenu)

    val miFullscreenState = JMenuItem("Is fullscreen mode")
    val ctrlI = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miFullscreenState.setAccelerator(ctrlI)
    miFullscreenState.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            println("${window.title} is in fullscreen mode: ${skiaLayer.fullscreen}")
        }
    })

    val miToggleFullscreen = JMenuItem("Toggle fullscreen")
    val ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miToggleFullscreen.setAccelerator(ctrlF)
    miToggleFullscreen.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            skiaLayer.fullscreen = !skiaLayer.fullscreen
        }
    })

    val defaultScreenshotPath =
        Files.createTempFile("compose_", ".png").toAbsolutePath().toString()
    val miTakeScreenshot = JMenuItem("Take screenshot to $defaultScreenshotPath")
    val ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miTakeScreenshot.setAccelerator(ctrlS)
    miTakeScreenshot.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            val screenshot = skiaLayer.screenshot()!!
            GlobalScope.launch(Dispatchers.IO) {
                val image = screenshot.toBufferedImage()
                ImageIO.write(image, "png", File(defaultScreenshotPath))
                println("Saved to $defaultScreenshotPath")
            }
        }
    })

    fileMenu.add(miToggleFullscreen)
    fileMenu.add(miFullscreenState)
    fileMenu.add(miTakeScreenshot)

    val editMenu = JMenu("Edit")
    menuBar.add(editMenu)

    val miEmojiAndSymbols = JMenuItem("Emoji & Symbols")
    miEmojiAndSymbols.setAccelerator(KeyStroke.getKeyStroke("ctrl meta SPACE"))
    miEmojiAndSymbols.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            orderEmojiAndSymbolsPopup()
        }
    })

    editMenu.add(miEmojiAndSymbols)

    window.setJMenuBar(menuBar)

    skiaLayer.onStateChanged(SkiaLayer.PropertyKind.Renderer) { layer ->
        println("Changed renderer for $layer: new value is ${layer.renderApi}")
    }

    skiaLayer.skikoView = GenericSkikoView(skiaLayer, clocks)

    skiaLayer.addMouseMotionListener(object : MouseMotionAdapter() {
        override fun mouseMoved(event: MouseEvent) {
            clocks.xpos = event.x
            clocks.ypos = event.y
        }
    })

    // Window transparency
    if (System.getProperty("skiko.transparency") == "true") {
        window.setUndecorated(true)
        // On Windows we don't set transparent background to avoid event input issues (JDK specific)
        if (hostOs != OS.Windows) {
            window.background = Color(0, 0, 0, 0)
        }
        skiaLayer.transparency = true
    } else {
        skiaLayer.background = Color.WHITE
    }

    // MANDATORY: set window preferred size before calling pack()
    window.preferredSize = Dimension(800, 600)
    window.pack()
    skiaLayer.disableTitleBar(32f)
    window.pack()
    skiaLayer.paint(window.graphics)
    window.isVisible = true
}
