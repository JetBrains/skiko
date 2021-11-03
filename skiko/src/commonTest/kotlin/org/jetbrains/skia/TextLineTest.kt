package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TextLineTest {

    private suspend fun fontInter36() =
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)

    private val eps = 0.001f // apparently only such not very small value can make tests pass in kotlin/js

    @Test
    fun canGetProperties() = runTest {
        val textLine = TextLine.make(text = "Привет!Hello!", font = fontInter36())

        // expected values were taken by running these tests on JVM

        assertContentEquals(
            expected = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326),
            actual = textLine.glyphs
        )

        assertCloseEnough(expected = -34.875f, actual = textLine.ascent, epsilon = eps)

        assertCloseEnough(expected = 8.692932f, actual = textLine.descent, epsilon = eps)

        assertCloseEnough(expected = 0f, actual = textLine.leading, epsilon = eps)

        assertCloseEnough(expected = 26.181818f, actual = textLine.capHeight, epsilon = eps)

        assertCloseEnough(expected = 19.636364f, actual = textLine.xHeight, epsilon = eps)

        assertCloseEnough(expected = 235.28409f, actual = textLine.width, epsilon = eps)

        assertCloseEnough(expected = 43.567932f, actual = textLine.height, epsilon = eps)

        assertNotEquals(null, textLine.textBlob)

        assertContentEquals(
            expected = floatArrayOf(
                0.0f, 0.0f, 26.0f, 0.0f, 48.0f, 0.0f, 69.0f, 0.0f, 89.0f, 0.0f, 109.28409f, 0.0f, 128.28409f, 0.0f,
                138.28409f, 0.0f, 165.28409f, 0.0f, 186.28409f, 0.0f, 195.28409f, 0.0f, 204.28409f, 0.0f, 225.28409f, 0.0f
            ),
            actual = textLine.positions
        )

        assertContentEquals(
            expected = floatArrayOf(0.0f, 138.28409f),
            actual = textLine.runPositions
        )

        assertContentEquals(
            expected = floatArrayOf(
                0.0f, 26.0f, 48.0f, 69.0f, 89.0f, 109.28409f, 128.28409f, 138.28409f, 138.28409f, 165.28409f,
                186.28409f, 195.28409f, 204.28409f, 225.28409f, 235.28409f
            ),
            actual = textLine.breakPositions
        )

        assertContentEquals(
            expected = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 7, 8, 9, 10, 11, 12, 13),
            actual = textLine.breakOffsets
        )

        assertEquals(expected = 0, actual = textLine.getOffsetAtCoord(0f))
        assertEquals(expected = 1, actual = textLine.getOffsetAtCoord(15f))
        assertEquals(expected = 2, actual = textLine.getOffsetAtCoord(45f))
        assertEquals(expected = 3, actual = textLine.getOffsetAtCoord(75f))
        assertEquals(expected = 4, actual = textLine.getOffsetAtCoord(90f))
        assertEquals(expected = 5, actual = textLine.getOffsetAtCoord(105f))

        assertEquals(expected = 0, actual = textLine.getLeftOffsetAtCoord(0f))
        assertEquals(expected = 1, actual = textLine.getLeftOffsetAtCoord(45f))
        assertEquals(expected = 2, actual = textLine.getLeftOffsetAtCoord(65f))
        assertEquals(expected = 3, actual = textLine.getLeftOffsetAtCoord(80f))
        assertEquals(expected = 4, actual = textLine.getLeftOffsetAtCoord(105f))
        assertEquals(expected = 5, actual = textLine.getLeftOffsetAtCoord(125f))

        assertCloseEnough(expected = 26.0f, actual = textLine.getCoordAtOffset(1), epsilon = eps)
        assertCloseEnough(expected = 48.0f, actual = textLine.getCoordAtOffset(2), epsilon = eps)
        assertCloseEnough(expected = 69.0f, actual = textLine.getCoordAtOffset(3), epsilon = eps)
    }
}
