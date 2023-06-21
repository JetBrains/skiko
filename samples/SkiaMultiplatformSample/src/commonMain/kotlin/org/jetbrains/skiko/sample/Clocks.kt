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

abstract class Clocks(private val layer: SkiaLayer): SkikoView {
    private val cursorManager = CursorManager()
    private val withFps = true
    private val fpsCounter = FPSCounter()
    private val platformYOffset = if (hostOs == OS.Ios) 50f else 5f
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
    private val style = ParagraphStyle()

    abstract val inputText:String
    abstract fun handleBackspace()

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
            .addText("Graphics API: ${layer.renderApi} ✿ﾟ ${currentSystemTheme}${maybeFps}")
            .popStyle()
            .build()
        renderInfo.layout(Float.POSITIVE_INFINITY)
        renderInfo.paint(canvas, 5f, platformYOffset)

        val input = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("TextInput: $inputText")
            .popStyle()
            .build()
        input.layout(Float.POSITIVE_INFINITY)
        input.paint(canvas, 5f, platformYOffset + 20f)
        
        val frames = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xff9BC730L.toInt()).setFontSize(20f))
            .addText("Frames: ${frame++}\nAngle: $rotate")
            .popStyle()
            .build()
        frames.layout(Float.POSITIVE_INFINITY)
        frames.paint(canvas, ((xpos - xOffset) / scale).toFloat(), ((ypos - yOffset) / scale).toFloat())

        canvas.resetMatrix()
    }

    private fun reset() {
        xOffset = 0.0
        yOffset = 0.0
        rotate = 0.0
        scale = 1.0
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {
        when (event.kind) {
            SkikoPointerEventKind.DOWN,
            SkikoPointerEventKind.MOVE -> {
                if (event.x > 200) {
                    cursorManager.setCursor(layer.component, PredefinedCursors.HAND)
                } else {
                    cursorManager.setCursor(layer.component, PredefinedCursors.DEFAULT)
                }
                xpos = event.x
                ypos = event.y
            }
            SkikoPointerEventKind.DRAG -> {
                xOffset += event.x - xpos
                yOffset += event.y - ypos
                xpos = event.x
                ypos = event.y
            }
            SkikoPointerEventKind.SCROLL -> {
                when (event.modifiers) {
                    SkikoInputModifiers.CONTROL -> {
                        rotate += if (event.deltaY < 0) -5.0 else 5.0
                    }
                    else -> {
                        if (event.y != 0.0) {
                            scale *= if (event.deltaY < 0) 0.9 else 1.1
                        }
                    }
                }
            }
            else -> {}
        }
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
        if (event.kind == SkikoKeyboardEventKind.DOWN) {
            when (event.key) {
                SkikoKey.KEY_NUMPAD_ADD -> scale *= 1.1
                SkikoKey.KEY_I -> {
                    if (event.modifiers == SkikoInputModifiers.SHIFT) {
                        scale *= 1.1
                    }
                }
                SkikoKey.KEY_NUMPAD_SUBTRACT -> scale *= 0.9
                SkikoKey.KEY_O -> {
                    if (event.modifiers == SkikoInputModifiers.SHIFT) {
                        scale *= 0.9
                    }
                }
                SkikoKey.KEY_R -> {
                    if (event.modifiers == SkikoInputModifiers.SHIFT) {
                        rotate -= 5.0
                    }
                }
                SkikoKey.KEY_L -> {
                    if (event.modifiers == SkikoInputModifiers.SHIFT) {
                        rotate += 5.0
                    }
                }
                SkikoKey.KEY_NUMPAD_4,
                SkikoKey.KEY_LEFT -> xOffset -= 5.0
                SkikoKey.KEY_NUMPAD_8,
                SkikoKey.KEY_UP -> yOffset -= 5.0
                SkikoKey.KEY_NUMPAD_6,
                SkikoKey.KEY_RIGHT -> xOffset += 5.0
                SkikoKey.KEY_NUMPAD_2,
                SkikoKey.KEY_DOWN -> yOffset += 5.0
                SkikoKey.KEY_SPACE -> { reset() }
                SkikoKey.KEY_BACKSPACE -> {
                    handleBackspace()
                }
                else -> {}
            }
        }
    }
    
    override fun onGestureEvent(event: SkikoGestureEvent) {
        when (event.kind) {
            SkikoGestureEventKind.TAP -> {
                xpos = event.x
                ypos = event.y
            }
            SkikoGestureEventKind.DOUBLETAP -> { reset() }
            SkikoGestureEventKind.PINCH -> {
                if (event.state == SkikoGestureEventState.STARTED) {
                    k = scale
                }
                scale = k * event.scale
            }
            SkikoGestureEventKind.PAN -> {
                xOffset += event.x - xpos
                yOffset += event.y - ypos
                xpos = event.x
                ypos = event.y
            }
            SkikoGestureEventKind.ROTATION -> {
                rotate = event.rotation * 180.0 / PI
            }
            else -> {}
        }
    }
}