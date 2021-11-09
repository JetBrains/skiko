package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FonTests {
    @Test
    fun fontTest() = runTest {
        val jbMono = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")
        Font(jbMono).use { font ->
            assertEquals(12f, font.size)
        }
    }
}