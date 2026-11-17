package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.assertCloseEnough

class DrawableTest {
    @Test
    fun canDrawDrawable() {
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

    @Test
    fun drawingChangesInvalidateGenerationButPreserveSnapshots() {
        var color = Color.RED
        val drawable = object : Drawable() {
            override fun onDraw(canvas: Canvas?) {
                Paint().use { paint ->
                    paint.color = color
                    canvas!!.drawRect(Rect(0f, 0f, 4f, 4f), paint)
                }
            }

            override fun onGetBounds() = Rect(0f, 0f, 4f, 4f)
        }
        drawable.use {
            val generation = drawable.generationId
            assertNotEquals(0, generation)
            assertEquals(generation, drawable.generationId)
            drawable.makePictureSnapshot().use { snapshot ->
                color = Color.BLUE
                drawable.notifyDrawingChanged()
                assertNotEquals(generation, drawable.generationId)
                Surface.makeRasterN32Premul(8, 4).use { surface ->
                    snapshot.playback(surface.canvas)
                    drawable.draw(surface.canvas, 4f, 0f)
                    surface.makeImageSnapshot().use { image ->
                        Bitmap.makeFromImage(image).use { pixels ->
                            assertEquals(Color.RED, pixels.getColor(1, 1))
                            assertEquals(Color.BLUE, pixels.getColor(5, 1))
                        }
                    }
                }
            }
        }
    }
}
