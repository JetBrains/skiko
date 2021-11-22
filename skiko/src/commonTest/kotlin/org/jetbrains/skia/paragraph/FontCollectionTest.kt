package org.jetbrains.skia.paragraph

import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FontCollectionTest {

    @Test
    fun fontCollectionTest() = runTest {
        val fm = TypefaceFontProvider()
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf", 0)
        fm.registerTypeface(jbMono)

        FontCollection().use { fontCollection ->
            fontCollection.setAssetFontManager(fm)
            val typefaces = fontCollection.findTypefaces(arrayOf("JetBrains Mono"), FontStyle.ITALIC)

            typefaces.first()!!.let { typeface ->
                assertEquals("JetBrains Mono", typeface.familyName)
                assertEquals(FontStyle.NORMAL, typeface.fontStyle)
            }
        }
    }
}