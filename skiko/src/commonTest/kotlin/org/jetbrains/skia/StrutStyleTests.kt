package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.StrutStyle
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


class StrutStyleTests {

    @Test
    fun topRatioRoundTrip() {
        StrutStyle().use { strutStyle ->
            strutStyle.topRatio = 0.25f
            assertEquals(0.25f, strutStyle.topRatio)
        }
    }

    @Test
    fun strutStyleEqualityTest() {
        StrutStyle().use { first ->
            StrutStyle().use { second ->
                assertEquals(first, second)
                second.setFontFamilies(arrayOf("Different family"))
                assertNotEquals(first, second)
                first.setFontFamilies(arrayOf("Different family"))
                assertEquals(first, second)
            }
        }
    }

    @Test
    fun strutStyleMetricsTest() {
        StrutStyle().use { strutStyle ->
            strutStyle.fontSize = 24f
            strutStyle.height = 1.5f
            strutStyle.leading = 0.25f
            strutStyle.topRatio = 0.375f

            assertEquals(24f, strutStyle.fontSize)
            assertEquals(1.5f, strutStyle.height)
            assertEquals(0.25f, strutStyle.leading)
            assertEquals(0.375f, strutStyle.topRatio)
        }
    }

    @Test
    fun strutStyleFlagsTest() {
        StrutStyle().use { strutStyle ->
            strutStyle.isEnabled = true
            strutStyle.isHeightForced = true
            strutStyle.isHeightOverridden = true
            assertTrue(strutStyle.isEnabled)
            assertTrue(strutStyle.isHeightForced)
            assertTrue(strutStyle.isHeightOverridden)

            strutStyle.isEnabled = false
            strutStyle.isHeightForced = false
            strutStyle.isHeightOverridden = false
            assertFalse(strutStyle.isEnabled)
            assertFalse(strutStyle.isHeightForced)
            assertFalse(strutStyle.isHeightOverridden)
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun strutStyleHalfLeadingTest() {
        StrutStyle().use { strutStyle ->
            strutStyle.isHalfLeading = true
            assertTrue(strutStyle.isHalfLeading)
            assertEquals(0.5f, strutStyle.topRatio)

            strutStyle.isHalfLeading = false
            assertFalse(strutStyle.isHalfLeading)
            assertEquals(-1f, strutStyle.topRatio)
        }
    }

    @Test
    fun strutStyleTest() {
        StrutStyle().use { strutStyle ->
            assertEquals(FontStyle(FontWeight.NORMAL, FontWidth.NORMAL, FontSlant.UPRIGHT), strutStyle.fontStyle)

            strutStyle.fontStyle = FontStyle(FontWeight.LIGHT, FontWidth.SEMI_CONDENSED, FontSlant.ITALIC)

            assertEquals(FontStyle(FontWeight.LIGHT, FontWidth.SEMI_CONDENSED, FontSlant.ITALIC), strutStyle.fontStyle)


            strutStyle.setFontFamilies(arrayOf("MonacoShmonaco"))
            assertContentEquals(arrayOf("MonacoShmonaco"), strutStyle.fontFamilies)
        }
    }
}
