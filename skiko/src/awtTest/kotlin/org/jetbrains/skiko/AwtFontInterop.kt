package org.jetbrains.skiko

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.jetbrains.skiko.tests.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Font

class AwtFontInterop {
    @Test
    fun canFindFont() = AwtFontManager.whenAllCachedBlocking {
        val font = Font("Verdana", Font.BOLD, 12)
        val path = AwtFontManager.findFontFile(font)
        assertTrue("Font must be found", path != null)
        path!!
        assertTrue("Font must be file", path.exists() && path.isFile)
    }

    @Test
    fun canFindFontSuspend() {
        runTest {
            AwtFontManager.waitCached()
            val font = Font("Verdana", Font.BOLD, 12)
            val path = AwtFontManager.findFontFile(font)
            assertTrue("Font must be found", path != null)
            path!!
            assertTrue("Font must be file", path.exists() && path.isFile)
        }
    }

    @Test
    fun nonExistentFont() = AwtFontManager.whenAllCachedBlocking {
        val font = Font("XXXYYY745", Font.BOLD, 12)
        val path = AwtFontManager.findFontFile(font)
        assertTrue("Font must not be found", path == null)
    }
}