package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertContentDifferent
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Ignore
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
    fun blend() = imageFilterTest {
        val region = Region().apply {
            setRect(IRect(5, 5, 10, 10))
        }
        ImageFilter.makeBlend(
            blendMode = BlendMode.COLOR,
            bg = ImageFilter.makeBlur(2f, 2f, mode = FilterTileMode.CLAMP),
            fg = ImageFilter.makeAlphaThreshold(r = region, innerMin = 0.5f, outerMax = 0.5f, input = null, crop = null),
            crop = null
        )
    }

    @Test
    fun blur() = imageFilterTest {
        ImageFilter.makeBlur(
            1f, 1f, FilterTileMode.CLAMP, crop = IRect(5, 5, 10, 10)
        )
    }

    @Test
    fun colorFilter() = imageFilterTest {
        ImageFilter.makeColorFilter(
            f = ColorFilter.luma,
            input = null, crop = null
        )
    }

    @Test
    fun compose() = imageFilterTest {
        val inner = ImageFilter.makeBlur(
            1f, 1f, FilterTileMode.CLAMP, crop = IRect(5, 5, 10, 10)
        )
        val outer = ImageFilter.makeColorFilter(
            f = ColorFilter.luma,
            input = null, crop = null
        )
        ImageFilter.makeCompose(
            inner = inner,
            outer = outer
        )
    }

    @Test
    fun displacementMap() = imageFilterTest {
        val colorFilter = ImageFilter.makeColorFilter(
            f = ColorFilter.luma,
            input = null, crop = null
        )
        ImageFilter.makeDisplacementMap(
            x = ColorChannel.R,
            y = ColorChannel.G,
            scale = 2f,
            displacement = null,
            color = colorFilter,
            crop = null
        )
    }

    @Test
    fun dropShadow() = imageFilterTest {
        ImageFilter.makeDropShadow(
            dx = 2f, dy = 2f, sigmaX = 2f, sigmaY = 2f, color = Color.BLACK,
            input = null, crop = null
        )
    }

    @Test
    fun dropShadowOnly() = imageFilterTest {
        ImageFilter.makeDropShadowOnly(
            dx = 2f, dy = 2f, sigmaX = 1f, sigmaY = 2f,
            color = Color.BLACK, input = null, crop = null
        )
    }

    @Test
    fun makeImage() = imageFilterTest {
        val bitmap = Bitmap()
        bitmap.setImageInfo(ImageInfo.makeN32Premul(5, 5))
        bitmap.installPixels(ByteArray(100) { -1 })

        ImageFilter.makeImage(
            Image.makeFromBitmap(bitmap)
        )
    }

    @Test
    fun makeMagnifier() = imageFilterTest {
        ImageFilter.makeMagnifier(
            r = Rect(0f, 0f, 15f, 15f),
            input = null,
            crop = null,
            inset = 2f
        )
    }

    @Test
    fun makeMatrixConvolution() = imageFilterTest {
        ImageFilter.makeMatrixConvolution(
            kernelH = 2,
            kernelW = 2,
            kernel = floatArrayOf(0.5f, 1f, 0.2f, 1.2f),
            gain = 1f,
            bias = 0f,
            offsetX = 1,
            offsetY = 1,
            tileMode = FilterTileMode.CLAMP,
            convolveAlpha = true,
            input = null,
            crop = null
        )
    }

    @Test // TODO: use SamplingMode._packAs2Ints after PR(316) gets merged
    fun makeMatrixTransform() = imageFilterTest {
        ImageFilter.makeMatrixTransform(
            Matrix33.makeTranslate(2f, 2f),
            mode = SamplingMode.LINEAR,
            input = null
        )
    }
}
