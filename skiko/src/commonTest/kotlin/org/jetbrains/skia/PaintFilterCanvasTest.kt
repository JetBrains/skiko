package org.jetbrains.skia

import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaintFilterCanvasTest {
    @Test
    fun canFilter() = runTest {
        val surface = Surface.makeRasterN32Premul(16, 16)
        val canvas = surface.canvas
        var called = false

        val canvasFilterRed = object : PaintFilterCanvas(canvas, true) {
            override fun onFilter(paint: Paint): Boolean {
                called = true
                return paint.color == Color.RED
            }
        }

        canvasFilterRed.drawRect(
            Rect(0f, 0f, 8f, 8f),
            Paint().apply { color = Color.RED }
        )

        canvasFilterRed.drawRect(
            Rect(0f, 0f, 8f, 8f),
            Paint().apply { color = Color.BLUE }
        ) // Must not be drawn

        val pixels = Bitmap.makeFromImage(surface.makeImageSnapshot())

        assertTrue(called)
        assertEquals(Color.RED, pixels.getColor(2, 2))
    }
}