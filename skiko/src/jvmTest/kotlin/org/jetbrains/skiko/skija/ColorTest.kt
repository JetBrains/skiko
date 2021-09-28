package org.jetbrains.skiko.skija

import org.jetbrains.skia.Color
import org.jetbrains.skia.Color4f
import kotlin.test.Test

import kotlin.test.assertEquals

class ColorTest {
    @Test
    fun testColor() {
        val cases = mapOf(
        0x00000000 to Color4f(0f, 0f, 0f, 0f),
        -0x1000000 to Color4f(0f, 0f, 0f, 1f),
        0x00FF0000 to Color4f(1f, 0f, 0f, 0f),
        0x0000FF00 to Color4f(0f, 1f, 0f, 0f),
        0x000000FF to Color4f(0f, 0f, 1f, 0f),
        -0x7f7f7f80 to Color4f(128 / 255f, 128 / 255f, 128 / 255f, 128 / 255f)
        )

        cases.forEach { (value, color) ->
            assertEquals(value, color.toColor(), "$value <-> $color")
            assertEquals(Color4f(value), color, "Color($value) <-> $color")
        }

        assertEquals(0x12, Color.getA(0x12345678))
        assertEquals(0x34, Color.getR(0x12345678))
        assertEquals(0x56, Color.getG(0x12345678))
        assertEquals(0x78, Color.getB(0x12345678))
        assertEquals(-0x1cba988, Color.withA(0x12345678, 0xFE))
        assertEquals(0x12FE5678, Color.withR(0x12345678, 0xFE))
        assertEquals(0x1234FE78, Color.withG(0x12345678, 0xFE))
        assertEquals(0x123456FE, Color.withB(0x12345678, 0xFE))
    }
}