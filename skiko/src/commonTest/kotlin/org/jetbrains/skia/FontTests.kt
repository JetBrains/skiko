package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FontTests {
    @Test
    fun fontTest() = runTest {
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")
        Font(jbMono).use { font ->
            assertEquals(12f, font.size)
            assertCloseEnough(14.880001f, font.spacing)

            val glyphs = font.getStringGlyphs("ABCDE")
            assertContentEquals(shortArrayOf(17, 18, 19, 20, 21), glyphs)

            assertContentEquals(floatArrayOf(7f, 7f, 7f, 7f, 7f), font.getWidths(glyphs))

            assertContentEquals(floatArrayOf(0f, 7f, 14f, 21f, 28f), font.getXPositions(glyphs))
            assertContentEquals(floatArrayOf(3f, 10f, 17f, 24f, 31f), font.getXPositions(glyphs, 3f))

            assertContentEquals(
                arrayOf(
                    Point(0f, 0f),
                    Point(7f, 0f),
                    Point(14f, 0f),
                    Point(21f, 0f),
                    Point(28f, 0f),
                ), font.getPositions(glyphs)
            )

            assertContentEquals(
                arrayOf(
                    Point(3f, 2f),
                    Point(10f, 2f),
                    Point(17f, 2f),
                    Point(24f, 2f),
                    Point(31f, 2f),
                ), font.getPositions(glyphs, Point(3f, 2f))
            )

            val firstGlyphPath = font.getPath(glyphs[0])!!
            assertEquals(26, firstGlyphPath.pointsCount)
            assertContentEquals(
                listOf(
                    Point(2.8798828f, -8.639648f),
                    Point(4.3916016f, -8.639648f),
                    Point(6.6708984f, 0.0f),
                    Point(5.5195312f, 0.0f),
                    Point(4.9189453f, -2.4345703f)
                ),
                firstGlyphPath.points.toList().subList(0, 5)
            )
        }
    }
}