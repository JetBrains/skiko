package org.jetbrains.skiko

import org.jetbrains.skiko.tests.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Font

class AwtFontInterop {
    @Test
    fun canFindAvailableFont() = AwtFontManager.whenAllFontsCachedBlocking {
        val font = Font("Verdana", Font.BOLD, 12)
        val path = AwtFontManager.findAvailableFontFile(font)
        assertTrue("Font must be found", path != null)
        path!!
        assertTrue("Font must be file", path.exists() && path.isFile)
    }

    @Test
    fun canFindFont() {
        runTest {
            val font = Font("Verdana", Font.BOLD, 12)
            val path = AwtFontManager.findFontFile(font)
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @Test
    fun nonExistentFont() = AwtFontManager.whenAllFontsCachedBlocking {
        val font = Font("XXXYYY745", Font.BOLD, 12)
        val path = AwtFontManager.findAvailableFontFile(font)
        assertTrue("Font must not be found", path == null)
    }

    @Test
    fun makeSkikoTypeface() {
        runTest {
            val font = Font("Verdana", Font.BOLD, 12)
            val skikoTypeface = font.toSkikoTypeface()
            assertTrue("Skiko typeface must work", skikoTypeface != null)
            skikoTypeface!!
            assertTrue("Skiko typeface name is incorrect: ${skikoTypeface.familyName}", skikoTypeface.familyName == "Verdana")
        }
    }

    @Test
    fun listAllFonts() {
        runTest {
            val fontFiles = AwtFontManager.listFontFiles()
            assertTrue("There must be fonts", fontFiles.size > 0)
        }
    }
}