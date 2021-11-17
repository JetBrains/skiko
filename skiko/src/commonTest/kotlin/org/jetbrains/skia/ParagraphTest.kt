package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParagraphTest {
    private val fontCollection = suspend {
        FontCollection().setDefaultFontManager(TypefaceFontProvider().apply {
            registerTypeface(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), "Inter")
        })
    }
    private val style = ParagraphStyle().apply {
        height = 40.0f
        maxLinesCount = 2
        textStyle = TextStyle().apply {
            fontFamilies = arrayOf("Inter")
            fontSize = 14.0f
        }
    }

    @Test
    @SkipJsTarget
    @SkipNativeTarget
    fun findTypefaces() = runTest {
        fontCollection().findTypefaces(emptyArray(), FontStyle.NORMAL)
    }

    private suspend fun singleLineMetrics(text: String): LineMetrics {
        return ParagraphBuilder(style, fontCollection()).use {
            it.addText(text)
            it.build()
        }.layout(Float.POSITIVE_INFINITY).lineMetrics.first()
    }

    @Test
    @SkipJsTarget
    @SkipNativeTarget
    fun layoutParagraph() = runTest {
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
    fun canCreate() = runTest {
        val text = "Hello,\n Пользователь1!"
        var paragraph = ParagraphBuilder(style, fontCollection()).use {
            it.addText(text)
            it.build()
        }.layout(100.0f)

        assertCloseEnough(100.0f, paragraph.maxWidth,0.01f)
        assertCloseEnough(102.63f, paragraph.minIntrinsicWidth,0.01f)
        assertCloseEnough(110.47f, paragraph.maxIntrinsicWidth,0.01f)
        assertCloseEnough(13.5625f, paragraph.alphabeticBaseline, 0.01f)
        assertCloseEnough(16.943085f, paragraph.ideographicBaseline, 0.01f)
        assertCloseEnough(92.3125f, paragraph.longestLine,0.01f)
        assertCloseEnough(34.0f, paragraph.height)
        assertTrue(paragraph.didExceedMaxLines())

        assertEquals(IRange(0, 5), paragraph.getWordBoundary(0))
        assertEquals(IRange(8, 21), paragraph.getWordBoundary(10))
        assertEquals(0, paragraph.unresolvedGlyphsCount)
        assertEquals(2, paragraph.lineNumber)

        assertContentEquals(arrayOf(), paragraph.rectsForPlaceholders)

        assertContentCloseEnough(arrayOf(
            TextBox(Rect(3.94f, 17.06f, 92.31f, 34.0f), Direction.LTR)
        ), paragraph.getRectsForRange(8, 21, RectHeightMode.TIGHT, RectWidthMode.TIGHT),0.01f)

        paragraph = paragraph
            .updateFontSize(8, 21, 48.0f)
            .updateForegroundPaint(8, 21, Paint().apply { color = Color.RED })
            .updateBackgroundPaint(8, 21, Paint().apply { color = Color.BLACK })
            .updateAlignment(Alignment.RIGHT)
            .markDirty()

        assertContentCloseEnough(arrayOf(
            TextBox(Rect(3.94f, 17.06f, 92.31f, 34.0f), Direction.LTR)
        ), paragraph.getRectsForRange(8, 21, RectHeightMode.TIGHT, RectWidthMode.TIGHT),0.01f)

        assertEquals(
            PositionWithAffinity(5, Affinity.UPSTREAM),
            paragraph.getGlyphPositionAtCoordinate(30f, 10f)
        )
    }

}