package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DrawableTest {
    @Test
    fun canDrawDrawable() = runTest {
        val cross = object : Drawable() {
            override fun onDraw(canvas: Canvas?) {
                val paint = Paint().apply { color = Color.RED }
                canvas!!.drawRect(Rect(4f, 0f, 12f, 16f), paint)
                canvas.drawRect(Rect(0f, 4f, 16f, 12f), paint)
            }

            override fun onGetBounds() = Rect(0f, 0f, 16f, 16f)
        }

        val surface = Surface.makeRasterN32Premul(16, 16)
        surface.canvas.drawDrawable(cross)
        val pixels = Bitmap.makeFromImage(surface.makeImageSnapshot())
        assertEquals(Color.RED, pixels.getColor(8, 8))
        assertEquals(Color.TRANSPARENT, pixels.getColor(2, 2))

        assertCloseEnough(Rect(0f, 0f, 16f, 16f), cross.bounds)
    }
}