package org.jetbrains.skia.paragraph

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import kotlin.test.Test
import kotlin.test.assertEquals

//@SkipJsTarget
//@SkipNativeTarget
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
}
