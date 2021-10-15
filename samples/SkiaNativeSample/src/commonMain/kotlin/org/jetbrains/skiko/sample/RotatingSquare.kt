package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skiko.SkiaRenderer

class RotatingSquare : SkiaRenderer {
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
}
