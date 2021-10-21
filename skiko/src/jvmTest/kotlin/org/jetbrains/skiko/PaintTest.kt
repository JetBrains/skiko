package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.jetbrains.skiko.util.loadResourceImage
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PaintTest {
    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun filterQuality() {
        // macOs has different results
        assumeTrue(hostOs.isWindows)

        val surface = Surface.makeRasterN32Premul(16, 16)

        surface.canvas.drawImageRect(
            image = loadResourceImage("test.png"),
            src = Rect.makeXYWH(0f, 2f, 2f, 4f),
            dst = Rect.makeXYWH(0f, 4f, 4f, 12f),
            samplingMode = FilterMipmap(FilterMode.NEAREST, MipmapMode.NONE),
            Paint(),
            true
        )
        surface.canvas.drawImageRect(
            image = loadResourceImage("test.png"),
            src = Rect.makeXYWH(0f, 2f, 2f, 4f),
            dst = Rect.makeXYWH(4f, 4f, 4f, 12f),
            samplingMode = FilterMipmap(FilterMode.LINEAR, MipmapMode.NONE),
            Paint(),
            true
        )
        surface.canvas.drawImageRect(
            image = loadResourceImage("test.png"),
            src = Rect.makeXYWH(0f, 2f, 2f, 4f),
            dst = Rect.makeXYWH(8f, 4f, 4f, 12f),
            samplingMode = CubicResampler(1 / 3.0f, 1 / 3.0f),
            Paint(),
            true
        )

        screenshots.assert(surface.makeImageSnapshot())
    }

    @Test
    fun paintTest() {
        // TODO: ported from skija and we need address the commented out assertions
        val paintA = Paint().apply { color = 0x12345678 }
        val paintB = Paint().apply { color = 0x12345678 }

        assertEquals(paintA, paintB)
//        assertEquals(paintA.hashCode(), paintB.hashCode(), "hash codes are not identical")
//
//        val paintC = Paint().apply { color = -0xcba988 }
//        assertEquals(paintA, paintC)
//        assertEquals(paintA.hashCode(), paintC.hashCode())
//

        assertNotEquals(Paint(), Paint().apply { isAntiAlias = false })
        assertNotEquals(Paint(), Paint().apply { isDither = true })

        Paint().use { paint ->
            paint.color = 0x12345678
//            assertEquals(false, paint == paint.makeClone(), "cloned paint is not equal")
//            assertEquals(paint, paint.makeClone())
//            assertNotEquals(paint.hashCode(), paint.makeClone().hashCode())
        }

        Paint().use { paint ->
            assertEquals(false, paint.hasNothingToDraw())
            paint.blendMode = BlendMode.DST
            assertEquals(true, paint.hasNothingToDraw())
            paint.blendMode = BlendMode.SRC_OVER
            assertEquals(false, paint.hasNothingToDraw())
            paint.alpha = 0
            assertEquals(true, paint.hasNothingToDraw())
        }
    }

}