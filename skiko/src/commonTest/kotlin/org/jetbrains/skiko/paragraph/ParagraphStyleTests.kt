package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontRastrSettings
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
    fun paragraphStyleFontRastrSettingsTests() {
        ParagraphStyle().use { paragraphStyle ->
            val poorRasterSettings = FontRastrSettings(
                edging = FontEdging.ALIAS,
                hinting = FontHinting.NONE,
                subpixel = false)
            paragraphStyle.fontRastrSettings = poorRasterSettings
            assertEquals(poorRasterSettings, paragraphStyle.fontRastrSettings)

            val gloriousRasterSettings = FontRastrSettings(
                edging = FontEdging.SUBPIXEL_ANTI_ALIAS,
                hinting = FontHinting.FULL,
                subpixel = true)

            paragraphStyle.fontRastrSettings = gloriousRasterSettings
            assertEquals(gloriousRasterSettings, paragraphStyle.fontRastrSettings)
        }
    }
}