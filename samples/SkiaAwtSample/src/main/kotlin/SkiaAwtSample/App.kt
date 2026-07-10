@file:OptIn(ExperimentalSkikoApi::class)

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
    setupSkikoLoggerFactory { DefaultConsoleLogger.fromLevel(System.getProperty("skiko.log.level", "INFO")) }
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
    // The push-only presenter: we record frames into a Picture and hand them over with present(); there is no
    // renderDelegate and no internal frame loop here (see DirectSurfaceDriver below).
    val skiaPanel = SkiaPanel(pixelGeometry = pixelGeometry)
    val clocks = ClocksAwt({ skiaPanel.contentScale })
    skiaPanel.addMouseMotionListener(clocks)

    // Owns the DisplayFrameTicker(window) + present() loop. Created once the window is realized (below), because
    // DisplayFrameTicker(window) derives the vsync source from the panel's heavyweight surface.
    var driver: DirectSurfaceDriver? = null

    val window = JFrame(title)
    window.defaultCloseOperation =
        if (exitOnClose) WindowConstants.EXIT_ON_CLOSE else WindowConstants.DISPOSE_ON_CLOSE
    window.background = Color.GREEN
    window.contentPane.add(skiaPanel)
    window.addWindowListener(object : WindowAdapter() {
        override fun windowClosed(e: WindowEvent?) { driver?.dispose() }
    })

    // Create menu.
    val menuBar = JMenuBar()
    val fileMenu = JMenu("File")
    menuBar.add(fileMenu)

    val miFullscreenState = JMenuItem("Is fullscreen mode")
    val ctrlI = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miFullscreenState.setAccelerator(ctrlI)
    miFullscreenState.addActionListener {
        println("${window.title} is in fullscreen mode: ${skiaPanel.fullscreen}")
    }

    val miToggleFullscreen = JMenuItem("Toggle fullscreen")
    val ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miToggleFullscreen.setAccelerator(ctrlF)
    miToggleFullscreen.addActionListener {
        skiaPanel.fullscreen = !skiaPanel.fullscreen
    }

    val defaultScreenshotPath =
        Files.createTempFile("compose_", ".png").toAbsolutePath().toString()
    val miTakeScreenshot = JMenuItem("Take screenshot to $defaultScreenshotPath")
    val ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miTakeScreenshot.setAccelerator(ctrlS)
    miTakeScreenshot.addActionListener {
        // SkiaPanel is push-only and has no screenshot(): the driver (which owns the content) rasterizes it.
        val screenshot = driver?.screenshot() ?: return@addActionListener
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            val image = screenshot.toBufferedImage()
            ImageIO.write(image, "png", File(defaultScreenshotPath))
            println("Saved to $defaultScreenshotPath")
        }
    }

    val miScaleState = JMenuItem("Get current content scale")
    val ctrlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miScaleState.setAccelerator(ctrlD)
    miScaleState.addActionListener {
        // SkiaPanel exposes contentScale; it has no currentDPI (that was a SkiaLayer-only accessor).
        println("Content scale: ${skiaPanel.contentScale}")
    }

    fileMenu.add(miToggleFullscreen)
    fileMenu.add(miFullscreenState)
    fileMenu.add(miScaleState)
    fileMenu.add(miTakeScreenshot)

    val editMenu = JMenu("Edit")
    menuBar.add(editMenu)

    val miEmojiAndSymbols = JMenuItem("Emoji & Symbols")
    miEmojiAndSymbols.setAccelerator(KeyStroke.getKeyStroke("ctrl meta SPACE"))
    miEmojiAndSymbols.addActionListener {
        orderEmojiAndSymbolsPopup()
    }

    editMenu.add(miEmojiAndSymbols)

    window.setJMenuBar(menuBar)

    // Window transparency
    if (System.getProperty("skiko.transparency") == "true") {
        window.isUndecorated = true

        /**
         * There is a hack inside skiko OpenGL and Software redrawers for Windows that makes current
         * window transparent without setting `background` to JDK's window. It's done by getting native
         * component parent and calling `DwmEnableBlurBehindWindow`.
         */
        if (hostOs != OS.Windows) {
            window.background = Color(0, 0, 0, 0)
        }
        skiaPanel.transparency = true

        /*
         * Windows makes clicks on transparent pixels fall through, but it doesn't work
         * with GPU accelerated rendering since this check requires having access to pixels from CPU.
         */
        if (hostOs == OS.Windows) {
            val contentPane = window.contentPane as JComponent
            contentPane.background = Color(0, 0, 0, 1)
            contentPane.isOpaque = true
        }
    } else {
        skiaPanel.background = Color.LIGHT_GRAY
    }

    // MANDATORY: set window preferred size before calling pack()
    window.preferredSize = Dimension(800, 600)
    window.pack()
    window.isVisible = true

    // The window is realized now, so its heavyweight skiko surface exists: start the vsync-driven present loop.
    driver = DirectSurfaceDriver(skiaPanel, clocks, window).also { it.start() }
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
