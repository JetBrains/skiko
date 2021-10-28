package org.jetbrains.skia

import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skia.util.assertContentSame
import org.jetbrains.skia.util.imageFromIntArray
import org.jetbrains.skia.util.printBitmap
import org.jetbrains.skiko.tests.SkipJsTarget
import org.jetbrains.skiko.tests.SkipNativeTarget
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class CanvasTest {
    @Test
    fun drawVertices() {
        val surface = Surface.makeRasterN32Premul(8, 8)

        val positions = Point.flattenArray(
            arrayOf(
                Point(0.0f, 0.0f),
                Point(4.0f, 4.0f),
                Point(8.0f, 0.0f),
                Point(4.0f, 4.0f),
                Point(0.0f, 8.0f),
                Point(8.0f, 8.0f),
            )
        )!!

        val colors = listOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW
        ).toIntArray()

        val indices = shortArrayOf(0, 1, 2, 3, 4, 5)

        surface.canvas.drawVertices(
            VertexMode.TRIANGLES,
            positions,
            colors,
            positions,
            indices,
            BlendMode.SRC_OVER,
            Paint()
        )

        // Hint: use surface.makeImageSnapshot().printBitmap() to print image as kotlin array
        val expected = imageFromIntArray(intArrayOf(
            0x00000000.toInt(), 0xffbf2020.toInt(), 0xff9f2040.toInt(), 0xff802060.toInt(), 0xff602080.toInt(), 0xff40209f.toInt(), 0xff2020bf.toInt(), 0xff0020df.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0xff806020.toInt(), 0xff606040.toInt(), 0xff406060.toInt(), 0xff206080.toInt(), 0xff00609f.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0xff409f20.toInt(), 0xff209f40.toInt(), 0xff009f60.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0xff00df20.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0xff20ffdf.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0xff60bfdf.toInt(), 0xff60dfbf.toInt(), 0xff60ff9f.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0xff9f80df.toInt(), 0xff9f9fbf.toInt(), 0xff9fbf9f.toInt(), 0xff9fdf80.toInt(), 0xff9fff60.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0xffdf40df.toInt(), 0xffdf60bf.toInt(), 0xffdf809f.toInt(), 0xffdf9f80.toInt(), 0xffdfbf60.toInt(), 0xffdfdf40.toInt(), 0xffdfff20.toInt(),
        ), surface.width)

        assertContentSame(expected, surface.makeImageSnapshot(), 0.25)
    }

    @Test
    fun drawPoints() {
        val surface = Surface.makeRasterN32Premul(8, 8)

        surface.canvas.drawPoints(
            paint = Paint().apply {
                setStroke(true)
                setARGB(0xFF, 0xFF, 0x00, 0x00)
                strokeWidth = 2.0f
            },
            coords = arrayOf(
                Point(1.0f, 1.0f),
                Point(4.0f, 4.0f),
                Point(3.0f, 5.0f),
                Point(2.0f, 6.0f),
                Point(8.0f, 8.0f),
            )
        )

        val expected = imageFromIntArray(intArrayOf(
            0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
            0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0xffff0000.toInt(),
        ), surface.width)

        assertContentSame(expected, surface.makeImageSnapshot(), 0.25)
    }

    private suspend fun fontInter36() =
        Font(Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf"), 36f)

    // TODO(karpovich): enable for all platforms
    @Test @SkipNativeTarget @SkipJsTarget // native and js don't work: resulting image has no changed pixels
    fun drawString() = runTest {
        val surface = Surface.makeRasterN32Premul(100, 100)

        val bytes =  Bitmap.makeFromImage(surface.makeImageSnapshot()).readPixels()!!
        assertTrue {
            bytes.isNotEmpty() && bytes.all { it == 0.toByte() }
        }

        surface.canvas.drawString(
            s = "Hello world!",
            x = 10f, y = 10f,
            font = fontInter36(),
            paint = Paint().apply {
                color = Color.RED
                setStroke(false)
            }
        )

        val bytes2 =  Bitmap.makeFromImage(surface.makeImageSnapshot()).readPixels()!!
        assertTrue {
            bytes2.isNotEmpty() && bytes2.any { it != 0.toByte() }
        }
    }
}
