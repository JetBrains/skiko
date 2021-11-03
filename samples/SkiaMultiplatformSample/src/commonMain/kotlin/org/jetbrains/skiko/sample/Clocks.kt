package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.SkikoView
import org.jetbrains.skiko.SkikoPointerEvent
import org.jetbrains.skiko.SkikoGestureEvent
import org.jetbrains.skiko.SkikoGestureEventKind
import org.jetbrains.skiko.SkikoGestureEventState
import org.jetbrains.skiko.isLeftClick
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class Clocks: SkikoView {
    private var frame = 0
    private var xpos = 0.0f
    private var ypos = 0.0f
    private var xOffset = 0.0f
    private var yOffset = 0.0f
    private var scale = 1.0f
    private var rotate = 0.0f
    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)

    override fun onRender(canvas: Canvas, w: Int, h: Int, nanoTime: Long) {
        val xOffset = xOffset
        val yOffset = yOffset
        val xpos = xpos * scale + xOffset * scale
        val ypos = ypos * scale + yOffset * scale
        val width = (w * scale).toInt() + xOffset.toInt()
        val height = (h * scale).toInt() + yOffset.toInt()
        val watchFill = Paint().apply { color = 0xFFFFFFFF.toInt() }
        val watchStroke = Paint().apply {
               color = 0xFF000000.toInt()
               mode = PaintMode.STROKE
               strokeWidth = 1f * scale
        }
        val watchStrokeAA = Paint().apply {
          color = 0xFF000000.toInt()
          mode = PaintMode.STROKE
          strokeWidth = 1f * scale
        }
        val watchFillHover = Paint().apply { color = 0xFFE4FF01.toInt() }
        val clockSize = 40f * scale
        val clockOffset = 5f * scale
        for (x in xOffset.toInt() .. (width - (50 * scale).toInt()) step (50 * scale).toInt()) {
            for (y in yOffset.toInt() .. (height - (50 * scale).toInt()) step (50 * scale).toInt()) {
                val hover = xpos / scale > x && xpos / scale < x + (50 * scale).toInt() && ypos / scale > y && ypos / scale < y + (50 * scale).toInt()
                val fill = if (hover) watchFillHover else watchFill
                val stroke = if (x > width / 2) watchStrokeAA else watchStroke
                canvas.drawOval(Rect.makeXYWH(x + clockOffset, y + clockOffset, clockSize, clockSize), fill)
                canvas.drawOval(Rect.makeXYWH(x + clockOffset, y + clockOffset, clockSize, clockSize), stroke)
                var angle = 0f
                while (angle < 2f * PI) {
                    canvas.drawLine(
                            (x + 25 * scale - 17 * sin(angle) * scale),
                            (y + 25 * scale + 17 * cos(angle) * scale),
                            (x + 25 * scale - 20 * sin(angle) * scale),
                            (y + 25 * scale + 20 * cos(angle) * scale),
                            stroke
                    )
                    angle += (2.0 * PI / 12.0).toFloat()
                }
                val time = (nanoTime / 1E6) % 60000 +
                        (x.toFloat() / width * 5000).toLong() +
                        (y.toFloat() / width * 5000).toLong()

                val angle1 = (time.toFloat() / 5000 * 2f * PI).toFloat()
                canvas.drawLine(
                        x + 25f * scale,
                        y + 25f * scale,
                        x + 25f * scale - 15f * sin(angle1) * scale,
                        y + 25f * scale + 15 * cos(angle1) * scale,
                        stroke)

                val angle2 = (time / 60000 * 2f * PI).toFloat()
                canvas.drawLine(
                        x + 25f * scale,
                        y + 25f * scale,
                        x + 25f * scale - 10f * sin(angle2) * scale,
                        y + 25f * scale + 10f * cos(angle2) * scale,
                        stroke)
            }
        }

        val style = ParagraphStyle()
        val frames = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xff9BC730L.toInt()).setFontSize(20f))
            .addText("Frames: ${frame++}\nAngle: $rotate")
            .popStyle()
            .build()
        frames.layout(Float.POSITIVE_INFINITY)
        frames.paint(canvas, xpos / scale, ypos / scale)
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {
        if (event.isLeftClick) {
            xpos = event.x.toFloat()
            ypos = event.y.toFloat()
        }
    }

    override fun onGestureEvent(event: SkikoGestureEvent) {
        when (event.kind) {
            SkikoGestureEventKind.PRESS,
            SkikoGestureEventKind.TAP -> {
                xpos = event.x.toFloat() - xOffset
                ypos = event.y.toFloat() - yOffset
            }
            SkikoGestureEventKind.PINCH -> {
                scale = event.scale.toFloat()
            }
            SkikoGestureEventKind.PAN -> {
                xOffset = event.x.toFloat() - xpos.toFloat()
                yOffset = event.y.toFloat() - ypos.toFloat()
            }
            SkikoGestureEventKind.ROTATION -> {
                rotate = (event.rotation * PI).toFloat()
            }
            else -> {}
        }
    }
}