package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private fun isMacOrIOS() = (hostOs == OS.MacOS) || (hostOs == OS.Ios)
private val COARSE_EPSILON = if (isMacOrIOS()) 2.4f else if (kotlinBackend == KotlinBackend.JS) 10e-5f else 10e-8f

class FontTests {
    @Test
    fun fontTest() = runTest {
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")
        Font(jbMono).use { font ->
            assertEquals(12f, font.size)
            // TODO: we have to use bigger epsilon because of MacOS and definitely need to investigate what would be a better solution
            assertCloseEnough(14.880001f, font.spacing, 10e-5f)

            val glyphs = font.getStringGlyphs("ABCDE")
            assertContentEquals(shortArrayOf(17, 18, 19, 20, 21), glyphs)

            assertEquals(6, font.getStringGlyphsCount("EЙ를üẞ無"))
            assertCloseEnough(49f, font.measureTextWidth("EЙ를üẞՇ無"), COARSE_EPSILON)

            assertContentCloseEnough(floatArrayOf(7f, 7f, 7f, 7f, 7f), font.getWidths(glyphs), COARSE_EPSILON)

            assertContentCloseEnough(floatArrayOf(0f, 7f, 14f, 21f, 28f), font.getXPositions(glyphs), COARSE_EPSILON)
            assertContentCloseEnough(floatArrayOf(3f, 10f, 17f, 24f, 31f), font.getXPositions(glyphs, 3f), COARSE_EPSILON)

            val firstGlyphPath = font.getPath(glyphs[0])!!
            assertContentCloseEnough(
                listOf(
                    Point(2.8798828f, -8.639648f),
                    Point(4.3916016f, -8.639648f),
                    Point(6.6708984f, 0.0f),
                    Point(5.5195312f, 0.0f),
                    Point(4.9189453f, -2.4345703f)
                ),
                // TODO: investigate why we have nullable points at all
                (firstGlyphPath.points.toList() as List<Point>).subList(0, 5),
                COARSE_EPSILON
            )

            assertContentCloseEnough(
                arrayOf(
                    Point(0f, 0f),
                    Point(7f, 0f),
                    Point(14f, 0f),
                    Point(21f, 0f),
                    Point(28f, 0f),
                ), font.getPositions(glyphs),
                COARSE_EPSILON
            )

            assertContentCloseEnough(
                arrayOf(
                    Point(3f, 2f),
                    Point(10f, 2f),
                    Point(17f, 2f),
                    Point(24f, 2f),
                    Point(31f, 2f),
                ), font.getPositions(glyphs, Point(3f, 2f)),
                COARSE_EPSILON
            )

            val expectedGlyphBounds = arrayOf(
                Rect(0.0f, -9.0f, 7.0f, 0.0f),
                Rect(1.0f, -9.0f, 7.0f, 0.0f),
                Rect(1.0f, -9.0f, 7.0f, 0.0f),
                Rect(1.0f, -9.0f, 7.0f, 0.0f),
                Rect(1.0f, -9.0f, 7.0f, 0.0f)
            )

            expectedGlyphBounds.zip(font.getBounds(glyphs)).forEach { (expected, actual) ->
                assertCloseEnough(expected, actual, COARSE_EPSILON)
            }

            if (!isMacOrIOS()) {
                assertEquals(26, firstGlyphPath.pointsCount)

                assertCloseEnough(FontMetrics(
                    -11.64f,
                    -11.64f,
                    3.2400002f,
                    3.2400002f,
                    0f,
                    7.2000003f,
                    29.460001f,
                    -20.880001f,
                    8.58f,
                    6.6000004f,
                    8.64f,
                    0.54f,
                    1.4399999f,
                    0.54f,
                    -3.8999999f
                ), font.metrics, COARSE_EPSILON)

            }


//            assertEquals(Rect(1f, -12f, 21f, 0f), font.measureText("ЕЁЫ"))

        }
    }
}