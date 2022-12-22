package org.jetbrains.skiko

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.jetbrains.skia.FontStyle
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.awt.GraphicsEnvironment
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AwtFontInteropTest {
    private val fontManager = AwtFontManager

    @Before
    fun assumeOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless())
        Assume.assumeTrue(hostOs != OS.Linux)
    }

    @Test
    fun canFindRegularFont() = runBlockingTest {
        val typeface = fontManager.getTypefaceOrNull("Verdana", FontStyle.NORMAL)
        assertNotNull(typeface, "Font must exist")
        assertEquals("Verdana", typeface.familyName, "Font family name must match")
        assertFalse(typeface.isBold, "Font must not be bold")
        assertFalse(typeface.isItalic, "Font must not be italic")
    }

    @Test
    fun canFindBoldFont() = runBlockingTest {
        val typeface = fontManager.getTypefaceOrNull("Verdana", FontStyle.BOLD)
        assertNotNull(typeface, "Font must exist")
        assertEquals("Verdana", typeface.familyName, "Font family name must match")
        assertTrue(typeface.isBold, "Font must be bold")
        assertFalse(typeface.isItalic, "Font must not be italic")
    }

    @Test
    fun canFindItalicFont() = runBlockingTest {
        val typeface = fontManager.getTypefaceOrNull("Verdana", FontStyle.ITALIC)
        assertNotNull(typeface, "Font must exist")
        assertEquals("Verdana", typeface.familyName, "Font family name must match")
        assertFalse(typeface.isBold, "Font must not be bold")
        assertTrue(typeface.isItalic, "Font must be italic")
    }

    @Test
    fun canFindBoldItalicFont() = runBlockingTest {
        val typeface = fontManager.getTypefaceOrNull("Verdana", FontStyle.BOLD_ITALIC)
        assertNotNull(typeface, "Font must exist")
        assertEquals("Verdana", typeface.familyName, "Font family name must match")
        assertTrue(typeface.isBold, "Font must be bold")
        assertTrue(typeface.isItalic, "Font must be italic")
    }

    @Test
    fun cantFindNonExistentFont() = runBlockingTest {
        val typeface = fontManager.getTypefaceOrNull("XXXYYY745", FontStyle.NORMAL)
        assertNull(typeface, "Font must not match (doesn't exist!)")
    }

    @Test
    fun canRegisterFontFromClasspath() = runBlockingTest {
        assertNull(
            fontManager.getTypefaceOrNull("Libre Barcode 39", FontStyle.NORMAL),
            "The font we're trying to add must not already be loaded"
        )

        fontManager.addResourceFont("LibreBarcode39-Regular.ttf")

        val typeface = fontManager.getTypefaceOrNull("Libre Barcode 39", FontStyle.NORMAL)
        assertNotNull(typeface, "Font must have been loaded from resources")
        assertEquals("Libre Barcode 39", typeface.familyName, "Font family name must match")
        assertFalse(typeface.isBold, "Font must not be bold")
        assertFalse(typeface.isItalic, "Font must not be italic")
    }

    @Test
    fun listAllFonts() = runBlockingTest {
        val families = fontManager.availableFontFamilies
        assertTrue(families.isNotEmpty(), "Available font families must not be empty")

        println("Enumerated font families:")
        println(families.sorted().joinToString("\n") { " * $it" })

        val systemFont = fontManager.getTypefaceOrNull("System font", FontStyle.NORMAL)
        assertNotNull(systemFont, "System font must be loaded")
        println(systemFont.familyNames.joinToString(", ", "System font families: "))
    }
}
