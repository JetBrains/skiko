package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.*
import org.jetbrains.skiko.data.*

class TextInput(val getText: () -> String) : SkikoView {
    private val cursorManager = CursorManager()
    private val platformYOffset = if (hostOs == OS.Ios) 50f else 5f
    private var frame = 0
    private var xOffset = 0.0
    private var yOffset = 0.0
    private var scale = 1.0
    private var rotate = 0.0
    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)
    private val style = ParagraphStyle()

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val input = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("getText(): " + getText())
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
        frames.paint(canvas, ((xOffset) / scale).toFloat(), ((yOffset) / scale).toFloat())

        canvas.resetMatrix()
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {

    }

    override fun onInputEvent(event: SkikoInputEvent) {

    }

    override val input: SkikoInput = object : SkikoInput {
        override fun hasText(): Boolean = true
        override fun insertText(text: String) {}
        override fun deleteBackward() {}
        override fun endOfDocument(): Long = 10L
        override fun selectedTextRange(): SkikoTextRange? = null
        override fun textInRange(range: SkikoTextRange): String? = "qwe"
        override fun replaceRange(range: SkikoTextRange, text: String){}
        override fun setMarkedText(markedText: String?, selectedRange: SkikoTextRange){}
        override fun markedTextRange(): SkikoTextRange? = null
        override fun unmarkText() {}
        override fun firstRectForRange(range: SkikoTextRange): SkikoRect? = null
        override fun caretRectForPosition(position: Long): SkikoRect? = null
        override fun selectionRectsForRange(range: SkikoTextRange): List<SkikoRect> = emptyList()
        override fun closestPositionToPoint(point: SkikoPoint):Int = 0
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
        if (event.kind == SkikoKeyboardEventKind.DOWN) {
            when (event.key) {
                SkikoKey.KEY_BACKSPACE -> {

                }

                else -> {}
            }
        }
    }

    override fun onTouchEvent(events: Array<SkikoTouchEvent>) {

    }

    override fun onGestureEvent(event: SkikoGestureEvent) {

    }
}