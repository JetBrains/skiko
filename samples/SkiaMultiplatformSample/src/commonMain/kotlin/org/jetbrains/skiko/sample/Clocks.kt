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

val BLEND = BlendMode.DST_ATOP
val CLR = 0x00000000.toInt()

abstract class Clocks(private val layer: SkiaLayer): SkikoView {
    abstract val inputText:String
    abstract fun handleBackspace()

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        canvas.drawCircle(50f, 50f, 50f, Paint().apply {
            color = CLR
            blendMode = BLEND
        })
        canvas.drawRect(Rect(50f, 0f, 200f, 200f), Paint().apply {
            color = 0xcc00ffff.toInt()
        })
//        canvas.drawCircle(50f, 50f, 50f, Paint().apply {
//            color = CLR
//            blendMode = BLEND
//        })
        canvas.resetMatrix()
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
    }

    override fun onTouchEvent(events: Array<SkikoTouchEvent>) {
    }

    override fun onGestureEvent(event: SkikoGestureEvent) {

    }
}