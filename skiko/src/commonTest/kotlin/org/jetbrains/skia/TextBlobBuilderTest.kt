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
            bounds = Rect(0f, 0f, 50f, 40f)
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
            bounds = Rect(0f, 0f, 50f, 40f),
            xs = FloatArray(glyphs.size) { (it + 1) * 10f },
            y = 5f
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

        val textBlob = TextBlobBuilder().appendRunPos(
            font = font,
            glyphs = glyphs,
            bounds = Rect(0f, 0f, 150f, 40f),
            pos = FloatArray(glyphs.size * 2) {
                if (it % 2 == 1) 1f else (it + 1) * 10f
            }.toList().chunked(2).map { Point(it[0], it[1]) }.toTypedArray()
        ).build()!!

        assertContentEquals(
            expected = shortArrayOf(1983, 830, 1213, 1205, 638, 1231, 1326, 161, 611, 721, 721, 773, 1326),
            actual = textBlob.glyphs
        )

        assertCloseEnough(
            expected = Rect(0f, 0f, 150f, 40f),
            textBlob.bounds
        )
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

        assertCloseEnough(
            expected = Rect(-1f, -28f, 138.784098f, 152f),
            actual = textBlob.bounds.also { println(it) },
            epsilon = 0.2f
        )
    }
}
