package org.jetbrains.skiko

import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Font

class AwtFontInterop {
    @Test
    fun canFindFont() = AwtFontManager.whenAllCached {
        val font = Font("Verdana", Font.BOLD, 12)
        val path = AwtFontManager.findFontFile(font)
        assertTrue("Font must be found", path != null)
        path!!
        assertTrue("Font must be file", path.exists() && path.isFile)
    }
}