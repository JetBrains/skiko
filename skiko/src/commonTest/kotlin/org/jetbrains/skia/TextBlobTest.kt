package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TextBlobTest {

    private val inter36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)
    }

    private val eps = 0.001f // only this value works for kotlin/js

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

        assertNotEquals(
            illegal = 0,
            actual = textBlob.uniqueId,
            message = "uniqueId should return a non-zero value unique among all text blobs."
        )

        assertContentEquals(
            expected = glyphs,
            actual = textBlob.glyphs
        )

        assertContentEquals(
            expected = Point.flattenArray(positions),
            actual = textBlob.positions
        )

        assertCloseEnough(
            expected = Rect(-26.59091f, -39.272827f, 318.27557f, 11.505432f),
            actual = textBlob.bounds,
            epsilon = eps
        )

        assertContentCloseEnough(
            expected = floatArrayOf(
                3.2215908f, 19.585226f, 28.761364f, 40.38493f, 50.761364f, 63.698864f, 71.76136f, 81.017044f, 97.01448f,
                102.99094f, 132.33693f, 134.25397f, 141.45454f, 158.58522f, 173.29857f, 179.27502f, 212.21196f,
                217.8335f, 229.33693f, 231.25397f
            ),
            actual = textBlob.getIntercepts(lowerBound = 0f, upperBound = 1f),
            epsilon = 0.02f // smaller values don't work on k/js :(
        )

//        val data = textBlob.serializeToData()
//        val blobFromData = TextBlob.makeFromData(data)!!
//
//        assertContentEquals(
//            expected = glyphs,
//            actual = blobFromData.glyphs
//        )
//
//        assertContentEquals(
//            expected = Point.flattenArray(positions),
//            actual = blobFromData.positions
//        )

//        assertContentEquals(
//            expected = intArrayOf(),
//            actual = textBlob.clusters
//        )
    }
}
