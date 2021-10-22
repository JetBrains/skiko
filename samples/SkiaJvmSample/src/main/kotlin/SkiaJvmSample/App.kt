package SkiaJvmSample
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.SkiaWindow
import org.jetbrains.skiko.SkikoView
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities

fun main() {
    System.setProperty("skiko.renderApi", "OPENGL")

    SwingUtilities.invokeLater {
        val window = SkiaWindow()
        window.layer.skikoView = object : SkikoView {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {

            }
        }
        window.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        window.size = Dimension(200,200)
        window.isVisible = true
    }
}
