package SkiaAwtSample

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skiko.*
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class ClocksAwt(private val scaleProvider: () -> Float) : SkikoRenderDelegate, MouseMotionListener {
    constructor(layer: SkiaLayer) : this({ layer.contentScale })

    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val font = Font(typeface, 13f).apply {
        edging = FontEdging.SUBPIXEL_ANTI_ALIAS
        hinting = FontHinting.SLIGHT
    }
    private val paint = Paint().apply {
            color = 0xff9BC730L.toInt()
            mode = PaintMode.FILL
            strokeWidth = 1f
    }

    private var frame = 0
    private var xpos = 0
    private var ypos = 0
    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val watchFill = Paint().apply { color = 0xFFFFFFFF.toInt() }
        val watchStroke = Paint().apply {
               color = Color.RED
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
                canvas.drawLine(x + 25f, y + 25f,
                        x + 25f - 15f * sin(angle1),
                        y + 25f + 15 * cos(angle1),
                        stroke)

                val angle2 = (time / 60000 * 2f * PI).toFloat()
                canvas.drawLine(x + 25f, y + 25f,
                        x + 25f - 10f * sin(angle2),
                        y + 25f + 10f * cos(angle2),
                        stroke)
            }
        }

        val text = "Frames: ${frame++}!"
        val x = xpos.toFloat()
        val y = ypos.toFloat()
        canvas.drawString(text, x, y, font, paint)

        val style = ParagraphStyle().apply {
            fontRastrSettings = FontRastrSettings(FontEdging.SUBPIXEL_ANTI_ALIAS, FontHinting.SLIGHT, true)
        }
        val paragraph = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("JRE: ${System.getProperty("java.vendor")}, ${System.getProperty("java.runtime.version")} $currentSystemTheme")
            .popStyle()
            .build()
        paragraph.layout(Float.POSITIVE_INFINITY)
        paragraph.paint(canvas, 5f, 5f)

        // Alpha layers test
        val rectW = 100f
        val rectH = 100f
        val scale = scaleProvider()
        val left = (width / scale - rectW) / 2f
        val top = (height / scale - rectH) / 2f
        val pictureRecorder = PictureRecorder()
        val pictureCanvas = pictureRecorder.beginRecording(
            Rect.makeLTRB(left, top, left + rectW, top + rectH)
        )
        pictureCanvas.drawLine(left, top, left + rectW, top + rectH, Paint())
        val picture = pictureRecorder.finishRecordingAsPicture()
        canvas.drawPicture(picture, null, Paint())
        canvas.drawLine(left, top + rectH, left + rectW, top, Paint())
    }

    override fun mouseDragged(e: MouseEvent) {
    }

    override fun mouseMoved(e: MouseEvent) {
        xpos = e.x
        ypos = e.y
    }
}