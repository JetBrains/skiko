package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FontTests {
    @Test
    fun fontTest() = runTest {
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")
        Font(jbMono).use { font ->
            assertEquals(12f, font.size)
            assertCloseEnough(14.880001f, font.spacing)

            val glyphs = font.getStringGlyphs("ABCDE")
            assertContentEquals(shortArrayOf(17, 18, 19, 20, 21), glyphs)

            assertContentEquals(floatArrayOf(7f, 7f, 7f, 7f, 7f), font.getWidths(glyphs))

            assertContentEquals(floatArrayOf(0f, 7f, 14f, 21f, 28f), font.getXPositions(glyphs))
            assertContentEquals(floatArrayOf(3f, 10f, 17f, 24f, 31f), font.getXPositions(glyphs, 3f))
        }
    }
}