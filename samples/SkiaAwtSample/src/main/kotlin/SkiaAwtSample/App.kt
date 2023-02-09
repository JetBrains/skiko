package SkiaAwtSample

import kotlinx.coroutines.*
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.*
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.*
import java.awt.RenderingHints
import javax.swing.*
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val windows = parseArgs(args)
    repeat(windows) {
        when (System.getProperty("skiko.swing.interop")) {
            "true" -> swingSkia()
            else -> createWindow("window $it", windows == 1)
        }
    }
}

fun createWindow(title: String, exitOnClose: Boolean) = SwingUtilities.invokeLater {
    val renderingHints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints") as Map<Any, Any>
    val pixelGeometry = when (renderingHints[RenderingHints.KEY_TEXT_ANTIALIASING]) {
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB -> PixelGeometry.RGB_H
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR -> PixelGeometry.BGR_H
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB -> PixelGeometry.RGB_V
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR -> PixelGeometry.BGR_V
        else -> PixelGeometry.UNKNOWN
    }
    val skiaLayer = SkiaLayer(pixelGeometry = pixelGeometry)
    val clocks = ClocksAwt(skiaLayer)

    val window = JFrame(title)
    window.defaultCloseOperation =
        if (exitOnClose) WindowConstants.EXIT_ON_CLOSE else WindowConstants.DISPOSE_ON_CLOSE
    window.background = Color.GREEN
    window.contentPane = skiaLayer

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
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.IO) {
                val image = screenshot.toBufferedImage()
                ImageIO.write(image, "png", File(defaultScreenshotPath))
                println("Saved to $defaultScreenshotPath")
            }
        }
    })

    val miDpiState = JMenuItem("Get current DPI")
    val ctrlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miDpiState.setAccelerator(ctrlD)
    miDpiState.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            println("DPI: ${skiaLayer.currentDPI}")
        }
    })

    fileMenu.add(miToggleFullscreen)
    fileMenu.add(miFullscreenState)
    fileMenu.add(miDpiState)
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

    skiaLayer.addView(GenericSkikoView(skiaLayer, clocks))

    // Window transparency
    if (System.getProperty("skiko.transparency") == "true") {
        window.isUndecorated = true
        // On Windows we don't set transparent background to avoid event input issues (JDK specific)
        if (hostOs != OS.Windows) {
            window.background = Color(0, 0, 0, 0)
        }
        skiaLayer.transparency = true
    } else {
        skiaLayer.background = Color.LIGHT_GRAY
    }

    // MANDATORY: set window preferred size before calling pack()
    window.preferredSize = Dimension(800, 600)
    window.pack()
    window.isVisible = true
}

private fun parseArgs(args: Array<String>): Int {
    var windows = 1
    for(arg in args) {
        try {
            windows = arg.toInt()
            break      
        }
        catch(e: NumberFormatException) {
            println("The passed argument:($arg) is not a integer number!")
        }
    }
    return windows
}
