package org.jetbrains.skiko

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.paragraph.*
import org.junit.Test
import kotlin.test.assertEquals

class ParagraphTest {
    private val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)

    @Test
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
    fun layoutParagraph() {
        singleLineMetrics("aa").let { lineMetrics -> // latin
            assertEquals(lineMetrics.startIndex, 0)
            assertEquals(lineMetrics.endIndex, 2)
            assertEquals(lineMetrics.endIncludingNewline, 2)
            assertEquals(lineMetrics.endExcludingWhitespaces, 2)
        }
        singleLineMetrics("аа").let { lineMetrics -> // cyrillic
            assertEquals(lineMetrics.startIndex, 0)
            assertEquals(lineMetrics.endIndex, 2)
            assertEquals(lineMetrics.endIncludingNewline, 2)
            assertEquals(lineMetrics.endExcludingWhitespaces, 2)
        }
        singleLineMetrics("зз").let { lineMetrics -> // cyrillic
            assertEquals(lineMetrics.startIndex, 0)
            assertEquals(lineMetrics.endIndex, 2)
            assertEquals(lineMetrics.endIncludingNewline, 2)
            assertEquals(lineMetrics.endExcludingWhitespaces, 2)
        }
    }
}