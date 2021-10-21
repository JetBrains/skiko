package org.jetbrains.skiko

import org.jetbrains.skia.Font
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skiko.tests.runTest

class TextLineTest {
    private var inter36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("InterHinted-Regular.ttf"), 36f)
    }
    private var firaCode36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("FiraCode-Regular.ttf"), 36f)
    }
    private var jbMono36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("JetBrainsMono-Regular.ttf"), 36f)
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

        TextLine.make("ă", jbMono36()).use { line -> }
        TextLine.make("aa̧", jbMono36()).use { line ->
            // JetBrains Mono supports “a” but not “ ̧ ”
            // Second grapheme cluster should fall back together, second “a” should resolve to different glyph
            assertNotEquals(line.glyphs.get(0), line.glyphs.get(1))
        }
    }

    @Test
    fun emojiTest() = runTest {
        TextLine.make("☺", firaCode36()).use { misc ->
            TextLine.make("☺️", firaCode36()).use { emoji ->
                assertContentEquals(shortArrayOf(1706), misc.glyphs)
                assertEquals(1, emoji.glyphs.size)
                assertNotEquals(misc.glyphs.get(0), emoji.glyphs.get(0))
            }
        }
    }
}