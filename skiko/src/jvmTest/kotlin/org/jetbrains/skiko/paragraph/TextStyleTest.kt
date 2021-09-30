package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.TextStyleAttribute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
                    assertEquals(true, ts1.equals(attr, ts2), "$attr")
                    assertEquals(true, ts2.equals(attr, ts1), "$attr")
                }
            }
        }

        assertFalse(
            TextStyle().setColor(-0x33cd00).equals(TextStyleAttribute.ALL_ATTRIBUTES, TextStyle().setColor(-0xff33cd))
        )

        assertTrue(
            TextStyle().setColor(-0x33cd00).equals(TextStyleAttribute.BACKGROUND, TextStyle().setColor(-0xff33cd))
        )
    }
}