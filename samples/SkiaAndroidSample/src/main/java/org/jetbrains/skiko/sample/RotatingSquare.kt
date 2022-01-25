package org.jetbrains.skiko.sample

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.SkikoInputEvent
import org.jetbrains.skiko.SkikoKeyboardEvent
import org.jetbrains.skiko.SkikoPointerEvent
import org.jetbrains.skiko.SkikoView

class RotatingSquare : SkikoView {
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val angleDeg = (nanoTime / 5_000_000) % 360
        val paint = Paint().apply { color = Color.GREEN }
        canvas.clear(Color.RED)
        canvas.save();
        canvas.translate(128.0f, 128.0f)
        canvas.rotate(angleDeg.toFloat())
        val rect = Rect.makeXYWH(-90.5f, -90.5f, 181.0f, 181.0f)
        canvas.drawRect(rect, paint)
        canvas.restore()
    }

    override fun onInputEvent(event: SkikoInputEvent) {
        println("onInput: $event")
    }

    override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
        println("onKeyboard: $event")
    }

    override fun onPointerEvent(event: SkikoPointerEvent) {
        println("onMouse: $event")
    }
}
