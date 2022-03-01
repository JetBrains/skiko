package org.jetbrains.skiko

import org.jetbrains.skiko.tests.runTest
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.awt.Font
import java.awt.GraphicsEnvironment

class AwtFontInterop {
    private val fontManager = AwtFontManager()

    private fun assumeOk() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless())
        Assume.assumeTrue(hostOs != OS.Linux)
    }

    @OptIn(DelicateSkikoApi::class)
    @Test
    fun canFindAvailableFont() = fontManager.whenAllFontsCachedBlocking {
        assumeOk()
        val font = Font("Verdana", Font.BOLD, 12)
        val path = fontManager.findAvailableFontFile(font)
        assertTrue("Font must be found", path != null)
        path!!
        assertTrue("Font must be file", path.exists() && path.isFile)
    }

    @Test
    fun canFindFont() {
        runTest {
            assumeOk()
            val font = Font("Verdana", Font.BOLD, 12)
            val path = fontManager.findFontFile(font)
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @Test
    fun canFindFamily() {
        runTest {
            assumeOk()
            val path = fontManager.findFontFamilyFile("Verdana")
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @OptIn(DelicateSkikoApi::class)
    @Test
    fun nonExistentFont() = fontManager.whenAllFontsCachedBlocking {
        assumeOk()
        val font = Font("XXXYYY745", Font.BOLD, 12)
        val path = fontManager.findAvailableFontFile(font)
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
            val fontFiles = fontManager.listFontFiles()
            assertTrue("There must be fonts", fontFiles.isNotEmpty())
        }
    }

    @Test
    fun addCustomPath() {
        runTest {
            assumeOk()
            val resDir = System.getProperty("skiko.test.font.dir")!!
            fontManager.addCustomPath(resDir)
            fontManager.invalidate()
            val path = fontManager.findFontFamilyFile("JetBrains Mono")
            assertTrue("Custom font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    // This test is disabled due to convoluted setup of tests.
    // @Test
    fun addCustomResource() {
        runTest {
            assumeOk()
            val fontManager = AwtFontManager()
            assertTrue("Custom resource must be found",
                fontManager.addResourceFont("/fonts/JetBrainsMono-Bold.ttf", Library.javaClass.classLoader))
            val path = fontManager.findFontFamilyFile("JetBrains Mono")
            assertTrue("Custom font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }
}