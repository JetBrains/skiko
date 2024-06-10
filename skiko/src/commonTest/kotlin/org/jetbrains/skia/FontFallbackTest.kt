package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FontFallbackTest {

    @Test
    fun testChinese_withFallback() = runTest {
        val fm = TypefaceFontProvider.createAsFallbackProvider()
        val notoSansSC = Typeface.makeFromResource("./fonts/NotoSansSC-Regular.ttf", 0)
        fm.registerTypeface(notoSansSC)

        FontCollection().use {
            it.setDefaultFontManager(FontMgr.defaultWithFallbackFontProvider(fm))

            // 0x6C34 = "水"
            val df = it.defaultFallback(0x6C34, FontStyle.NORMAL, null)!!
            val glyphs = df.getStringGlyphs("水")

            if (kotlinBackend.isWeb()) {
                assertEquals("Noto Sans SC", df.familyName)
                assertContentEquals(shortArrayOf(16391), glyphs)
            } else {
                // the actual font can be different on different systems,
                // so just making sure it's not null
                assertNotEquals<String?>(null, df.familyName)
            }
        }
    }
}