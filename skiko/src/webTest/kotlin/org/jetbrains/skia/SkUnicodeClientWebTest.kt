package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.SkUnicodeClient
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.WebBreakIteratorProvider
import org.jetbrains.skia.paragraph.createForText
import org.jetbrains.skiko.SkikoFeatureFlags
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkUnicodeClientWebTest {

    private val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)

    @AfterTest
    fun resetFlags() {
        SkikoFeatureFlags.useBrowserSegmenter = false
    }

    @Test
    fun intlSegmenterIsAvailable() {
        assertTrue(SkUnicodeClient.isSupported, "Intl.Segmenter is expected to be available")
    }

    @Test
    fun graphemeBreaksAreUtf16Based() {
        if (!SkUnicodeClient.isSupported) return
        val provider = WebBreakIteratorProvider()
        // A surrogate pair + a combining sequence: 4 UTF-16 code units, 2 graphemes.
        val text = "\uD83D\uDE00e\u0301"
        assertContentEquals(intArrayOf(0, 2, 4), provider.computeGraphemeBreaks(text))
        assertContentEquals(intArrayOf(0), provider.computeGraphemeBreaks(""))
    }

    @Test
    fun wordBreaksReportWordLikeFlag() {
        if (!SkUnicodeClient.isSupported) return
        val words = WebBreakIteratorProvider().computeWordBreaks("ab cd")
        assertEquals(0, words.size % 3)
        assertEquals(0, words[0])
        assertEquals(2, words[1])
        assertEquals(1, words[2])
        assertEquals("ab cd".length, words[words.size - 2])
    }

    @Test
    fun lineBreaksMarkHardBreaks() {
        if (!SkUnicodeClient.isSupported) return
        val breaks = WebBreakIteratorProvider().computeLineBreaks("a\nb c")
        assertEquals(0, breaks.size % 2)
        val hard = (breaks.indices step 2).filter { breaks[it + 1] == 1 }.map { breaks[it] }
        // A hard break opportunity is reported right after the newline.
        assertContentEquals(listOf(2), hard)
    }

    @Test
    fun paragraphIsLaidOutWithBrowserSegmenter() {
        if (!SkUnicodeClient.isSupported) return
        SkikoFeatureFlags.useBrowserSegmenter = true

        val text = "Hello browser segmenter\nsecond line"
        val paragraphStyle = ParagraphStyle().apply {
            textStyle = TextStyle().apply { fontSize = 16.0f }.setColor(Color.BLACK)
        }
        val paragraph = ParagraphBuilder.createForText(paragraphStyle, fontCollection, text).use {
            it.addText(text)
            it.build().layout(80.0f)
        }

        // The text has a mandatory break and does not fit into 80px, so it must wrap.
        assertTrue(paragraph.lineNumber > 2, "Expected the text to wrap, got ${paragraph.lineNumber} lines")
        assertTrue(paragraph.height > 0f)

        val surface = Surface.makeRasterN32Premul(80, paragraph.height.toInt() + 1)
        surface.canvas.clear(Color.WHITE)
        paragraph.paint(surface.canvas, 0.0f, 0.0f)
        surface.makeImageSnapshot().use { image ->
            val bitmap = Bitmap.makeFromImage(image)
            var notWhite = 0
            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    if (bitmap.getColor(x, y) != Color.WHITE) notWhite++
                }
            }
            assertTrue(notWhite > 0, "Expected the text to be rendered")
        }
    }
}
