package org.jetbrains.skia.paragraph

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FontCollectionTest {

    @Test
    fun managerSlotsAndFallbackCanBeChanged() {
        TypefaceFontProvider().use { manager ->
            FontCollection().use { collection ->
                assertEquals(0, collection.fontManagersCount)
                collection.setDynamicFontManager(manager)
                assertEquals(1, collection.fontManagersCount)
                collection.setTestFontManager(manager)
                assertEquals(2, collection.fontManagersCount)
                collection.setDefaultFontManager(manager)
                assertEquals(3, collection.fontManagersCount)

                collection.setEnableFallback(false)
                assertEquals(2, collection.fontManagersCount)
                collection.setEnableFallback(true)
                assertEquals(3, collection.fontManagersCount)

                collection.setDynamicFontManager(null)
                assertEquals(2, collection.fontManagersCount)
                collection.setTestFontManager(null)
                assertEquals(1, collection.fontManagersCount)
            }
        }
    }

    @Test
    fun defaultFallbackUsesConfiguredFamily() = runTest {
        Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf").use { typeface ->
            TypefaceFontProvider().use { manager ->
                manager.registerTypeface(typeface, "Coverage Mono")
                FontCollection().use { collection ->
                    assertNull(collection.defaultFallback())
                    collection.setDefaultFontManager(manager, "Coverage Mono")
                    assertNotNull(collection.defaultFallback()).use { fallback ->
                        assertEquals(typeface.familyName, fallback.familyName)
                        assertEquals(typeface.uniqueId, fallback.uniqueId)
                    }
                }
            }
        }
    }

    @Test
    fun familyNameTest() = runTest {
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

    @Test
    fun fontCollectionTest() = runTest {
        val fm = TypefaceFontProvider()
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf", 0)

        fm.registerTypeface(jbMono)
        val inter = Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf", 0)
        fm.registerTypeface(inter, "Interface")

        // FontCollection
        val fontCollection = FontCollection()
        fontCollection.setAssetFontManager(fm)
        assertEquals(1, fontCollection.fontManagersCount)
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

        // Note: thanks to https://skia-review.googlesource.com/c/skia/+/834816 there is a fallback,
        // therefore the result is non-empty array (it used to be empty)
        assertContentEquals(arrayOf(jbMono), fontCollection.findTypefaces(arrayOf("No Such Font"), FontStyle.NORMAL))

        assertContentEquals(arrayOf(jbMono), fontCollection.findTypefaces(arrayOf("JetBrains Mono"), FontStyle.NORMAL))

        // Note: thanks to https://skia-review.googlesource.com/c/skia/+/834816 there is a fallback,
        // therefore the result is non-empty array (it used to be empty)
        assertContentEquals(arrayOf(jbMono), fontCollection.findTypefaces(arrayOf("Inter"), FontStyle.NORMAL))

        assertContentEquals(arrayOf(inter), fontCollection.findTypefaces(arrayOf("Interface"), FontStyle.NORMAL))
        assertContentEquals(
            arrayOf(jbMono, inter),
            fontCollection.findTypefaces(arrayOf("JetBrains Mono", "Interface"), FontStyle.NORMAL)
        )

        val defaultFM = FontMgr.default
        fontCollection.setDefaultFontManager(defaultFM)
        assertEquals(2, fontCollection.fontManagersCount)


        fontCollection.fallbackManager!!.use { ffm ->
            //assertEquals(4, defaultFM.refCount)
            assertEquals(defaultFM, ffm)
        }

        if (kotlinBackend.isNotJs()) {
            fontCollection.defaultFallback(65 /* A */, null, FontStyle.NORMAL, "en-US")!!.use { t1 ->
                val refCnt: Int = t1.refCount
                fontCollection.defaultFallback(65 /* A */, null, FontStyle.NORMAL, "en-US")!!.use { t2 ->
                    assertEquals(refCnt + 1, t1.refCount)
                    assertEquals(refCnt + 1, t2.refCount)
                    assertEquals(t1, t2)
                }
            }
        }

    }

}
