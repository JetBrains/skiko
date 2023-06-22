package org.jetbrains.skiko.awt.font

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import org.junit.Test
import kotlin.io.path.createTempFile
import kotlin.io.path.writeBytes
import kotlin.test.*

class InMemoryTypefaceCacheTest {
    private val cache = TypefaceCache.inMemory()

    @Test
    fun `should be able to register, and then find custom fonts from classpath resources`() = runTest {
        cache.addResource("LibreBarcode39-Regular.ttf")

        val typeface = cache.getTypefaceOrNull("Libre Barcode 39", FontStyle.NORMAL)!!
        assertContains(cache.familyNames(), typeface.familyName, "Custom font not listed in all fonts")
        assertContains(
            cache.familyNames(),
            typeface.familyName,
            "Custom font not listed in custom fonts"
        )

        assertNotNull(typeface, "Font must have been loaded from resources")
        assertEquals("Libre Barcode 39", typeface.familyName, "Font family name must match")
        assertFalse(typeface.isBold, "Font must not be bold")
        assertFalse(typeface.isItalic, "Font must not be italic")
    }

    @Test
    fun `should be able to register, and then find custom fonts from a file`() = runTest {
        assertNull(
            cache.getTypefaceOrNull("Libre Barcode 39", FontStyle.NORMAL),
            "The font we're trying to add must not already be loaded"
        )

        val fontFile = createTempFile("awtfontmanagertest", "testfont")
        withContext(Dispatchers.IO) {
            val fontBytes = Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("LibreBarcode39-Regular.ttf")!!
                .readAllBytes()

            fontFile.writeBytes(fontBytes)
        }

        cache.addFile(fontFile.toFile())

        assertTrue(cache.familyNames().contains("Libre Barcode 39"), "Font must be registered")

        val typeface = cache.getTypefaceOrNull("Libre Barcode 39", FontStyle.NORMAL)
        assertNotNull(typeface, "Font must have been loaded from disk")
        assertEquals("Libre Barcode 39", typeface.familyName, "Font family name must match")
        assertFalse(typeface.isBold, "Font must not be bold")
        assertFalse(typeface.isItalic, "Font must not be italic")

        assertContains(cache.familyNames(), typeface.familyName, "Custom font not listed in all fonts")
        assertContains(
            cache.familyNames(),
            typeface.familyName,
            "Custom font not listed in custom fonts"
        )
    }

    @Test
    fun `should be able to register, and then find custom fonts from a typeface`() = runTest {
        assertNull(
            cache.getTypefaceOrNull("Libre Barcode 39", FontStyle.NORMAL),
            "The font we're trying to add must not already be loaded"
        )

        val loadedTypeface = withContext(Dispatchers.IO) {
            val fontBytes = Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("LibreBarcode39-Regular.ttf")!!
                .readAllBytes()

            Typeface.makeFromData(Data.makeFromBytes(fontBytes))
        }

        cache.addTypeface(loadedTypeface)

        val typeface = cache.getTypefaceOrNull("Libre Barcode 39", FontStyle.NORMAL)!!
        assertContains(cache.familyNames(), typeface.familyName, "Custom font not listed in all fonts")
        assertContains(
            cache.familyNames(),
            typeface.familyName,
            "Custom font not listed in custom fonts"
        )

        assertNotNull(typeface, "Font must have been loaded from disk")
        assertEquals("Libre Barcode 39", typeface.familyName, "Font family name must match")
        assertFalse(typeface.isBold, "Font must not be bold")
        assertFalse(typeface.isItalic, "Font must not be italic")
    }

    @Test
    fun `should remove all custom fonts when clearing custom fonts`() = runTest {
        cache.addResource("./fonts/Inter-V.ttf")

        cache.addTypeface(Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf"))
        cache.addResource("./fonts/JetBrainsMono-Italic.ttf")

        val fontFile = createTempFile("awtfontmanagertest", "testfont")
        withContext(Dispatchers.IO) {
            val fontBytes = Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("./fonts/JetBrainsMono-Bold.ttf")!!
                .readAllBytes()

            fontFile.writeBytes(fontBytes)
        }

        cache.addFile(fontFile.toFile())

        assertEquals(2, cache.familyNames().size)

        cache.clear()

        assertTrue(cache.familyNames().isEmpty())
    }

    @Test
    fun `should do nothing when trying to remove a non-existent custom font`() {
        cache.removeFontFamily("Bananananananananana*&^%$")
        assertTrue(cache.isEmpty())
    }
}
