package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertContentEquals

class FontTest {

    @Test
    fun getUTF32GlyphsTest() {
        Font().use { font ->
            assertContentEquals(shortArrayOf(68, 69, 70), font.getStringGlyphs("abc"))
            assertContentEquals(shortArrayOf(695, 68, 695, 68, 68, 728, 697, 697), font.getStringGlyphs("̆ăaä̧̈"))
            assertContentEquals(shortArrayOf(695, 3858), font.getStringGlyphs("̆☺"))
        }
    }

}