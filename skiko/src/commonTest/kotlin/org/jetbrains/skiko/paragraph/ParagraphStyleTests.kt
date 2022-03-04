package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextIndent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParagraphStyleTests {

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
}