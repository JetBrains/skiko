package org.jetbrains.skiko

import org.jetbrains.skia.Font
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skiko.tests.runTest

class TextLineTest {
    private val inter36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)
    }
    private val firaCode36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/FiraCode-Regular.ttf"), 36f)
    }
    private val jbMono36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf"), 36f)
    }

    @Test
    fun getOffsetAtCoordTest() = runTest {
        TextLine.make("abc", inter36()).use { line ->
            assertContentEquals(shortArrayOf(503, 574, 581), line.glyphs)
            assertEquals(0, line.getOffsetAtCoord(-10f)) // before “a”
            assertEquals(0, line.getOffsetAtCoord(0f)) // beginning of “a”
            assertEquals(0, line.getOffsetAtCoord(5f)) // left half of “a”
            assertEquals(1, line.getOffsetAtCoord(10f)) // center of “a”
            assertEquals(1, line.getOffsetAtCoord(15f)) // right half of “a”
            assertEquals(1, line.getOffsetAtCoord(20f)) // between “a” and “b”
            assertEquals(1, line.getOffsetAtCoord(25f)) // left half of “b”
            assertEquals(2, line.getOffsetAtCoord(31f)) // center of “b”
            assertEquals(2, line.getOffsetAtCoord(36f)) // right half of “b”
            assertEquals(2, line.getOffsetAtCoord(42f)) // between “b” and “c”
            assertEquals(2, line.getOffsetAtCoord(47f)) // left half of “c”
            assertEquals(3, line.getOffsetAtCoord(52f)) // center of “c”
            assertEquals(3, line.getOffsetAtCoord(57f)) // right half of “c”
            assertEquals(3, line.getOffsetAtCoord(62f)) // end of “c”
            assertEquals(3, line.getOffsetAtCoord(100f)) // after “c”
        }
    }

    @Test
    fun getLeftOffsetAtCoordTest() = runTest {
        TextLine.make("abc", inter36()).use { line ->
            assertEquals(0, line.getLeftOffsetAtCoord(-10f)) // before “a”
            assertEquals(0, line.getLeftOffsetAtCoord(0f)) // beginning of “a”
            assertEquals(0, line.getLeftOffsetAtCoord(5f)) // left half of “a”
            assertEquals(0, line.getLeftOffsetAtCoord(10f)) // center of “a”
            assertEquals(0, line.getLeftOffsetAtCoord(15f)) // right half of “a”
            assertEquals(1, line.getLeftOffsetAtCoord(20f)) // between “a” and “b”
            assertEquals(1, line.getLeftOffsetAtCoord(25f)) // left half of “b”
            assertEquals(1, line.getLeftOffsetAtCoord(31f)) // center of “b”
            assertEquals(1, line.getLeftOffsetAtCoord(36f)) // right half of “b”
            assertEquals(2, line.getLeftOffsetAtCoord(42f)) // between “b” and “c”
            assertEquals(2, line.getLeftOffsetAtCoord(47f)) // left half of “c”
            assertEquals(2, line.getLeftOffsetAtCoord(52f)) // center of “c”
            assertEquals(2, line.getLeftOffsetAtCoord(57f)) // right half of “c”
            assertEquals(3, line.getLeftOffsetAtCoord(62f)) // end of “c”
            assertEquals(3, line.getLeftOffsetAtCoord(100f)) // after “c”
        }
    }

    @Test
    fun getCoordAtOffsetTest() = runTest {
        TextLine.make("abc", inter36()).use { line ->
            assertCloseEnough(0f, line.getCoordAtOffset(0))
            assertCloseEnough(20f, line.getCoordAtOffset(1))
            assertCloseEnough(42f, line.getCoordAtOffset(2))
            assertCloseEnough(62f, line.getCoordAtOffset(3))
        }
    }

    @Test
    fun ligaturesTest() = runTest {
        TextLine.make("<=>->", inter36()).use { line ->
            assertContentEquals(shortArrayOf(1712, 1701), line.glyphs)

            assertEquals(0, line.getOffsetAtCoord(0f))
            assertEquals(1, line.getOffsetAtCoord(16f))
            assertEquals(2, line.getOffsetAtCoord(32f))
            assertEquals(3, line.getOffsetAtCoord(48f))
            assertEquals(4, line.getOffsetAtCoord(65f))
            assertEquals(5, line.getOffsetAtCoord(82f))

            assertCloseEnough(0f, line.getCoordAtOffset(0))
            assertCloseEnough(16f, line.getCoordAtOffset(1))
            assertCloseEnough(32f, line.getCoordAtOffset(2))
            assertCloseEnough(48f, line.getCoordAtOffset(3))
            assertCloseEnough(65f, line.getCoordAtOffset(4))
            assertCloseEnough(82f, line.getCoordAtOffset(5))
        }
    }

    @Test
    fun combiningTest() = runTest {
        // u   U+0075  LATIN SMALL LETTER U
        // ̈    U+0308  COMBINING DIAERESIS
        // a   U+0061  LATIN SMALL LETTER A
        // ̧    U+0327  COMBINING CEDILLA
        assertEquals("üa̧", ManagedString("üa̧").toString())
        TextLine.make("üa̧", inter36()).use { line ->
            assertContentEquals(shortArrayOf(898 /* ü */, 503 /* a */, 1664 /* ̧  */), line.glyphs)

            assertEquals(0, line.getOffsetAtCoord(0f))
            assertEquals(0, line.getOffsetAtCoord(10f))
            assertEquals(2, line.getOffsetAtCoord(12f))
            assertEquals(2, line.getOffsetAtCoord(21f))
            assertEquals(2, line.getOffsetAtCoord(30f))
            assertEquals(4, line.getOffsetAtCoord(32f))
            assertEquals(4, line.getOffsetAtCoord(41f))

            assertCloseEnough(0f, line.getCoordAtOffset(0))
            assertCloseEnough(0f, line.getCoordAtOffset(1))
            assertCloseEnough(21f, line.getCoordAtOffset(2))
            assertCloseEnough(41f, line.getCoordAtOffset(3))
            assertCloseEnough(41f, line.getCoordAtOffset(4))
        }

        assertEquals("ă", ManagedString("ă").toString())
        assertEquals("aa̧", ManagedString("aa̧").toString())

        TextLine.make("ă", jbMono36()).use { line -> }
        TextLine.make("aa̧", jbMono36()).use { line ->
            assertEquals(42, line.glyphs[0])
            // JetBrains Mono supports “a” but not “ ̧ ”
            // Second grapheme cluster should fall back together, second “a” should resolve to different glyph
            if (kotlinBackend.isNotJs()) { // TODO(karpovich): figure out why js gives different result
                assertNotEquals(line.glyphs[0], line.glyphs[1])
            }
        }
    }

    @Test
    fun emojiTest() = runTest {
        assertEquals("☺", ManagedString("☺").toString())
        assertEquals("☺️", ManagedString("☺️").toString())

        TextLine.make("☺", firaCode36()).use { misc ->
            TextLine.make("☺️", firaCode36()).use { emoji ->
                assertContentEquals(shortArrayOf(1706), misc.glyphs)
                assertEquals(1, emoji.glyphs.size)
                if (kotlinBackend.isNotJs()) { // TODO(karpovich): try with a FontMngr without fallbacks
                    assertNotEquals(misc.glyphs[0], emoji.glyphs[0])
                }
            }
        }
    }


    private val eps = 0.001f // apparently only such not very small value can make tests pass in kotlin/js

    @Test
    fun canGetProperties() = runTest {
        val textLine = TextLine.make(text = "Привет!Hello!", font = inter36())

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
