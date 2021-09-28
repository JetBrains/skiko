package org.jetbrains.skiko.skija

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Paint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PaintTest {
    @Test
    fun paintTest() {
        val paintA = Paint().apply { color = 0x12345678 }
        val paintB = Paint().apply { color = 0x12345678 }

        assertEquals(paintA, paintB)
        assertEquals(paintA.hashCode(), paintB.hashCode())

        val paintC = Paint().apply { color = -0xcba988 }
        assertEquals(paintA, paintC)
        assertEquals(paintA.hashCode(), paintC.hashCode())

        assertNotEquals(Paint(), Paint().apply { isAntiAlias = false })
        assertNotEquals(Paint(), Paint().apply { isDither = true })

        Paint().use { paint ->
            paint.color = 0x12345678
            assertEquals(false, paint == paint.makeClone())
            assertEquals(paint, paint.makeClone())
            assertNotEquals(paint.hashCode(), paint.makeClone().hashCode())
        }

        Paint().use { paint ->
            assertEquals(false, paint.hasNothingToDraw())
            paint.blendMode = BlendMode.DST
            assertEquals(true, paint.hasNothingToDraw())
            paint.blendMode = BlendMode.SRC_OVER
            assertEquals(false, paint.hasNothingToDraw())
            paint.alpha = 0
            assertEquals(true, paint.hasNothingToDraw())
        }
    }
}