package org.jetbrains.skiko

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.paragraph.FontCollection
import org.junit.Test

class ParagraphTest {
    @Test
    fun findTypefaces() {
        val fontCollection = FontCollection().setDefaultFontManager(FontMgr.default)
        fontCollection.findTypefaces(emptyArray(), FontStyle.NORMAL)
    }
}