package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test

class PaintTest {
    @Test
    fun color4f() {
        val paint = Paint()
        val expected = Color4f(0.2f, 0.4f, 0.8f, 1.0f)
        paint.setColor4f(expected, colorSpace = ColorSpace.sRGB)
        assertCloseEnough(expected, paint.color4f)
    }
}