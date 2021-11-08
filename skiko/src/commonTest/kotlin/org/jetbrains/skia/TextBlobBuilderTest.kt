package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

class TextBlobBuilderTest {

    private val inter36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)
    }

    private val eps = when (kotlinBackend) {
        KotlinBackend.JS -> 0.02f // smaller values make tests fail when running in k/js
        else -> 0.00001f
    }

    @Test
    fun canAppendRunWithText() = runTest {
        val glyphs = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326)

        val textBlob = TextBlobBuilder().appendRun(
            font = inter36(),
            text = "Привет!Hello!",
            x = 0f,
            y = 0f
        ).build()!!

        assertContentEquals(
            expected = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326),
            actual = textBlob.glyphs
        )
    }
}
