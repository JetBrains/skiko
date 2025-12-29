package org.jetbrains.skia

import org.jetbrains.skia.shaper.TextBlobBuilderRunHandler
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.assertContentCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.tests.runTest
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TextBlobBuilderTest {

    private val inter36: suspend () -> Font = suspend {
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)
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
            expected = glyphs,
            actual = textBlob.glyphs
        )

        // We don't make assertions because bounds vary on platforms. So at least ensure this doesn't throw an exception.
        val bounds = textBlob.bounds
    }

    @Test
    fun canAppendRunWithTextAndBounds() = runTest {
        val textBlob = TextBlobBuilder().appendRun(
            font = inter36(),
            text = "Привет!Hello!",
            x = 0f,
            y = 0f,
            bounds = floatArrayOf(0f, 0f, 50f, 40f)
        ).build()!!

        assertContentEquals(
            expected = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326),
            actual = textBlob.glyphs
        )

        assertCloseEnough(
            expected = Rect(0f, 0f, 50f, 40f),
            textBlob.bounds
        )
    }

    @Test
    fun canAppendRunWithPositionH() = runTest {
        val font = inter36()
        val str = "Привет!Hello!"
        val glyphs = font.getStringGlyphs(str)
        assertEquals(str.length, glyphs.size)

        val textBlob = TextBlobBuilder().appendRunPosH(
            font = font,
            glyphs = glyphs,
            xs = FloatArray(glyphs.size) { (it + 1) * 10f },
            y = 5f,
            bounds = floatArrayOf(0f, 0f, 50f, 40f)
        ).build()!!

        assertContentEquals(
            expected = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326),
            actual = textBlob.glyphs
        )

        assertContentEquals(
            expected = FloatArray(glyphs.size) { (it + 1) * 10f  },
            actual = textBlob.positions
        )

        assertCloseEnough(
            expected = Rect(0f, 0f, 50f, 40f),
            textBlob.bounds
        )
    }

    @Test
    fun canAppendRunWithPosition() = runTest {
        val font = inter36()
        val str = "Привет!Hello!"
        val glyphs = font.getStringGlyphs(str)
        assertEquals(str.length, glyphs.size)

        val positions = FloatArray(glyphs.size * 2) {
            if (it % 2 == 1) it + 1f else (it + 1) * 10f
        }.toList().chunked(2).map { Point(it[0], it[1]) }.toTypedArray()

        val textBlob = TextBlobBuilder().appendRunPos(
            font = font,
            glyphs = glyphs,
            pos = positions,
            bounds = floatArrayOf(0f, 0f, 150f, 40f),
        ).build()!!

        assertContentEquals(
            expected = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326),
            actual = textBlob.glyphs
        )

        assertContentEquals(
            expected = positions,
            actual = textBlob.positions.toList().chunked(2).map { Point(it[0], it[1]) }.toTypedArray()
        )

        assertCloseEnough(
            expected = Rect(0f, 0f, 150f, 40f),
            textBlob.bounds
        )
    }

    @Test
    fun simpleTextBlobBuilderRunHandler() {
        val handler = TextBlobBuilderRunHandler("Some text")
        handler.makeBlob()
        handler.close()
        require(handler.isClosed)
    }

    @Test
    fun canAppendRunWithRSXform() = runTest {
        val font = inter36()
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

        val textBlob = TextBlobBuilder().appendRunRSXform(
            font = font,
            glyphs = glyphs,
            xform = rsxForms
        ).build()!!

        assertContentEquals(
            expected = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326),
            actual = textBlob.glyphs
        )

        assertCloseEnough(
            expected = Rect(-243.59294f, -306.74146f, 272.9392f, 173.94711f),
            actual = textBlob.bounds,
            epsilon = 0.2f
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
            actual = textBlob.positions,
            epsilon = .2f
        )
    }

    @Test
    fun canAppendMultipleTimes() = runTest {
        val font = inter36()

        val textBlob = TextBlobBuilder()
            .appendRun(font = font, text = "Привет,", x = 0f, y = 0f)
            .appendRun(font = font, text = "Мир!", x = 0f, y = 50f)
            .appendRun(font = font, text = "Hello", x = 0f, y = 100f)
            .appendRun(font = font, text = "World!", x = 0f, y = 150f)
            .build()!!

        assertContentEquals(
            expected = shortArrayOf(
                1983, 830, 1213, 1205, 638, 1231, 1398, 297, 1213, 830, 1326,
                161, 611, 721, 721, 773, 455, 773, 835, 721, 593, 1326
            ),
            actual = textBlob.glyphs
        )

        // We don't make assertions because bounds vary on platforms. So at least ensure this doesn't throw an exception.
        val bounds = textBlob.bounds
    }
}
