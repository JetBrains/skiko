package org.jetbrains.skia

import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ShaperTest {

    private suspend fun fontInter36() =
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)

    @Test
    fun canShapeLine() = runTest {
        val textLine = Shaper.make().shapeLine(
            text = "Abc123", font = fontInter36()
        )

        assertEquals(6, textLine.glyphsLength)

        assertContentEquals(
            expected = shortArrayOf(2, 574, 581, 1292, 1293, 1295),
            actual = textLine.glyphs
        )
    }

    @Test
    fun canShapeTextBlob() = runTest {
        val textBlob = Shaper.make().shape(
            text = "text",
            font = fontInter36(),
            width = 100f
        )

        assertNotEquals(null, textBlob)
        assertEquals(4, textBlob!!.glyphsLength)
        assertContentEquals(
            expected = shortArrayOf(882, 611, 943, 882),
            actual = textBlob.glyphs
        )
    }

}
