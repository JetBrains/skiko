package org.jetbrains.skia.paragraph

import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParagraphCacheTest {
    @Test
    fun cacheInsertionLookupAndLifecycle() = runTest {
        Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf").use { typeface ->
            TypefaceFontProvider().use { manager ->
                manager.registerTypeface(typeface, "Cache Mono")
                FontCollection().use { collection ->
                    collection.setDefaultFontManager(manager, "Cache Mono")
                    ParagraphStyle().use { style ->
                        val cache = collection.paragraphCache
                        assertEquals(0, cache.count)
                        ParagraphBuilder(style, collection).use { builder ->
                            builder.addText("Cached paragraph").build()
                        }.use { paragraph ->
                            paragraph.layout(300f)
                            assertEquals(1, cache.count)

                            cache.reset()
                            assertEquals(0, cache.count)
                            assertFalse(cache.findParagraph(paragraph))
                            assertTrue(cache.updateParagraph(paragraph))
                            assertEquals(1, cache.count)
                            assertFalse(cache.updateParagraph(paragraph))

                            ParagraphBuilder(style, collection).use { builder ->
                                builder.addText("Cached paragraph").build()
                            }.use { sameText ->
                                assertTrue(cache.findParagraph(sameText))
                                cache.setEnabled(false)
                                assertFalse(cache.findParagraph(sameText))
                                assertFalse(cache.updateParagraph(paragraph))
                                cache.setEnabled(true)
                                assertTrue(cache.findParagraph(sameText))

                                cache.printStatistics()
                                assertEquals(1, cache.count)
                                assertTrue(cache.findParagraph(sameText))
                            }

                            cache.abandon()
                            assertEquals(0, cache.count)
                            assertFalse(cache.findParagraph(paragraph))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun printStatisticsPreservesEmptyCache() {
        FontCollection().use { collection ->
            val cache = collection.paragraphCache
            val count = cache.count
            cache.printStatistics()
            assertEquals(count, cache.count)
        }
    }
}
