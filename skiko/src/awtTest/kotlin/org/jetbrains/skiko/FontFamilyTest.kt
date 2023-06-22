package org.jetbrains.skiko

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.FontFamily.FontFamilySource
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FontFamilyTest {

    private val aFontFamily = runBlocking {
        FontFamily.fromTypefaces(
            "JetBrains Mono",
            FontFamilySource.Custom,
            Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf"),
            Typeface.makeFromResource("./fonts/JetBrainsMono-Italic.ttf"),
            Typeface.makeFromResource("./fonts/JetBrainsMono-Bold.ttf")
        )
    }

    private val expectedFontMap = runBlocking {
        mapOf(
            Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf").let { it.fontStyle to it },
            Typeface.makeFromResource("./fonts/JetBrainsMono-Italic.ttf").let { it.fontStyle to it },
            Typeface.makeFromResource("./fonts/JetBrainsMono-Bold.ttf").let { it.fontStyle to it },
        )
    }

    @Test
    fun `should throw IllegalArgumentException when constructed with a blank family name`() {
        assertFailsWith<IllegalArgumentException> {
            FontFamily("", FontFamilySource.Custom)
        }
        assertFailsWith<IllegalArgumentException> {
            FontFamily(" ", FontFamilySource.Custom)
        }
        assertFailsWith<IllegalArgumentException> {
            FontFamily("\t", FontFamilySource.Custom)
        }
    }

    @Test
    fun `should list all available styles provided`() {
        assertEquals(expectedFontMap.keys, aFontFamily.availableStyles)
    }

    @Test
    fun `should list all available typefaces provided`() {
        assertTrue(expectedFontMap.values.equalsLogically(aFontFamily.availableTypefaces))
    }

    @Test
    fun `should add existing typeface to an empty instance`() {
        val loadedTypeface = runBlocking {
            Typeface.makeFromResource("fonts/JetBrainsMono-Regular.ttf")
        }
        val newFontFamilyInfo = FontFamily.fromTypefaces("JetBrains Mono", FontFamilySource.Custom, loadedTypeface)
        assertEquals(setOf(loadedTypeface.fontStyle), newFontFamilyInfo.availableStyles)
    }

    @Test
    fun `should add existing typeface`() {
        val loadedTypeface = runBlocking {
            Typeface.makeFromResource("fonts/JetBrainsMono-Regular.ttf")
        }
        val oldTypeface = aFontFamily[loadedTypeface.fontStyle]!!
        val newFontFamilyInfo = aFontFamily + loadedTypeface
        assertEquals(expectedFontMap.keys, newFontFamilyInfo.availableStyles)
        assertNotEquals(oldTypeface, loadedTypeface)
    }

    @Test
    fun `should throw when adding new, incompatible typeface`() {
        val incompatibleFont = runBlocking { Typeface.makeFromResource("fonts/Inter-V.ttf") }
        assertFailsWith(IllegalArgumentException::class) {
            aFontFamily + incompatibleFont
        }
    }

    @Test
    fun `should remove available style`() {
        val removedStyle = expectedFontMap.keys.last()
        val fontFamilyCache = aFontFamily - removedStyle
        assertEquals(expectedFontMap.keys - removedStyle, fontFamilyCache.availableStyles)
    }

    @Test
    fun `should remove available typeface`() {
        val removedTypeface = expectedFontMap.values.last()
        val fontFamilyCache = aFontFamily - removedTypeface
        val removedStyle = removedTypeface.fontStyle
        assertTrue(fontFamilyCache.availableTypefaces.equalsLogically((expectedFontMap - removedStyle).values))
    }

    @Test
    fun `should do nothing when removing any style from an empty family info`() {
        val anyStyle = FontStyle.BOLD
        val fontFamily = FontFamily("Anything", FontFamilySource.Custom) - anyStyle
        assertTrue(fontFamily.isEmpty())
    }

    @Test
    fun `should do nothing when removing typeface from an empty family info`() {
        val anyTypeface = expectedFontMap.values.first()
        val fontFamily = FontFamily("Anything", FontFamilySource.Custom) - anyTypeface
        assertTrue(fontFamily.isEmpty())
    }

    @Test
    fun `should do nothing when removing any non-existing style`() {
        val anyStyle = FontStyle(weight = 1000, FontWidth.EXTRA_CONDENSED, FontSlant.OBLIQUE)
        val fontFamilyCache = aFontFamily - anyStyle
        assertEquals(aFontFamily.availableStyles, fontFamilyCache.availableStyles)
    }

    @Test
    fun `find closest style weight`() {
        val styles = setOf(
            FontStyle(weight = 400, width = 5, slant = FontSlant.UPRIGHT),
            FontStyle(weight = 400, width = 7, slant = FontSlant.ITALIC),
            FontStyle(weight = 400, width = 5, slant = FontSlant.ITALIC),
            FontStyle(weight = 700, width = 5, slant = FontSlant.UPRIGHT),
        )

        fun closestStyle(weight: Int, width: Int, slant: FontSlant) =
            FontFamily.closestStyle(styles, FontStyle(weight, width, slant))

        assertEquals(
            FontStyle(weight = 400, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 300, width = 5, slant = FontSlant.UPRIGHT)
        )
        assertEquals(
            FontStyle(weight = 400, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 400, width = 5, slant = FontSlant.UPRIGHT)
        )
        assertEquals(
            FontStyle(weight = 700, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 500, width = 5, slant = FontSlant.UPRIGHT)
        )
        assertEquals(
            FontStyle(weight = 700, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 700, width = 5, slant = FontSlant.UPRIGHT)
        )
        assertEquals(
            FontStyle(weight = 700, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 800, width = 5, slant = FontSlant.UPRIGHT)
        )
    }

    @Test
    fun `find closest style width`() {
        val styles = setOf(
            FontStyle(weight = 400, width = 5, slant = FontSlant.UPRIGHT),
            FontStyle(weight = 400, width = 7, slant = FontSlant.ITALIC),
            FontStyle(weight = 400, width = 5, slant = FontSlant.ITALIC),
            FontStyle(weight = 700, width = 5, slant = FontSlant.UPRIGHT),
        )

        fun closestStyle(weight: Int, width: Int, slant: FontSlant) =
            FontFamily.closestStyle(styles, FontStyle(weight, width, slant))

        assertEquals(
            FontStyle(weight = 400, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 300, width = 3, slant = FontSlant.UPRIGHT)
        )
        assertEquals(
            FontStyle(weight = 400, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 300, width = 5, slant = FontSlant.UPRIGHT)
        )
        assertEquals(
            FontStyle(weight = 400, width = 7, slant = FontSlant.ITALIC),
            closestStyle(weight = 300, width = 6, slant = FontSlant.UPRIGHT)
        )
        assertEquals(
            FontStyle(weight = 400, width = 7, slant = FontSlant.ITALIC),
            closestStyle(weight = 300, width = 7, slant = FontSlant.UPRIGHT)
        )

        assertEquals(
            FontStyle(weight = 700, width = 5, slant = FontSlant.UPRIGHT),
            closestStyle(weight = 700, width = 7, slant = FontSlant.UPRIGHT)
        )
    }

    @Test
    fun `should do nothing when removing any non-existing typeface`() {
        val anyTypeface = runBlocking { Typeface.makeFromResource("fonts/Inter-V.ttf") }

        val fontFamily = FontFamily("Anything", FontFamilySource.Custom) - anyTypeface
        assertTrue(fontFamily.isEmpty())
    }

    @Test
    fun `should throw when removing an existing but logically different typeface`() {
        val incompatibleTypeface = runBlocking { Typeface.makeFromResource("fonts/Inter-V.ttf") }

        assertFailsWith(IllegalArgumentException::class) {
            aFontFamily - incompatibleTypeface
        }
    }
}

private fun Collection<Typeface>.equalsLogically(other: Collection<Typeface>): Boolean {
    if (size != other.size) return false
    return all { thisTypeface ->
        other.any { it.equalsLogically(thisTypeface) }
    }
}

private fun Typeface.equalsLogically(other: Typeface?): Boolean {
    if (other == null) return false

    fun Array<FontFamilyName>.equalsLogically(other: Array<FontFamilyName>): Boolean {
        if (size != other.size) return false
        return all { thisName ->
            other.any { it.language == thisName.language && it.name == thisName.name }
        }
    }

    return familyName == other.familyName &&
            familyNames.equalsLogically(other.familyNames) &&
            fontStyle == other.fontStyle
}
