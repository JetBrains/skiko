package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertContentDifferent
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageFilterTest {

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
    fun blur() = runTest {
        val pixelsBytesWithoutFilter = renderAndReturnBytes()
        val pixelsBytesWithBlur = renderAndReturnBytes(
            ImageFilter.makeBlur(1f, 1f, FilterTileMode.CLAMP)
        )

        assertEquals(pixelsBytesWithoutFilter.size, pixelsBytesWithBlur.size)

        // we don't check the actual content of the pixels, we only assume they're different when ImageFilter applied
        assertContentDifferent(
            array1 = pixelsBytesWithoutFilter,
            array2 = pixelsBytesWithBlur,
            message = "pixels with applied ImageFilter should be different"
        )
    }
}
