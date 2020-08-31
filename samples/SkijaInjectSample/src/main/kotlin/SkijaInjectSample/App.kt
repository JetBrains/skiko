package SkijaInjectSample

import org.jetbrains.skiko.SkiaWindow

import java.awt.event.MouseEvent
import javax.swing.WindowConstants
import javax.swing.event.MouseInputAdapter
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaRenderer
import java.awt.event.MouseMotionAdapter
import kotlin.math.cos
import kotlin.math.sin
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.TextStyle
import javax.swing.JOptionPane

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.KeyStroke
import java.awt.Toolkit

fun main(args: Array<String>) {
    createWindow("First window");
}

fun createWindow(title: String) {
    var mouseX = 0
    var mouseY = 0

    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

    // Create menu.
    val menuBar = JMenuBar()
    val menu = JMenu("File")
    menuBar.add(menu)
    val menuItem = JMenuItem("Say Hello")
    val ctrlJ = KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
    menuItem.setAccelerator(ctrlJ)
    menuItem.addActionListener(object : ActionListener {
        override fun actionPerformed(actionEvent: ActionEvent?) {
            println("Hello, World")
        }
    })

    menu.add(menuItem)
    window.setJMenuBar(menuBar)
    java.awt.Desktop.getDesktop().setDefaultMenuBar(menuBar)

    val state = State()
    state.text = title

    window.layer.renderer = Renderer { renderer, w, h -> displayScene(renderer, w, h, mouseX, mouseY, state)
    }

    window.layer.addMouseMotionListener(object : MouseMotionAdapter() {
        override fun mouseMoved(event: MouseEvent) {
            mouseX = event.x
            mouseY = event.y
            window.display()
        }
    })

    window.setVisible(true)
    // MANDATORY: set window size after calling setVisible(true)
    window.setSize(800, 600)
}

class Renderer(val displayScene: (Renderer, Int, Int) -> Unit): SkiaRenderer {
    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val font = Font(typeface, 40f)
    val paint = Paint().apply {
            setColor(0xff9BC730L.toInt())
            setMode(PaintMode.FILL)
            setStrokeWidth(1f)
    }

    var canvas: Canvas? = null

    override fun onInit() {
    }

    override fun onDispose() {
    }

    override fun onReshape(width: Int, height: Int) {
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int) {
        this.canvas = canvas
        displayScene(this, width, height)
    }
}

class State {
    var frame: Int = 0
    var text: String = "Hello Skija"
}

fun displayScene(renderer: Renderer, width: Int, height: Int, xpos: Int, ypos: Int, state: State) {
    val canvas = renderer.canvas!!
    val watchFill = Paint().setColor(0xFFFFFFFF.toInt())
    val watchStroke = Paint().setColor(0xFF000000.toInt()).setMode(PaintMode.STROKE).setStrokeWidth(1f)
    val watchStrokeAA = Paint().setColor(0xFF000000.toInt()).setMode(PaintMode.STROKE).setStrokeWidth(1f)
    val watchFillHover = Paint().setColor(0xFFE4FF01.toInt())
    for (x in 0 .. (width - 50) step 50) {
        for (y in 0 .. (height - 50) step 50) {
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
            val time = System.currentTimeMillis() % 60000 +
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

    val fontCollection = FontCollection()
            .setDefaultFontManager(FontMgr.getDefault())
    val style = ParagraphStyle()
    val paragraph = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("Text")
            .popStyle()
            .build()
    paragraph.layout(Float.POSITIVE_INFINITY)
    paragraph.paint(canvas, 0f, 0f)
}
