package org.jetbrains.skia

import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotEquals

class TextBlobTest {

    private val inter36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)
    }

    @Test
    fun canMakeFromPos() = runTest {
        val glyphs = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326)

        val positions = listOf(
            0.0f, 0.0f, 26.0f, 0.0f, 48.0f, 0.0f, 69.0f, 0.0f, 89.0f, 0.0f, 109.28409f, 0.0f, 128.28409f, 0.0f,
            138.28409f, 0.0f, 165.28409f, 0.0f, 186.28409f, 0.0f, 195.28409f, 0.0f, 204.28409f, 0.0f, 225.28409f, 0.0f
        ).chunked(2).map { Point(it[0], it[1]) }.toTypedArray()

        val textBlob = TextBlob.makeFromPos(
            glyphs = glyphs,
            pos = positions,
            font = inter36()
        )!!

        assertContentEquals(
            expected = glyphs,
            actual = textBlob.glyphs
        )

        assertContentEquals(
            expected = Point.flattenArray(positions),
            actual = textBlob.positions
        )

        val data = textBlob.serializeToData()
        val blobFromData = TextBlob.makeFromData(data)!!

        assertContentEquals(
            expected = glyphs,
            actual = blobFromData.glyphs
        )

        assertContentEquals(
            expected = Point.flattenArray(positions),
            actual = blobFromData.positions
        )

//        assertContentEquals(
//            expected = intArrayOf(),
//            actual = textBlob.clusters
//        )
    }
}
