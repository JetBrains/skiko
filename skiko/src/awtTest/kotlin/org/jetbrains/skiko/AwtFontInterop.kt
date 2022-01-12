package org.jetbrains.skiko

import org.jetbrains.skiko.tests.runTest
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.awt.Font
import java.awt.GraphicsEnvironment

class AwtFontInterop {
    @Test
    fun canFindAvailableFont() = AwtFontManager.whenAllFontsCachedBlocking {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless())
        val font = Font("Verdana", Font.BOLD, 12)
        val path = AwtFontManager.findAvailableFontFile(font)
        assertTrue("Font must be found", path != null)
        path!!
        assertTrue("Font must be file", path.exists() && path.isFile)
    }

    @Test
    fun canFindFont() {
        runTest {
            Assume.assumeFalse(GraphicsEnvironment.isHeadless())
            val font = Font("Verdana", Font.BOLD, 12)
            val path = AwtFontManager.findFontFile(font)
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @Test
    fun canFindFamily() {
        runTest {
            Assume.assumeFalse(GraphicsEnvironment.isHeadless())
            val path = AwtFontManager.findFontFamilyFile("Verdana")
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @Test
    fun nonExistentFont() = AwtFontManager.whenAllFontsCachedBlocking {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless())
        val font = Font("XXXYYY745", Font.BOLD, 12)
        val path = AwtFontManager.findAvailableFontFile(font)
        assertTrue("Font must not be found", path == null)
    }

    @Test
    fun makeSkikoTypeface() {
        runTest {
            Assume.assumeFalse(GraphicsEnvironment.isHeadless())
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
            Assume.assumeFalse(GraphicsEnvironment.isHeadless())
            val fontFiles = AwtFontManager.listFontFiles()
            assertTrue("There must be fonts", fontFiles.size > 0)
        }
    }
}