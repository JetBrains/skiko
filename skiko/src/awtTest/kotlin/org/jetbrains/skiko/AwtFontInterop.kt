package org.jetbrains.skiko

import org.jetbrains.skiko.tests.runTest
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.awt.Font
import java.awt.GraphicsEnvironment

class AwtFontInterop {
    fun assumeOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless())
        Assume.assumeTrue(hostOs != OS.Linux)
    }

    @Test
    fun canFindAvailableFont() = AwtFontManager.whenAllFontsCachedBlocking {
        assumeOk()
        val font = Font("Verdana", Font.BOLD, 12)
        val path = AwtFontManager.findAvailableFontFile(font)
        assertTrue("Font must be found", path != null)
        path!!
        assertTrue("Font must be file", path.exists() && path.isFile)
    }

    @Test
    fun canFindFont() {
        runTest {
            assumeOk()
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
            assumeOk()
            val path = AwtFontManager.findFontFamilyFile("Verdana")
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @Test
    fun nonExistentFont() = AwtFontManager.whenAllFontsCachedBlocking {
        assumeOk()
        val font = Font("XXXYYY745", Font.BOLD, 12)
        val path = AwtFontManager.findAvailableFontFile(font)
        assertTrue("Font must not be found", path == null)
    }

    @Test
    fun makeSkikoTypeface() {
        runTest {
            assumeOk()
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
            assumeOk()
            val fontFiles = AwtFontManager.listFontFiles()
            assertTrue("There must be fonts", fontFiles.isNotEmpty())
        }
    }

    @Test
    fun addCustomPath() {
        runTest {
            assumeOk()
            val resDir = System.getProperty("skiko.test.font.dir")!!
            AwtFontManager.addCustomPath(resDir)
            AwtFontManager.invalidate()
            val path = AwtFontManager.findFontFamilyFile("JetBrains Mono")
            assertTrue("Custom font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }
}