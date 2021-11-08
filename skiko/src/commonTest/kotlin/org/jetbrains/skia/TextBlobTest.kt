package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.*

class TextBlobTest {

    private val inter36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)
    }

    private val eps = when (kotlinBackend) {
        KotlinBackend.JS -> 0.02f // smaller values make tests fail when running in k/js
        else -> 0.00001f
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
            epsilon = eps // smaller values don't work on k/js :(
        )

        // Ensure can get tightBounds. we don't make assertEquals because platforms' results vary
        val tightBoundsRect = textBlob.tightBounds

        assertCloseEnough(
            expected = Rect(0f, -34.875f, 235.30681f, 8.692932f),
            actual = textBlob.blockBounds.also { println(it) },
            epsilon = 0.2f // smaller values don't work on k/js :(
        )

        assertEquals(0f, textBlob.firstBaseline)
        assertEquals(0f, textBlob.lastBaseline)

        assertFailsWith<IllegalArgumentException> {
            // this TextBlob doesn't have such information
            textBlob.clusters
        }

        val data = textBlob.serializeToData()
        val blobFromData = TextBlob.makeFromData(data)!!
        assertContentEquals(expected = glyphs, actual = blobFromData.glyphs)
        assertContentEquals(expected = Point.flattenArray(positions), actual = blobFromData.positions)
    }

    @Test
    fun canMakeFromPosH() = runTest {
        val glyphs = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326)

        val positions = listOf(
            0.0f, 0.0f, 26.0f, 0.0f, 48.0f, 0.0f, 69.0f, 0.0f, 89.0f, 0.0f, 109.28409f, 0.0f, 128.28409f, 0.0f,
            138.28409f, 0.0f, 165.28409f, 0.0f, 186.28409f, 0.0f, 195.28409f, 0.0f, 204.28409f, 0.0f, 225.28409f, 0.0f
        ).filterIndexed {
            // remove y, leave only x (horizontal positions)
                ix, _ ->
            ix % 2 == 0
        }.toTypedArray().toFloatArray()

        val textBlob = TextBlob.makeFromPosH(
            glyphs = glyphs,
            xpos = positions,
            ypos = 1f,
            font = inter36()
        )!!

        assertNotEquals(
            illegal = 0,
            actual = textBlob.uniqueId,
            message = "uniqueId should return a non-zero value unique among all text blobs."
        )

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
            expected = positions,
            actual = textBlob.positions
        )

        assertCloseEnough(
            expected = Rect(-26.59091f, -38.272827f, 318.27557f, 12.505432f),
            actual = textBlob.bounds,
            epsilon = eps
        )

        assertContentCloseEnough(
            expected = floatArrayOf(
                3.2215908f, 22.75568f, 28.761364f, 42.330772f, 50.761364f, 66.71591f, 71.76136f, 85.03906f, 94.86898f,
                105.11858f, 117.41477f, 120.431816f, 131.26941f, 135.32149f, 141.45454f, 161.75568f, 171.15308f,
                181.40268f, 189.04546f, 192.0625f, 198.04546f, 201.0625f, 210.14375f, 219.9017f, 228.26941f, 232.32149f
            ),
            actual = textBlob.getIntercepts(lowerBound = 0f, upperBound = 1f)!!,
            epsilon = eps
        )

        assertFailsWith<IllegalArgumentException> { textBlob.tightBounds }
        assertFailsWith<IllegalArgumentException> { textBlob.blockBounds }
        assertFailsWith<IllegalArgumentException> { textBlob.firstBaseline }
        assertFailsWith<IllegalArgumentException> { textBlob.lastBaseline }

        val data = textBlob.serializeToData()
        val blobFromData = TextBlob.makeFromData(data)!!
        assertContentEquals(expected = glyphs, actual = blobFromData.glyphs)
        assertContentEquals(expected = positions, actual = blobFromData.positions)
    }

    @Test // https://fiddle.skia.org/c/@Canvas_drawTextRSXform
    fun canMakeFromRSXform() = runTest {

        val glyphs = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326)

        var angle = 0f
        var scale = 1f

        val rsxForms = glyphs.map {
            val s = sin(angle.toDouble()).toFloat() * scale
            val c = cos(angle.toDouble()).toFloat() * scale
            angle += .45f
            scale += .2f
            RSXform(scos = c, ssin = s, tx = -s * 16, ty = c * 16)
        }.toTypedArray()

        val textBlob = TextBlob.makeFromRSXform(
            glyphs = glyphs,
            xform = rsxForms,
            font = inter36()
        )!!

        assertNotEquals(
            illegal = 0,
            actual = textBlob.uniqueId,
            message = "uniqueId should return a non-zero value unique among all text blobs."
        )

        assertNotEquals(
            illegal = 0,
            actual = textBlob.uniqueId,
            message = "uniqueId should return a non-zero value unique among all text blobs."
        )

        assertContentEquals(
            expected = glyphs,
            actual = textBlob.glyphs
        )

        assertContentCloseEnough(
            expected = floatArrayOf(
                1.0f, 0.0f, -0.0f, 16.0f, 1.0805366f, 0.52195865f, -8.351338f, 17.288586f, 0.87025404f, 1.0966578f,
                -17.546524f, 13.924065f, 0.35041088f, 1.5611575f, -24.97852f, 5.606574f, -0.4089637f, 1.7529259f,
                -28.046814f, -6.5434194f, -1.2563474f, 1.5561466f, -24.898346f, -20.101559f, -1.9889591f, 0.9402358f,
                -15.043773f, -31.823345f, -2.3999155f, -0.020177627f, 0.32284203f, -38.398647f, -2.331572f, -1.1505536f,
                18.408857f, -37.305153f, -1.7220064f, -2.2078712f, 35.32594f, -27.552103f, -0.6323875f, -2.9325907f,
                46.92145f, -10.1182f, 0.75322014f, -3.1100905f, 49.761448f, 12.051522f, 2.1579552f, -2.6274006f,
                42.03841f, 34.527283f
            ),
            actual = textBlob.positions.also { println(it.joinToString()) },
            epsilon = eps
        )

        assertCloseEnough(
            expected = Rect(-243.59294f, -306.74146f, 272.9392f, 173.94711f),
            actual = textBlob.bounds,
            epsilon = eps
        )

        // These values are not available for this textBlob
        assertFailsWith<IllegalArgumentException> { textBlob.tightBounds }
        assertFailsWith<IllegalArgumentException> { textBlob.blockBounds }
        assertFailsWith<IllegalArgumentException> { textBlob.firstBaseline }
        assertFailsWith<IllegalArgumentException> { textBlob.lastBaseline }

        val data = textBlob.serializeToData()
        val blobFromData = TextBlob.makeFromData(data)!!
        assertContentEquals(expected = glyphs, actual = blobFromData.glyphs)
    }
}
