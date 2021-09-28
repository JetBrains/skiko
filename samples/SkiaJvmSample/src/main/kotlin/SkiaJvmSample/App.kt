package SkiaJvmSample

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.*
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.*
import javax.swing.*
import kotlin.math.cos
import kotlin.math.sin
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
    var mouseX = 0
    var mouseY = 0

    val window = SkiaWindow()

    window.defaultCloseOperation =
        if (exitOnClose) WindowConstants.EXIT_ON_CLOSE else WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    // Create menu.
    val menuBar = JMenuBar()
    val fileMenu = JMenu("File")
    menuBar.add(fileMenu)

    val miFullscreenState = JMenuItem("Is fullscreen mode")
    val ctrlI = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miFullscreenState.setAccelerator(ctrlI)
    miFullscreenState.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            println("${window.title} is in fullscreen mode: ${window.layer.fullscreen}")
        }
    })

    val miToggleFullscreen = JMenuItem("Toggle fullscreen")
    val ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miToggleFullscreen.setAccelerator(ctrlF)
    miToggleFullscreen.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            window.layer.fullscreen = !window.layer.fullscreen
        }
    })

    val defaultScreenshotPath =
        Files.createTempFile("compose_", ".png").toAbsolutePath().toString()
    val miTakeScreenshot = JMenuItem("Take screenshot to $defaultScreenshotPath")
    val ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
    miTakeScreenshot.setAccelerator(ctrlS)
    miTakeScreenshot.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            val screenshot = window.layer.screenshot()!!
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

    val state = State()
    state.text = title

    window.layer.onStateChanged(SkiaLayer.PropertyKind.Renderer) { layer ->
        println("Changed renderer for $layer: new value is ${layer.renderApi}")
    }

    window.layer.renderer = Renderer(window.layer) {
        renderer, w, h, nanoTime -> displayScene(renderer, w, h, nanoTime, mouseX, mouseY, state)
    }

    window.layer.addMouseMotionListener(object : MouseMotionAdapter() {
        override fun mouseMoved(event: MouseEvent) {
            mouseX = event.x
            mouseY = event.y
        }
    })

    // MANDATORY: set window preferred size before calling pack()
    window.preferredSize = Dimension(800, 600)
    window.pack()
    window.layer.paint(window.graphics)
    window.isVisible = true
}

class Renderer(
    val layer: SkiaLayer,
    val displayScene: (Renderer, Int, Int, Long) -> Unit
): SkiaRenderer {
    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val font = Font(typeface, 40f)
    val paint = Paint().apply {
            color = 0xff9BC730L.toInt()
            mode = PaintMode.FILL
            strokeWidth = 1f
    }

    var canvas: Canvas? = null

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        this.canvas = canvas
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        displayScene(this, (width / contentScale).toInt(), (height / contentScale).toInt(), nanoTime)

        // Alpha layers test
        val rectW = 100f
        val rectH = 100f
        val left = (width / layer.contentScale - rectW) / 2f
        val top = (height / layer.contentScale - rectH) / 2f
        val pictureRecorder = PictureRecorder()
        val pictureCanvas = pictureRecorder.beginRecording(
            Rect.makeLTRB(left, top, left + rectW, top + rectH)
        )
        pictureCanvas.drawLine(left, top, left + rectW, top + rectH, Paint())
        val picture = pictureRecorder.finishRecordingAsPicture()
        canvas.drawPicture(picture, null, Paint())
        canvas.drawLine(left, top + rectH, left + rectW, top, Paint())

        layer.needRedraw()
    }
}

class State {
    var frame: Int = 0
    var text: String = "Hello Skija"
}

private val fontCollection = FontCollection()
    .setDefaultFontManager(FontMgr.default)

fun displayScene(renderer: Renderer, width: Int, height: Int, nanoTime: Long, xpos: Int, ypos: Int, state: State) {
    val canvas = renderer.canvas!!
    val watchFill = Paint().apply { color = 0xFFFFFFFF.toInt() }
    val watchStroke = Paint().apply {
           color = 0xFF000000.toInt()
           mode = PaintMode.STROKE
           strokeWidth = 1f
    }
    val watchStrokeAA = Paint().apply {
      color = 0xFF000000.toInt()
      mode = PaintMode.STROKE
      strokeWidth = 1f
    }
    val watchFillHover = Paint().apply { color = 0xFFE4FF01.toInt() }
    for (x in 0 .. (width - 50) step 50) {
        for (y in 20 .. (height - 50) step 50) {
            val hover = xpos > x + 0 && xpos < x + 50 && ypos > y + 0 && ypos < y + 50
            val fill = if (hover) watchFillHover else watchFill
            val stroke = if (x > width / 2) watchStrokeAA else watchStroke
            canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), fill)
            canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), stroke)
            var angle = 0f
            while (angle < 2f * Math.PI) {
                canvas.drawLine(
                        (x + 25 - 17 * sin(angle)),
                        (y + 25 + 17 * cos(angle)),
                        (x + 25 - 20 * sin(angle)),
                        (y + 25 + 20 * cos(angle)),
                        stroke
                )
                angle += (2.0 * Math.PI / 12.0).toFloat()
            }
            val time = (nanoTime / 1E6) % 60000 +
                    (x.toFloat() / width * 5000).toLong() +
                    (y.toFloat() / width * 5000).toLong()

            val angle1 = (time.toFloat() / 5000 * 2f * Math.PI).toFloat()
            canvas.drawLine(x + 25f, y + 25f,
                    x + 25f - 15f * sin(angle1),
                    y + 25f + 15 * cos(angle1),
                    stroke)

            val angle2 = (time / 60000 * 2f * Math.PI).toFloat()
            canvas.drawLine(x + 25f, y + 25f,
                    x + 25f - 10f * sin(angle2),
                    y + 25f + 10f * cos(angle2),
                    stroke)
        }
    }
    val text = "${state.text} ${state.frame++}!"
    canvas.drawString(text, xpos.toFloat(), ypos.toFloat(), renderer.font, renderer.paint)

    val style = ParagraphStyle()
    val paragraph = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("Graphics API: ${renderer.layer.renderApi} ✿ﾟ ${currentSystemTheme}")
            .popStyle()
            .build()
    paragraph.layout(Float.POSITIVE_INFINITY)
    paragraph.paint(canvas, 5f, 5f)
}
