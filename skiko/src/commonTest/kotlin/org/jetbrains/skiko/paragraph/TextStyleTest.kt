package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.jbMonoPath
import org.jetbrains.skia.paragraph.DecorationLineStyle
import org.jetbrains.skia.paragraph.DecorationStyle
import org.jetbrains.skia.paragraph.Shadow
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.TextStyleAttribute
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
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
                assertContentEquals(ts1.fontFamilies, ts2.fontFamilies)
                ts1.fontFamilies = arrayOf("foo", "qux")
                ts2.fontFamilies = arrayOf("bar", "zig")
                assertNotEquals(ts1, ts2)
            }
        }
    }

    @Test
    fun textStyleAddFontFeatureTest() {
        TextStyle().use { textStyle ->
            textStyle.addFontFeature(FontFeature("FONT", true))
            textStyle.addFontFeatures(arrayOf(FontFeature("TONF", true), FontFeature("IDDI", false)))
            assertContentEquals(arrayOf(
                FontFeature(84, 1, 0u, 4294967295u),
                FontFeature(70, 1, 0u, 4294967295u),
                FontFeature(73, 0, 0u, 4294967295u),
            ),  textStyle.fontFeatures)
        }
    }

    @Test
    fun textStyleLocaleTest() {
        TextStyle().use { textStyle ->
            textStyle.locale = "fr_FR.UTF-8"
            assertEquals("fr_FR.UTF-8", textStyle.locale)
        }
    }

    @Test
    fun textDecorationStyleTest() {
        TextStyle().use { textStyle ->
            textStyle.decorationStyle = DecorationStyle(
                underline = true,
                overline = true,
                lineThrough = false,
                gaps = true,
                color = Color.BLUE,
                lineStyle = DecorationLineStyle.DASHED.ordinal,
                thicknessMultiplier = 2f
            )

            val expectedTextStyle = DecorationStyle(
                underline = true,
                overline = true,
                lineThrough = false,
                gaps = true,
                color = Color.BLUE,
                lineStyle = DecorationLineStyle.DASHED.ordinal,
                thicknessMultiplier = 2f
            )

            assertEquals(expectedTextStyle, textStyle.decorationStyle)
        }
    }

    @Test
    fun textStyleHeightTest() {
        TextStyle().use { textStyle ->
            assertNull(textStyle.height)
            textStyle.height = 4f
            assertEquals(4f, textStyle.height)
        }
    }

    @Test
    fun textShadowsTest() {
        TextStyle().use { textStyle ->

            textStyle.addShadow(Shadow(200, 0.2f, 0.4f, 1.4))
            textStyle.addShadows(arrayOf(Shadow(100, 0.3f, 0.1f, 2.0)))

            assertContentCloseEnough(
                arrayOf(
                    Shadow(200, 0.2f, 0.4f, 1.4),
                    Shadow(100, 0.3f, 0.1f, 2.0)
                ), textStyle.shadows, if (kotlinBackend == KotlinBackend.JS) 0.00001f else 0f
            )

        }
    }

    @Test
    fun textStyleBaselineTest() {
        TextStyle().use { textStyle ->
            assertEquals(0.0f, textStyle.baselineShift)
            textStyle.baselineShift = 4f
            assertEquals(4f, textStyle.baselineShift)
        }
    }

    @Test
    fun textStyleMetricsContainsMeaningfulValues() = runTest {
        val jbMono = Typeface.makeFromResource(jbMonoPath)
        TextStyle().use { textStyle ->
            textStyle.typeface = jbMono
            val metrics = textStyle.fontMetrics
            assertFalse(metrics.ascent == 0f && metrics.descent == 0f && metrics.leading == 0f)
        }
    }

    @Test
    fun textStyleNotContainNaNValues() = runTest {
        val jbMono = Typeface.makeFromResource(jbMonoPath)
        TextStyle().use { textStyle ->
            textStyle.typeface = jbMono
            textStyle.height = 32f
            assertTrue(!textStyle.fontMetrics.ascent.isNaN())
        }
    }

}
