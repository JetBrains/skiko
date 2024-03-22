package org.jetbrains.skiko.sample

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.FPSCounter
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.currentSystemTheme
import org.jetbrains.skiko.hostOs
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

abstract class Clocks(private val renderApi: GraphicsApi): SkikoRenderDelegate {
    private val withFps = true
    private val fpsCounter = FPSCounter()
    private val platformYOffset = if (hostOs == OS.Ios) 50f else 5f
    private var frame = 0

    var xpos = 0.0
    var ypos = 0.0
    var xOffset = 0.0
    var yOffset = 0.0
    var scale = 1.0
    var rotate = 0.0

    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)
    private val style = ParagraphStyle()

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        if (withFps) fpsCounter.tick()
        canvas.translate(xOffset.toFloat(), yOffset.toFloat())
        canvas.scale(scale.toFloat(), scale.toFloat())
        canvas.rotate(rotate.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
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
        for (x in 0 .. width - 50 step 50) {
            for (y in 30 + platformYOffset.toInt() .. height - 50 step 50) {
                val hover =
                    (xpos - xOffset) / scale > x &&
                    (xpos - xOffset) / scale < x + 50 &&
                    (ypos - yOffset) / scale > y &&
                    (ypos - yOffset) / scale < y + 50
                val fill = if (hover) watchFillHover else watchFill
                val stroke = if (x > width / 2) watchStrokeAA else watchStroke
                canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), fill)
                canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), stroke)
                var angle = 0f
                while (angle < 2f * PI) {
                    canvas.drawLine(
                            (x + 25 - 17 * sin(angle)),
                            (y + 25 + 17 * cos(angle)),
                            (x + 25 - 20 * sin(angle)),
                            (y + 25 + 20 * cos(angle)),
                            stroke
                    )
                    angle += (2.0 * PI / 12.0).toFloat()
                }
                val time = (nanoTime / 1E6) % 60000 +
                        (x.toFloat() / width * 5000).toLong() +
                        (y.toFloat() / width * 5000).toLong()

                val angle1 = (time.toFloat() / 5000 * 2f * PI).toFloat()
                canvas.drawLine(
                        x + 25f,
                        y + 25f,
                        x + 25f - 15f * sin(angle1),
                        y + 25f + 15 * cos(angle1),
                        stroke)

                val angle2 = (time / 60000 * 2f * PI).toFloat()
                canvas.drawLine(
                        x + 25f,
                        y + 25f,
                        x + 25f - 10f * sin(angle2),
                        y + 25f + 10f * cos(angle2),
                        stroke)
            }
        }

        val maybeFps = if (withFps) " ${fpsCounter.average}FPS " else ""

        val renderInfo = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("Graphics API: $renderApi ✿ﾟ ${currentSystemTheme}${maybeFps}")
            .popStyle()
            .build()
        renderInfo.layout(Float.POSITIVE_INFINITY)
        renderInfo.paint(canvas, 5f, platformYOffset)

        val frames = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xff9BC730L.toInt()).setFontSize(20f))
            .addText("Frames: ${frame++}\nAngle: $rotate")
            .popStyle()
            .build()
        frames.layout(Float.POSITIVE_INFINITY)
        frames.paint(canvas, ((xpos - xOffset) / scale).toFloat(), ((ypos - yOffset) / scale).toFloat())

        canvas.resetMatrix()
    }
}
