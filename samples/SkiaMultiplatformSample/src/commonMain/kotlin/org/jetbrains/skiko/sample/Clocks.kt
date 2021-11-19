package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.pow

class Clocks(private val layer: SkiaLayer): SkikoView {
    private var frame = 0
    private var xpos = 0.0
    private var ypos = 0.0
    private var xOffset = 0.0
    private var yOffset = 0.0
    private var scale = 1.0
    private var k = scale
    private var rotate = 0.0
    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        canvas.translate(xOffset.toFloat(), yOffset.toFloat())
        canvas.scale(scale.toFloat(), scale.toFloat())
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
            for (y in 20 .. height - 50 step 50) {
                val hover = xpos / scale > x && xpos / scale < x + 50 && ypos / scale > y && ypos / scale < y + 50
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

        val style = ParagraphStyle()
        val renderInfo = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("Graphics API: ${layer.renderApi} ✿ﾟ ${currentSystemTheme}")
            .popStyle()
            .build()
        renderInfo.layout(Float.POSITIVE_INFINITY)
        renderInfo.paint(canvas, 5f, 5f)
        val frames = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xff9BC730L.toInt()).setFontSize(20f))
            .addText("Frames: ${frame++}\nAngle: $rotate")
            .popStyle()
            .build()
        frames.layout(Float.POSITIVE_INFINITY)
        frames.paint(canvas, (xpos / scale).toFloat(), (ypos / scale).toFloat())
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {
        when (event.kind) {
            SkikoPointerEventKind.DOWN,
            SkikoPointerEventKind.MOVE -> {
                xpos = event.x
                ypos = event.y
            }
            else -> {}
        }
        // TODO: provide example that covers all features of pointer event
    }

    override fun onInputEvent(event: SkikoInputEvent) {
        // TODO: provide example that covers all features of text input event
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
        println(event.kind)
        // TODO: provide example that covers all features of keyboard event
    }

    override fun onGestureEvent(event: SkikoGestureEvent) {
        when (event.kind) {
            SkikoGestureEventKind.PRESS,
            SkikoGestureEventKind.TAP -> {
                xpos = event.x - xOffset
                ypos = event.y - yOffset
            }
            SkikoGestureEventKind.PINCH -> {
                if (event.state == SkikoGestureEventState.STARTED) {
                    k = scale
                }
                scale = k * event.scale
            }
            SkikoGestureEventKind.PAN -> {
                xOffset = event.x - xpos
                yOffset = event.y - ypos
            }
            SkikoGestureEventKind.ROTATION -> {
                rotate = (event.rotation * PI)
            }
            else -> {}
        }
    }
}