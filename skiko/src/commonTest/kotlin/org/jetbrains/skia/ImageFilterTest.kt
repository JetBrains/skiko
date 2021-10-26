package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertContentDifferent
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageFilterTest {

    private val originalBytes: ByteArray by lazy {
        renderAndReturnBytes(imageFilter = null)
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

    private fun imageFilterTest(imageFilter: () -> ImageFilter) = runTest {
        val modifiedPixels = renderAndReturnBytes(imageFilter = imageFilter())
        assertEquals(originalBytes.size, modifiedPixels.size)

        // we don't check the actual content of the pixels, we only assume they're different when ImageFilter applied
        assertContentDifferent(
            array1 = originalBytes,
            array2 = modifiedPixels,
            message = "pixels with applied ImageFilter should be different"
        )
    }

    @Test
    fun alphaThreshold() = imageFilterTest {
        ImageFilter.makeAlphaThreshold(
            Region().apply {
                setRect(IRect(5, 5, 10, 10))
            },
            innerMin = 0.9f,
            outerMax = 0.2f,
            input = null,
            crop = null
        )
    }

    @Test
    fun arithmetic() = imageFilterTest {
        ImageFilter.makeArithmetic(
            k1 = 0.5f, k2 = 0.5f, k3 = 0.5f, k4 = 0.5f, enforcePMColor = true,
            bg = null, fg = null, crop = null
        )
    }

    @Test
    fun blur() = imageFilterTest {
        ImageFilter.makeBlur(
            1f, 1f, FilterTileMode.CLAMP, crop = IRect(5, 5, 10, 10)
        )
    }
}
