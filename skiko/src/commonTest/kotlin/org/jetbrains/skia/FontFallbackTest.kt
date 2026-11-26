package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TypefaceFontProviderWithFallback
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FontFallbackTest {

    @Test
    fun clearCachesMakesNewFallbackFontVisible() = runTest {
        // System fonts on other platforms can resolve the character before registration.
        if (!kotlinBackend.isWeb()) return@runTest

        val codePoint = '水'.code
        val notoSansSC = Typeface.makeFromResource("./fonts/NotoSansSC-Regular.ttf", 0)
        val fm = TypefaceFontProviderWithFallback()

        FontCollection().use { collection ->
            collection.setDefaultFontManager(FontMgrWithFallback(fm))
            assertNull(collection.defaultFallback(codePoint, null, FontStyle.NORMAL, null))

            fm.registerTypeface(notoSansSC)
            collection.clearCaches()

            val fallback = collection.defaultFallback(codePoint, null, FontStyle.NORMAL, null)
            assertNotNull(fallback).use { assertEquals(notoSansSC.uniqueId, it.uniqueId) }
        }
    }

    @Test
    fun testChinese_withFallback() = runTest {
        val fm = TypefaceFontProviderWithFallback()
        val notoSansSC = Typeface.makeFromResource("./fonts/NotoSansSC-Regular.ttf", 0)
        fm.registerTypeface(notoSansSC)

        FontCollection().use {
            it.setDefaultFontManager(FontMgrWithFallback(fm))

            // 0x6C34 = "水"
            val df = it.defaultFallback(0x6C34, null, FontStyle.NORMAL, null)!!
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
