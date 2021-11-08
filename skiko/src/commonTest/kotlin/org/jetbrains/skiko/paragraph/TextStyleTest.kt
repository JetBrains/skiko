package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.FontMetrics
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.TextStyleAttribute
import kotlin.test.*

class TextStyleTest {

    @Test
    fun textStyleTest() {
        assertEquals(TextStyle(), TextStyle())
        TextStyle().use { ts1 ->
            TextStyle().use { ts2 ->
                for (attr in arrayOf(
                    TextStyleAttribute.ALL_ATTRIBUTES,
                    TextStyleAttribute.FONT,
                    TextStyleAttribute.FOREGROUND,
                    TextStyleAttribute.BACKGROUND,
                    TextStyleAttribute.SHADOW,
                    TextStyleAttribute.DECORATIONS,
                    TextStyleAttribute.LETTER_SPACING,
                    TextStyleAttribute.WORD_SPACING,
                    TextStyleAttribute.FONT_EXACT
                )) {
                    assertTrue(ts1.equals(attr, ts2), "$attr")
                    assertTrue(ts2.equals(attr, ts1), "$attr")
                }
            }
        }

        assertFalse(
            TextStyle().setColor(-0x33cd00).equals(TextStyleAttribute.ALL_ATTRIBUTES, TextStyle().setColor(-0xff33cd))
        )

        assertTrue(
            TextStyle().setColor(-0x33cd00).equals(TextStyleAttribute.BACKGROUND, TextStyle().setColor(-0xff33cd))
        )

        TextStyle().use { ts1 ->
            TextStyle().use { ts2 ->
                ts1.fontFamilies = arrayOf("foo", "qux")
                ts2.fontFamilies = arrayOf("foo", "qux")
                assertEquals(ts1, ts2)
                ts1.fontFamilies = arrayOf("foo", "qux")
                ts2.fontFamilies = arrayOf("bar", "zig")
                assertNotEquals(ts1, ts2)
            }
        }
    }

    @Test
    fun textStyleFontMetrics() {
        TextStyle().use { textStyle ->
            val defaultFontMetrics = FontMetrics(
                top = -17.253906f,
                ascent = -12.995117f,
                descent = 3.3017578f,
                bottom = 6.4804688f,
                leading = 0f,
                avgCharWidth = 7.095703f,
                maxCharWidth = 39.395508f,
                xMin = -14.287109f,
                xMax = 25.108398f,
                xHeight = 8.0f,
                capHeight = 10.0f,
                underlineThickness = 0.6152344f,
                underlinePosition = 0.2734375f,
                strikeoutThickness = 0.6972656f,
                strikeoutPosition = -3.6230469f,
            )

            assertEquals(defaultFontMetrics, textStyle.fontMetrics)
        }
    }
}
