package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FontFallbackWebTest {

    @Test
    fun testChinese_noFallback() = runTest {
        FontCollection().use {
            it.setDefaultFontManager(FontMgr.default)

            // 0x6C34 = "水"
            val df = it.defaultFallback(0x6C34, FontStyle.NORMAL, null)
            assertEquals(null, df)
        }
    }
}