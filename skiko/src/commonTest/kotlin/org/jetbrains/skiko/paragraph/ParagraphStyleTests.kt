package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.HeightMode
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.StrutStyle
import org.jetbrains.skia.paragraph.TextIndent
import org.jetbrains.skia.paragraph.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ParagraphStyleTests {

    @Test
    fun alignmentResolvesAgainstDirection() {
        ParagraphStyle().use { style ->
            style.alignment = Alignment.START
            style.direction = Direction.LTR
            assertEquals(Alignment.START, style.alignment)
            assertEquals(Direction.LTR, style.direction)
            assertEquals(Alignment.LEFT, style.effectiveAlignment)

            style.direction = Direction.RTL
            assertEquals(Direction.RTL, style.direction)
            assertEquals(Alignment.RIGHT, style.effectiveAlignment)

            style.alignment = Alignment.END
            assertEquals(Alignment.END, style.alignment)
            assertEquals(Alignment.LEFT, style.effectiveAlignment)
        }
    }

    @Test
    fun heightAndLineLimitRoundTrip() {
        ParagraphStyle().use { style ->
            style.height = 1.5f
            style.maxLinesCount = 3
            assertEquals(1.5f, style.height)
            assertEquals(3, style.maxLinesCount)
            for (mode in HeightMode.entries) {
                style.heightMode = mode
                assertEquals(mode, style.heightMode)
            }
        }
    }

    @Test
    fun stylesAreCopiedIntoParagraphStyle() {
        ParagraphStyle().use { style ->
            StrutStyle().use { strut ->
                strut.fontSize = 24f
                style.strutStyle = strut
                strut.fontSize = 36f
                style.strutStyle.use { copy ->
                    assertEquals(24f, copy.fontSize)
                }
            }
            TextStyle().use { text ->
                text.fontSize = 18f
                style.textStyle = text
                text.fontSize = 30f
                style.textStyle.use { copy ->
                    assertEquals(18f, copy.fontSize)
                }
            }
        }
    }

    @Test
    fun equalityTracksStyleChanges() {
        ParagraphStyle().use { first ->
            ParagraphStyle().use { second ->
                assertEquals(first, second)
                second.maxLinesCount = 2
                assertNotEquals(first, second)
                first.maxLinesCount = 2
                assertEquals(first, second)
            }
        }
    }

    @Test
    fun hintingCanBeDisabled() {
        ParagraphStyle().use { style ->
            assertTrue(style.isHintingEnabled)
            assertSame(style, style.disableHinting())
            assertFalse(style.isHintingEnabled)
        }
    }

    @Test
    fun paragraphStyleEllipsisTests() {
        ParagraphStyle().use { paragraphStyle ->
            assertNull(paragraphStyle.ellipsis)

            paragraphStyle.ellipsis = ".^."
            assertEquals(".^.", paragraphStyle.ellipsis)
        }
    }

    @Test
    fun paragraphStyleTextIndentTests() {
        ParagraphStyle().use { paragraphStyle ->
            assertEquals(TextIndent(), paragraphStyle.textIndent)
            val indent = TextIndent(20f, 10f)
            paragraphStyle.textIndent = indent
            assertEquals(indent, paragraphStyle.textIndent)

            val indent2 = TextIndent(-20f, -10f)
            paragraphStyle.textIndent = indent2
            assertEquals(indent2, paragraphStyle.textIndent)
        }
    }

    @Test
    fun paragraphStyleRoundingHackTests() {
        ParagraphStyle().use { paragraphStyle ->
            assertEquals(false, paragraphStyle.isApplyRoundingHackEnabled)
            paragraphStyle.isApplyRoundingHackEnabled = true
            assertEquals(true, paragraphStyle.isApplyRoundingHackEnabled)
        }
    }
}
