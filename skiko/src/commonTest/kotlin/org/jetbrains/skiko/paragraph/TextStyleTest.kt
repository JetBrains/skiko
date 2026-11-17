package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class TextStyleTest {

    @Test
    fun textStyleFontAndColorTest() {
        TextStyle().use { textStyle ->
            val fontStyle = FontStyle(FontWeight.BOLD, FontWidth.CONDENSED, FontSlant.ITALIC)
            textStyle.fontStyle = fontStyle
            textStyle.fontSize = 24f
            textStyle.color = Color.BLUE

            assertEquals(fontStyle, textStyle.fontStyle)
            assertEquals(24f, textStyle.fontSize)
            assertEquals(Color.BLUE, textStyle.color)
        }
    }

    @Test
    fun textStyleSpacingTest() {
        TextStyle().use { textStyle ->
            textStyle.letterSpacing = 1.25f
            textStyle.wordSpacing = 3.5f
            assertEquals(1.25f, textStyle.letterSpacing)
            assertEquals(3.5f, textStyle.wordSpacing)

            textStyle.letterSpacing = -0.5f
            textStyle.wordSpacing = 0f
            assertEquals(-0.5f, textStyle.letterSpacing)
            assertEquals(0f, textStyle.wordSpacing)
        }
    }

    @Test
    fun textStylePaintsTest() {
        TextStyle().use { textStyle ->
            assertNull(textStyle.foreground)
            assertNull(textStyle.background)

            Paint().use { paint ->
                paint.color = Color.RED
                textStyle.foreground = paint
                paint.color = Color.BLUE
                textStyle.background = paint
            }

            assertNotNull(textStyle.foreground).use { foreground ->
                assertEquals(Color.RED, foreground.color)
            }
            assertNotNull(textStyle.background).use { background ->
                assertEquals(Color.BLUE, background.color)
            }

            textStyle.foreground = null
            textStyle.background = null
            assertNull(textStyle.foreground)
            assertNull(textStyle.background)
        }
    }

    @Test
    fun textStyleBaselineModeTest() {
        TextStyle().use { textStyle ->
            textStyle.baselineMode = BaselineMode.IDEOGRAPHIC
            assertEquals(BaselineMode.IDEOGRAPHIC, textStyle.baselineMode)
            textStyle.baselineMode = BaselineMode.ALPHABETIC
            assertEquals(BaselineMode.ALPHABETIC, textStyle.baselineMode)
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun textStyleHalfLeadingTest() {
        TextStyle().use { textStyle ->
            textStyle.isHalfLeading = true
            assertTrue(textStyle.isHalfLeading)
            assertEquals(0.5f, textStyle.topRatio)

            textStyle.isHalfLeading = false
            assertFalse(textStyle.isHalfLeading)
            assertEquals(-1f, textStyle.topRatio)
        }
    }

    @Test
    fun textStylePlaceholderTest() {
        TextStyle().use { textStyle ->
            assertFalse(textStyle.isPlaceholder)
            textStyle.setPlaceholder()
            assertTrue(textStyle.isPlaceholder)
        }
    }

    @Test
    fun textStyleTypefaceTest() = runTest {
        Typeface.makeFromResource(jbMonoPath).use { typeface ->
            TextStyle().use { textStyle ->
                assertNull(textStyle.typeface)
                textStyle.typeface = typeface
                assertNotNull(textStyle.typeface).use { actual ->
                    assertEquals(typeface.uniqueId, actual.uniqueId)
                }
                textStyle.typeface = null
                assertNull(textStyle.typeface)
            }
        }
    }

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

            textStyle.clearFontFeatures()
            assertTrue(textStyle.fontFeatures.isEmpty())
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

            textStyle.clearShadows()
            assertTrue(textStyle.shadows.isEmpty())

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
    fun textStyleTopRatioTest() {
        TextStyle().use { textStyle ->
            assertEquals(-1f, textStyle.topRatio)
            textStyle.topRatio = 0.42f
            assertEquals(0.42f, textStyle.topRatio, 0.001f)
        }
    }

    @Test
    fun textStyleFontEdgingTest() {
        TextStyle().use { textStyle ->
            assertEquals(FontEdging.ANTI_ALIAS, textStyle.fontEdging)
            textStyle.fontEdging = FontEdging.SUBPIXEL_ANTI_ALIAS
            assertEquals(FontEdging.SUBPIXEL_ANTI_ALIAS, textStyle.fontEdging)
        }
    }

    @Test
    fun textStyleSubpixelTest() {
        TextStyle().use { textStyle ->
            assertTrue(textStyle.subpixel)
            textStyle.subpixel = false
            assertFalse(textStyle.subpixel)
        }
    }

    @Test
    fun textStyleFontHintingTest() {
        TextStyle().use { textStyle ->
            assertEquals(FontHinting.SLIGHT, textStyle.fontHinting)
            textStyle.fontHinting = FontHinting.FULL
            assertEquals(FontHinting.FULL, textStyle.fontHinting)
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
