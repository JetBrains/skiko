package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.ParagraphStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class ParagraphStyleTests {

    @Test
    fun paragraphStyleEllipsisTests() {
        ParagraphStyle().use { paragraphStyle ->
            paragraphStyle.ellipsis = ".^."
            assertEquals(".^.", paragraphStyle.ellipsis)
        }
    }
}