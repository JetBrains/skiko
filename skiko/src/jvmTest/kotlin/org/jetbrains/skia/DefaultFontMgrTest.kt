package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.tests.runTest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultFontMgrTest {
    @Test
    fun defaultManagerSurvivesReleasingAllCallerReferences() {
        Library.load()
        assertTrue(checkDefaultFontManagerLifetime())
    }

    @Test
    fun defaultManagerIsSharedAcrossThreads() {
        Library.load()
        assertTrue(checkConcurrentDefaultFontManagerAccess())
    }

    @Test
    fun independentShortLivedShapersCanUseFallbackConcurrently() = runTest {
        Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf").use { typeface ->
            val samples = listOf("text", "中文", "🙂", "العربية עברית")
            val expected = Font(typeface, 36f).use { font ->
                Shaper.makeShapeDontWrapOrReorder().use { shaper ->
                    samples.map { text ->
                        shaper.shapeLine(text, font).use { line ->
                            assertNotNull(shaper.shape(text, font)).use { blob ->
                                Triple(line.glyphs, line.width, blob.glyphs)
                            }
                        }
                    }
                }
            }
            val executor = Executors.newFixedThreadPool(8)
            try {
                val tasks = List(8) {
                    executor.submit(Callable {
                        Font(typeface, 36f).use { font ->
                            repeat(25) {
                                Shaper.makeShapeDontWrapOrReorder().use { shaper ->
                                    samples.forEachIndexed { index, text ->
                                        shaper.shapeLine(text, font).use { line ->
                                            assertContentEquals(expected[index].first, line.glyphs)
                                            assertEquals(expected[index].second, line.width)
                                        }
                                        assertNotNull(shaper.shape(text, font)).use { blob ->
                                            assertContentEquals(expected[index].third, blob.glyphs)
                                        }
                                    }
                                }
                            }
                        }
                    })
                }
                tasks.forEach { it.get(60, TimeUnit.SECONDS) }
            } finally {
                executor.shutdownNow()
                // Keep the typeface alive until all workers finish, even if a check fails.
                assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS))
            }
        }
    }
}

private external fun checkDefaultFontManagerLifetime(): Boolean
private external fun checkConcurrentDefaultFontManagerAccess(): Boolean
