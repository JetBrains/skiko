package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FontMgrTest {
    @Test
    fun fontMgrTest() = runTest {

        TypefaceFontProvider().let { outerFontManager ->
            val fontManager = TypefaceFontProvider()

            val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")
            fontManager.registerTypeface(jbMono)

            val jbMonoBold = Typeface.makeFromResource("./fonts/JetBrainsMono-Bold.ttf")
            fontManager.registerTypeface(jbMonoBold)

            val inter: Typeface = Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf")
            fontManager.registerTypeface(inter, "Interface")

            assertEquals(2, fontManager.familiesCount)
            assertEquals("JetBrains Mono", fontManager.getFamilyName(0))
            assertEquals("Interface", fontManager.getFamilyName(1))

            fontManager.makeStyleSet(0)!!.use { styleSet ->
                // assert was changed after update to m126 due to
                // https://skia-review.googlesource.com/c/skia/+/834816
                assertEquals(2, styleSet.count())
            }

            fontManager.makeStyleSet(1)!!.use { styleSet ->
                // assert was changed after update to m126 due to
                // https://skia-review.googlesource.com/c/skia/+/834816
                assertEquals(1, styleSet.count())
            }

            fontManager.matchFamily("JetBrains Mono").use { styleSet ->
                assertEquals(2, styleSet.count())
                assertEquals(FontStyle.NORMAL, styleSet.getStyle(0))
                assertEquals("JetBrains Mono", styleSet.getStyleName(0))
                assertEquals(FontStyle.BOLD, styleSet.getStyle(1))
                assertEquals("JetBrains Mono", styleSet.getStyleName(1))

                assertEquals(2, jbMono.refCount)

                styleSet.getTypeface(0)!!.use { face ->
                    assertEquals(3, jbMono.refCount)
                    assertEquals(jbMono, face)
                }

                assertEquals(2, jbMono.refCount)
                assertEquals(2, jbMonoBold.refCount)

                styleSet.getTypeface(1)!!.use { face ->
                    assertEquals(3, jbMonoBold.refCount)
                    assertEquals(jbMonoBold, face)
                }

                assertEquals(2, jbMonoBold.refCount)
                assertEquals(2, jbMono.refCount)

                styleSet.matchStyle(FontStyle.NORMAL)!!.use { face ->
                    assertEquals(3, jbMono.refCount)
                    assertEquals(jbMono, face)
                }

                assertEquals(2, jbMono.refCount)
                assertEquals(2, jbMonoBold.refCount)

                styleSet.matchStyle(FontStyle.BOLD)!!.use { face ->
                    assertEquals(3, jbMonoBold.refCount)
                    assertEquals(jbMonoBold, face)
                }

                assertEquals(2, jbMonoBold.refCount)
                assertEquals(jbMono, styleSet.matchStyle(FontStyle.ITALIC))
            }

            assertNull(outerFontManager.matchFamilyStyle("JetBrains Mono", FontStyle.BOLD))
            assertNull(outerFontManager.matchFamilyStyle("Interface", FontStyle.NORMAL))

            // TODO: it would be definitely beneficial to check the notNull branch as well
            assertNull(
                outerFontManager.matchFamilyStyleCharacter("JetBrains Mono", FontStyle.BOLD, arrayOf("en-US"), 65 /* A */)
            )

        }
    }


    @Test
    @SkipJsTarget
    @SkipWasmTarget
    @SkipNativeTarget
    fun makeFromDataTest() {
        makeFromFileName("src/commonTest/resources/fonts/JetBrainsMono-Italic.ttf").use { data ->
            FontMgr.default.makeFromData(data)!!.use { typeFace ->
                assertEquals("JetBrains Mono", typeFace.familyName)
                assertEquals(FontStyle.ITALIC, typeFace.fontStyle)
            }
        }
    }
}
