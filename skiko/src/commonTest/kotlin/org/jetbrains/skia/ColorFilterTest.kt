package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ColorFilterTest {
    @Test
    fun canCreate() {
        ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)
        ColorFilter.makeLighting(10, 10)
        ColorFilter.makeHighContrast(true, InversionMode.LIGHTNESS, 0.5f)
        ColorFilter.makeOverdraw(intArrayOf(127, 127, 127, 127, 127, 127))

        val colorMatrix = ColorMatrix(FloatArray(20) { 0.5f })
        ColorFilter.makeMatrix(colorMatrix)
        ColorFilter.makeHSLAMatrix(colorMatrix)

        val tableComponent = ByteArray(256) { 127 }
        ColorFilter.makeTable(tableComponent)
        ColorFilter.makeTableARGB(null, tableComponent, null, tableComponent)

        val src = ColorFilter.luma
        val dst = ColorFilter.sRGBToLinearGamma

        ColorFilter.makeComposed(src, dst)
        ColorFilter.makeLerp(dst, src, 0.4f)
    }

    @Test
    fun makeBlendReturnsNullForNoOpCombinations() {
        // Skia collapses these away: the filtered color would come out unchanged.
        assertNull(ColorFilter.makeBlend(Color.RED, BlendMode.DST), "DST is a no-op")
        assertNull(ColorFilter.makeBlend(Color.TRANSPARENT, BlendMode.DST), "DST is a no-op")
        for (mode in listOf(
            BlendMode.SRC_OVER,
            BlendMode.DST_OVER,
            BlendMode.DST_OUT,
            BlendMode.SRC_ATOP,
            BlendMode.XOR,
            BlendMode.DARKEN,
        )) {
            assertNull(
                ColorFilter.makeBlend(Color.TRANSPARENT, mode),
                "a transparent color with $mode is a no-op"
            )
        }
        assertNull(
            ColorFilter.makeBlend(Color.RED, BlendMode.DST_IN),
            "an opaque color with DST_IN is a no-op"
        )
    }

    @Test
    fun makeBlendReturnsFilterForBlendsThatChangeTheColor() {
        assertNotNull(ColorFilter.makeBlend(Color.RED, BlendMode.SRC_ATOP))
        assertNotNull(ColorFilter.makeBlend(Color.RED, BlendMode.SRC_OVER))
        assertNotNull(ColorFilter.makeBlend(Color.TRANSPARENT, BlendMode.SRC))
        assertNotNull(ColorFilter.makeBlend(Color.TRANSPARENT, BlendMode.DST_IN))
    }

    @Test
    fun failsColorMatrixConstruction() {
        assertFailsWith<IllegalArgumentException>("Expected 20 elements, got 21") {
            ColorMatrix(FloatArray(21) { 1.0f })
        }
        assertFailsWith<IllegalArgumentException>("Expected 20 elements, got 19") {
            ColorMatrix(FloatArray(19) { 1.0f })
        }
    }
}