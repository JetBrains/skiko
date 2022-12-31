package org.jetbrains.skiko

import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*
import org.jetbrains.skia.tests.makeFromResource
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FontFamilyInfoTest {

    private val aFontFamilyInfo = runBlocking {
        FontFamilyInfo.fromTypefaces(
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

    private val emptyFontFamilyInfo = FontFamilyInfo.fromTypefaces()

    @Test
    fun `should list all available styles provided`() {
        assertEquals(expectedFontMap.keys, aFontFamilyInfo.availableStyles)
    }

    @Test
    fun `should list all available typefaces provided`() {
        assertTrue(expectedFontMap.values.equalsLogically(aFontFamilyInfo.availableTypefaces))
    }

    @Test
    fun `should add existing typeface to an empty instance`() {
        val loadedTypeface = runBlocking {
            Typeface.makeFromResource("fonts/JetBrainsMono-Regular.ttf")
        }
        val newFontFamilyInfo = emptyFontFamilyInfo + loadedTypeface
        assertEquals(setOf(loadedTypeface.fontStyle), newFontFamilyInfo.availableStyles)
    }

    @Test
    fun `should add existing typeface`() {
        val loadedTypeface = runBlocking {
            Typeface.makeFromResource("fonts/JetBrainsMono-Regular.ttf")
        }
        val oldTypeface = aFontFamilyInfo[loadedTypeface.fontStyle]!!
        val newFontFamilyInfo = aFontFamilyInfo + loadedTypeface
        assertEquals(expectedFontMap.keys, newFontFamilyInfo.availableStyles)
        assertNotEquals(oldTypeface, loadedTypeface)
    }

    @Test
    fun `should throw when adding new, incompatible typeface`() {
        val incompatibleFont = runBlocking { Typeface.makeFromResource("fonts/Inter-V.ttf") }
        assertFailsWith(IllegalArgumentException::class) {
            aFontFamilyInfo + incompatibleFont
        }
    }

    @Test
    fun `should remove available style`() {
        val removedStyle = expectedFontMap.keys.last()
        val fontFamilyInfo = aFontFamilyInfo - removedStyle
        assertEquals(expectedFontMap.keys - removedStyle, fontFamilyInfo.availableStyles)
    }

    @Test
    fun `should remove available typeface`() {
        val removedTypeface = expectedFontMap.values.last()
        val fontFamilyInfo = aFontFamilyInfo - removedTypeface
        val removedStyle = removedTypeface.fontStyle
        assertTrue(fontFamilyInfo.availableTypefaces.equalsLogically((expectedFontMap - removedStyle).values))
    }

    @Test
    fun `should do nothing when removing any style from an empty family info`() {
        val anyStyle = FontStyle.BOLD
        val fontFamilyInfo = emptyFontFamilyInfo - anyStyle
        assertTrue(fontFamilyInfo.isEmpty())
    }

    @Test
    fun `should do nothing when removing typeface from an empty family info`() {
        val anyTypeface = expectedFontMap.values.first()
        val fontFamilyInfo = emptyFontFamilyInfo - anyTypeface
        assertTrue(fontFamilyInfo.isEmpty())
    }

    @Test
    fun `should do nothing when removing any non-existing style`() {
        val anyStyle = FontStyle(weight = 1000, FontWidth.EXTRA_CONDENSED, FontSlant.OBLIQUE)
        val fontFamilyInfo = aFontFamilyInfo - anyStyle
        assertEquals(aFontFamilyInfo.availableStyles, fontFamilyInfo.availableStyles)
    }

    @Test
    fun `should do nothing when removing any non-existing typeface`() {
        val anyTypeface = runBlocking { Typeface.makeFromResource("fonts/Inter-V.ttf") }

        val fontFamilyInfo = emptyFontFamilyInfo - anyTypeface
        assertTrue(fontFamilyInfo.isEmpty())
    }

    @Test
    fun `should throw when removing an existing but logically different typeface`() {
        val incompatibleTypeface = runBlocking { Typeface.makeFromResource("fonts/Inter-V.ttf") }

        assertFailsWith(IllegalArgumentException::class) {
            aFontFamilyInfo - incompatibleTypeface
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
