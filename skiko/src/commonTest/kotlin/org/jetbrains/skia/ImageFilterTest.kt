package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertContentDifferent
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageFilterTest {

    private val originalBytes: ByteArray by lazy {
         renderAndReturnBytes()
    }

    private fun renderAndReturnBytes(imageFilter: ImageFilter? = null): ByteArray {
        return Surface.makeRasterN32Premul(20, 20).use {
            val paint = Paint().apply {
                setStroke(true)
                strokeWidth = 2f
            }

            val region = Region().apply {
                op(IRect(3, 3, 18, 18), Region.Op.UNION)
            }

            paint.imageFilter = imageFilter
            it.canvas.drawRegion(region, paint)

            val image = it.makeImageSnapshot()
            Bitmap.makeFromImage(image).readPixels()!!
        }
    }

    @Test
    fun alphaThreshold() = runTest {
        val pixelsBytesWithAlphaThreshold = renderAndReturnBytes(
            ImageFilter.makeAlphaThreshold(
                Region().apply {
                    setRect(IRect(5, 5, 10, 10))
                },
                innerMin = 0.9f,
                outerMax = 0.2f,
                input = null,
                crop = null
            )
        )

        assertEquals(originalBytes.size, pixelsBytesWithAlphaThreshold.size)

        // we don't check the actual content of the pixels, we only assume they're different when ImageFilter applied
        assertContentDifferent(
            array1 = originalBytes,
            array2 = pixelsBytesWithAlphaThreshold,
            message = "pixels with applied ImageFilter should be different"
        )
    }

    @Test
    fun blur() = runTest {
        val pixelsBytesWithBlur = renderAndReturnBytes(
            ImageFilter.makeBlur(
                1f, 1f, FilterTileMode.CLAMP, crop = IRect(5, 5, 10, 10)
            )
        )

        assertEquals(originalBytes.size, pixelsBytesWithBlur.size)

        // we don't check the actual content of the pixels, we only assume they're different when ImageFilter applied
        assertContentDifferent(
            array1 = originalBytes,
            array2 = pixelsBytesWithBlur,
            message = "pixels with applied ImageFilter should be different"
        )
    }
}
