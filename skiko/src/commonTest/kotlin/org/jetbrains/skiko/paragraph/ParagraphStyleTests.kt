package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontRasterSettings
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

    @Test
    fun paragraphStyleFontRasterSettingsTests() {
        ParagraphStyle().use { paragraphStyle ->
            val poorRasterSettings = FontRasterSettings(
                edging = FontEdging.ALIAS,
                hinting = FontHinting.NONE,
                subpixel = false)
            paragraphStyle.fontRasterSettings = poorRasterSettings
            assertEquals(poorRasterSettings, paragraphStyle.fontRasterSettings)

            val gloriousRasterSettings = FontRasterSettings(
                edging = FontEdging.SUBPIXEL_ANTI_ALIAS,
                hinting = FontHinting.FULL,
                subpixel = true)

            paragraphStyle.fontRasterSettings = gloriousRasterSettings
            assertEquals(gloriousRasterSettings, paragraphStyle.fontRasterSettings)
        }
    }
}