package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import org.jetbrains.skiko.tests.SkipWasmTarget
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class ParagraphTest {
    @Test
    fun paintRendersTextIntoRasterCanvas() = runTest {
        fontCollection().use { collection ->
            ParagraphStyle().use { paragraphStyle ->
                TextStyle().use { textStyle ->
                    textStyle.fontFamilies = arrayOf("Inter")
                    textStyle.fontSize = 32f
                    textStyle.color = Color.RED
                    paragraphStyle.textStyle = textStyle
                }
                ParagraphBuilder(paragraphStyle, collection).use { builder ->
                    builder.addText("Hello").build()
                }.use { paragraph ->
                    paragraph.layout(160f)
                    Bitmap().use { bitmap ->
                        assertTrue(bitmap.allocPixels(ImageInfo.makeN32Premul(180, 80)))
                        Canvas(bitmap).use { canvas ->
                            canvas.clear(Color.WHITE)
                            assertSame(paragraph, paragraph.paint(canvas, 12f, 8f))
                        }
                        assertEquals(Color.WHITE, bitmap.getColor(0, 0))
                        assertTrue((8 until 80).any { y ->
                            (12 until 180).any { x -> bitmap.getColor(x, y) == Color.RED }
                        }, "Painting the paragraph should produce red text pixels")
                    }
                }
            }
        }
    }

    private val fontCollection = suspend {
        TypefaceFontProvider().use { manager ->
            Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf").use { typeface ->
                manager.registerTypeface(typeface, "Inter")
            }
            Typeface.makeFromResource("./fonts/JetBrainsMono_2_304/JetBrainsMono-Regular.ttf").use { typeface ->
                manager.registerTypeface(typeface, "JetBrains Mono")
            }
            FontCollection().setDefaultFontManager(manager)
        }
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
    @SkipWasmTarget
    @SkipNativeTarget
    fun findTypefaces() = runTest {
        fontCollection().findTypefaces(emptyArray(), FontStyle.NORMAL)
    }

    private suspend fun layoutParagraph(text: String): Paragraph {
        return ParagraphBuilder(style, fontCollection()).use {
            it.addText(text)
            it.build()
        }.layout(Float.POSITIVE_INFINITY)
    }

    private suspend fun singleLineMetrics(text: String): LineMetrics {
        return layoutParagraph(text).lineMetrics.first()
    }

    @Test
    fun layoutParagraph() = runTest {
        val lineMetricsEpsilon = 0.001f

        assertCloseEnough(
            actual = singleLineMetrics("aa"),
            expected = LineMetrics(
                startIndex = 0,
                endIndex = 2,
                endExcludingWhitespaces = 2,
                endIncludingNewline = 2,
                isHardBreak = true,
                ascent = 13.5625,
                descent = 3.380584716796875,
                unscaledAscent = 13.5625,
                height = 17.0,
                width = 15.789764404296875,
                left = 0.0,
                baseline = 13.619415283203125,
                lineNumber = 0
            ), epsilon = lineMetricsEpsilon
        )


        assertCloseEnough(
            actual = singleLineMetrics("яя"),
            expected = LineMetrics(
                startIndex = 0,
                endIndex = 2,
                endExcludingWhitespaces = 2,
                endIncludingNewline = 2,
                isHardBreak = true,
                ascent = 13.5625,
                descent = 3.380584716796875,
                unscaledAscent = 13.5625,
                height = 17.0,
                width = 15.710235595703125,
                left = 0.0,
                baseline = 13.619415283203125,
                lineNumber = 0
            ), epsilon = lineMetricsEpsilon
        )
    }

    @Test
    fun invalidUnicode() = runTest {
        val invalidUnicodeText = "🦊qwerty".substring(1)

        val paragraph = layoutParagraph(invalidUnicodeText)

        // There is an intermediate conversation to UTF-8, so U+FFFD is expected instead of the invalid one.
        assertEquals("�qwerty", paragraph.getText())
        assertEquals(1, paragraph.lineNumber)
    }

    @Test
    fun emptyString() = runTest {
        // https://github.com/JetBrains/skiko/issues/963
        val paragraph = ParagraphBuilder(style, fontCollection())
            .pushStyle(TextStyle())
            .addText("")
            .popStyle()
            .build()
        assertEquals("", paragraph.getText())
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
        assertEquals(0, paragraph.unresolvedCodepointsCount)
        assertContentEquals(IntArray(0), paragraph.unresolvedCodepoints)
        assertEquals(2, paragraph.lineNumber)

        assertContentEquals(arrayOf(), paragraph.rectsForPlaceholders)

        assertContentCloseEnough(arrayOf(
            TextBox(Rect(3.94f, 17.06f, 92.31f, 34.0f), Direction.LTR)
        ), paragraph.getRectsForRange(8, 21, RectHeightMode.TIGHT, RectWidthMode.TIGHT),0.01f)

        paragraph = paragraph
            .updateFontSize(0, text.length, 48.0f)
            .updateForegroundPaint(0, text.length, Paint().apply { color = Color.RED })
            .updateBackgroundPaint(0, text.length, Paint().apply { color = Color.BLACK })
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

    @Test
    fun unresolvedCodepointsWithUnknownSymbols() = runTest {
        // U+E001 is a Private Use Area character not present in Inter.
        // It appears twice in the text to verify count-vs-unique-set semantics.
        val unknownChar = '\uE001'
        val text = "Hello${unknownChar}World${unknownChar}!"

        val paragraph = ParagraphBuilder(style, fontCollection()).use {
            it.addText(text)
            it.build()
        }.layout(Float.POSITIVE_INFINITY)

        // unresolvedGlyphsCount counts all glyph instances (2 occurrences of U+E001)
        assertEquals(2, paragraph.unresolvedGlyphsCount)
        // unresolvedCodepoints returns the set of unique unresolved codepoints
        assertEquals(1, paragraph.unresolvedCodepointsCount)
        val codepoints = paragraph.unresolvedCodepoints
        assertEquals(1, codepoints.size)
        assertTrue(unknownChar.code in codepoints, "Expected U+E001 to be in unresolvedCodepoints")
    }

    @Test
    fun getRectsForRange() {
        val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)

        repeat(1000) { // the bug is flaky, and isn't always reproducible
            val para = ParagraphBuilder(ParagraphStyle(), fontCollection).use {
                it.addText("xxx\r\nxxx")
                it.build()
            }.layout(Float.POSITIVE_INFINITY)

            val rects = para.getRectsForRange(2, 8, RectHeightMode.MAX, RectWidthMode.MAX)
            for (rect in rects) {
                rect.rect.left
                rect.rect.right
                rect.rect.top
                rect.rect.bottom
            }
        }
    }

    @Test
    fun layout_paragraph_with_its_maxIntrinsicWidth_shouldnt_lead_to_wraps() = runTest {
        suspend fun testWraps(isApplyRoundingHackEnabled: Boolean, unexpectedWrapsPresent: Boolean) {
            val paragraphStyle = ParagraphStyle().apply {
                this.isApplyRoundingHackEnabled = isApplyRoundingHackEnabled
                textStyle = TextStyle().apply {
                    fontFamilies = arrayOf("JetBrains Mono")
                    fontSize = 13.0f * 2f
                }
            }
            val paragraph = ParagraphBuilder(paragraphStyle, fontCollection()).use {
                it.addText("x".repeat(104))
                it.addText(" ")
                it.addText("y".repeat(100))
                it.build()
            }.layout(Float.POSITIVE_INFINITY)
            assertEquals(1, paragraph.lineNumber, "Layout in one line with Inf width")

            val maxIntrinsicWidth = paragraph.maxIntrinsicWidth
            val expectedLines = if (unexpectedWrapsPresent) 2 else 1

            paragraph.layout(paragraph.maxIntrinsicWidth)
            assertEquals(expectedLines, paragraph.lineNumber, "Layout with maxIntrinsicWidth " +
                                                              "maxIntrinsicWidth: $maxIntrinsicWidth " +
                                                              "unexpectedWrapsPresent: $unexpectedWrapsPresent " +
                                                              "isApplyRoundingHackEnabled: $isApplyRoundingHackEnabled")
        }
        testWraps(isApplyRoundingHackEnabled = false, unexpectedWrapsPresent = false)
        testWraps(isApplyRoundingHackEnabled = true, unexpectedWrapsPresent = true)
    }

    @Test
    fun paragraphStyleCanChangeReplaceTab() {
        ParagraphStyle().use { it ->
            assertFalse(it.replaceTabCharacters)
            it.replaceTabCharacters = true
            assertTrue(it.replaceTabCharacters)
        }
    }
}
