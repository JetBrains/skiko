package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import kotlin.test.*

class ParagraphTest {
    private val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)

    @Test
    @SkipJsTarget
    @SkipNativeTarget
    fun findTypefaces() {
        fontCollection.findTypefaces(emptyArray(), FontStyle.NORMAL)
    }

    private fun singleLineMetrics(text: String): LineMetrics {
        val style = ParagraphStyle()

        return ParagraphBuilder(style, fontCollection).use {
            it.addText(text)
            it.build()
        }.layout(Float.POSITIVE_INFINITY).lineMetrics.first()
    }

    @Test
    @SkipJsTarget
    @SkipNativeTarget
    fun layoutParagraph() {
        singleLineMetrics("aa").let { lineMetrics -> // latin
            assertEquals(0, lineMetrics.startIndex)
            assertEquals(2, lineMetrics.endIndex)
            assertEquals(2, lineMetrics.endIncludingNewline)
            assertEquals(2, lineMetrics.endExcludingWhitespaces)
        }
        singleLineMetrics("яя").let { lineMetrics -> // cyrillic
            assertEquals(0, lineMetrics.startIndex)
            assertEquals(2, lineMetrics.endIndex)
            assertEquals(2, lineMetrics.endIncludingNewline)
            assertEquals(2, lineMetrics.endExcludingWhitespaces)
        }
    }

    @Test
    @SkipJsTarget
    fun canCreate() {
        val style = ParagraphStyle().apply {
            height = 40.0f
            maxLinesCount = 2
        }
        val text = "Hello,\r\n Пользователь1!"
        var paragraph = ParagraphBuilder(style, fontCollection).use {
            it.addText(text)
            it.build()
        }.layout(100.0f)

        assertCloseEnough(100.0f, paragraph.maxWidth)
        assertCloseEnough(100.78f, paragraph.minIntrinsicWidth)
        assertCloseEnough(108.55f, paragraph.maxIntrinsicWidth)
        assertCloseEnough(10.780273f, paragraph.alphabeticBaseline)
        assertCloseEnough(14.0f, paragraph.ideographicBaseline)
        assertCloseEnough(96.87891f, paragraph.longestLine)
        assertCloseEnough(28.0f, paragraph.height)
        assertTrue(paragraph.didExceedMaxLines())

        assertEquals(IRange(0, 5), paragraph.getWordBoundary(0))
        assertEquals(IRange(9, 22), paragraph.getWordBoundary(10))
        assertEquals(0, paragraph.unresolvedGlyphsCount)
        assertEquals(2, paragraph.lineNumber)

        assertContentEquals(arrayOf(), paragraph.rectsForPlaceholders)

        assertContentEquals(arrayOf(
            TextBox(Rect(3.89f, 14.0f, 96.88f, 28.0f), Direction.LTR)
        ), paragraph.getRectsForRange(9, 22, RectHeightMode.TIGHT, RectWidthMode.TIGHT))

        paragraph = paragraph
            .updateFontSize(9, 22, 48.0f)
            .updateForegroundPaint(9, 22, Paint().apply { color = Color.RED })
            .updateBackgroundPaint(9, 22, Paint().apply { color = Color.BLACK })
            .updateAlignment(Alignment.RIGHT)
            .markDirty()

        assertContentEquals(arrayOf(
            TextBox(Rect(3.89f, 14.0f, 96.88f, 28.0f), Direction.LTR)
        ), paragraph.getRectsForRange(9, 22, RectHeightMode.TIGHT, RectWidthMode.TIGHT))

        assertEquals(
            PositionWithAffinity(5, Affinity.UPSTREAM),
            paragraph.getGlyphPositionAtCoordinate(30f, 10f)
        )
    }

}