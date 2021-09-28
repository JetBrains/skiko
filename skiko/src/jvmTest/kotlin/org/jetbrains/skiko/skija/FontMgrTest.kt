package org.jetbrains.skiko.skija

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class FontMgrTest {
    @Test
    fun fontMgrTest() {
        val fontManager = TypefaceFontProvider()

        val jbMono = Typeface.makeFromFile("src/jvmTest/resources/fonts/JetBrainsMono-Regular.ttf", 0)
        fontManager.registerTypeface(jbMono)

        val jbMonoBold = Typeface.makeFromFile("src/jvmTest/resources/fonts/JetBrainsMono-Bold.ttf", 0)
        fontManager.registerTypeface(jbMonoBold)

        val inter: Typeface = Typeface.makeFromFile("src/jvmTest/resources/fonts/InterHinted-Regular.ttf", 0)
        fontManager.registerTypeface(inter, "Interface")

        assertEquals(2, fontManager.familiesCount)
        assertEquals("JetBrains Mono", fontManager.getFamilyName(0))
        assertEquals("Interface", fontManager.getFamilyName(1))

        fontManager.makeStyleSet(0).use { styleSet ->
            assertEquals(0, styleSet?.count())
        }

        fontManager.makeStyleSet(1).use { styleSet ->
            assertEquals(0, styleSet?.count())
        }

        fontManager.matchFamily("JetBrains Mono").use { styleSet ->
            assertEquals(2, styleSet.count())
            assertEquals(FontStyle.NORMAL, styleSet.getStyle(0))
            assertEquals("JetBrains Mono", styleSet.getStyleName(0))
            assertEquals(FontStyle.BOLD, styleSet.getStyle(1))
            assertEquals("JetBrains Mono", styleSet.getStyleName(1))
            assertEquals(2, jbMono.refCount)

            styleSet.getTypeface(0).use { face ->
                assertEquals(3, jbMono.refCount)
                assertEquals(jbMono, face)
            }

            assertEquals(2, jbMono.refCount)
            assertEquals(2, jbMonoBold.refCount)

            styleSet.getTypeface(1).use { face ->
                assertEquals(3, jbMonoBold.refCount)
                assertEquals(jbMonoBold, face)
            }

            assertEquals(2, jbMonoBold.refCount)
            assertEquals(2, jbMono.refCount)

            styleSet.matchStyle(FontStyle.NORMAL).use { face ->
                assertEquals(3, jbMono.refCount)
                assertEquals(jbMono, face)
            }

            assertEquals(2, jbMono.refCount)
            assertEquals(2, jbMonoBold.refCount)

            styleSet.matchStyle(FontStyle.BOLD).use { face ->
                assertEquals(3, jbMonoBold.refCount)
                assertEquals(jbMonoBold, face)
            }

            assertEquals(2, jbMonoBold.refCount)
            assertEquals(jbMono, styleSet.matchStyle(FontStyle.ITALIC))
        }

        assertEquals(null, fontManager.matchFamilyStyle("JetBrains Mono", FontStyle.BOLD))
        assertEquals(null, fontManager.matchFamilyStyle("Interface", FontStyle.NORMAL))

        assertEquals(
            null,
            fontManager.matchFamilyStyleCharacter("JetBrains Mono", FontStyle.BOLD, arrayOf("en-US"), 65 /* A */)
        )

        Data.makeFromFileName("src/jvmTest/resources/fonts/JetBrainsMono-Italic.ttf").use { data ->
            fontManager.makeFromData(data).use {
                fontManager.matchFamily("JetBrains Mono").use { styleSet ->
                    assertEquals(2, fontManager.familiesCount)
                    assertEquals(2, styleSet.count())
                }
            }
        }

        fontManager.close()
    }
}