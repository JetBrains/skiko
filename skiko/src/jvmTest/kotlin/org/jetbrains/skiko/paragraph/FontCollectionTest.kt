package org.jetbrains.skiko.paragraph

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FontCollectionTest {

    @Test
    fun fontCollectionTest() = runTest {
        val fm = TypefaceFontProvider()
        val jbMono = Typeface.makeFromResource("JetBrainsMono-Regular.ttf", 0)

        fm.registerTypeface(jbMono)
        val inter = Typeface.makeFromResource("InterHinted-Regular.ttf", 0)
        fm.registerTypeface(inter, "Interface")

        // FontCollection
        val fontCollection = FontCollection()
        fontCollection.setAssetFontManager(fm)
        assertEquals(1L, fontCollection.fontManagersCount)
        assertEquals(2, jbMono.refCount)

        fontCollection.findTypefaces(arrayOf("JetBrains Mono"), FontStyle.NORMAL)[0]!!.use { jbMono2 ->
            assertEquals(4, jbMono.refCount)
            assertEquals(4, jbMono2.refCount)

            fontCollection.findTypefaces(arrayOf("JetBrains Mono"), FontStyle.NORMAL)[0]!!.use { jbMono3 ->
                assertEquals(5, jbMono.refCount)
                assertEquals(5, jbMono2.refCount)
                assertEquals(5, jbMono3.refCount)
            }

            assertEquals(4, jbMono.refCount)
            assertEquals(4, jbMono2.refCount)
        }

            //TODO: commented out assertions seem not to be isolated and are turned off till further investigation
//        assertEquals(3, jbMono.refCount)
        assertContentEquals(arrayOf(), fontCollection.findTypefaces(arrayOf("No Such Font"), FontStyle.NORMAL))
        assertContentEquals(arrayOf(jbMono), fontCollection.findTypefaces(arrayOf("JetBrains Mono"), FontStyle.NORMAL))
        assertContentEquals(arrayOf(), fontCollection.findTypefaces(arrayOf("Inter"), FontStyle.NORMAL))
        assertContentEquals(arrayOf(inter), fontCollection.findTypefaces(arrayOf("Interface"), FontStyle.NORMAL))
        assertContentEquals(
            arrayOf(jbMono, inter),
            fontCollection.findTypefaces(arrayOf("JetBrains Mono", "Interface"), FontStyle.NORMAL)
        )

        val defaultFM = FontMgr.default
        fontCollection.setDefaultFontManager(defaultFM)
        assertEquals(2L, fontCollection.fontManagersCount)

//        assertEquals(3, defaultFM.refCount)

        fontCollection.fallbackManager.use { ffm ->
            //assertEquals(4, defaultFM.refCount)
            assertEquals(defaultFM, ffm)
        }

//        assertEquals(3, defaultFM.refCount)
        fontCollection.defaultFallback(65 /* A */, FontStyle.NORMAL, "en-US")!!.use { t1 ->
            val refCnt: Int = t1.refCount
            fontCollection.defaultFallback(65 /* A */, FontStyle.NORMAL, "en-US")!!.use { t2 ->
                assertEquals(refCnt + 1, t1.refCount)
                assertEquals(refCnt + 1, t2.refCount)
                assertEquals(t1, t2)
            }
            assertEquals(refCnt, t1.refCount)
        }
    }
}