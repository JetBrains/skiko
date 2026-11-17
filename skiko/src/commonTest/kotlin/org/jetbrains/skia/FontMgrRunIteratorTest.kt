package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.shaper.FontMgrRunIterator
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class FontMgrRunIteratorTest {
    @Test
    fun returnsFontAndEndOfRun() = runTest {
        Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf").use { typeface ->
            Font(typeface, 36f).use { font ->
                val text = "Abc123"
                FontMgrRunIterator(text, font).use { iterator ->
                    assertTrue(iterator.hasNext())
                    val run = iterator.next()
                    assertEquals(text.length, run.end)
                    run.font.use { currentFont ->
                        assertEquals(font, currentFont)
                        assertEquals(36f, currentFont.size)
                        assertContentEquals(font.getStringGlyphs(text), currentFont.getStringGlyphs(text))
                    }
                    assertFalse(iterator.hasNext())
                }
            }
        }
    }
}
