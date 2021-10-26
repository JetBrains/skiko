package org.jetbrains.skia

import kotlin.test.Test

class ColorFilterTest {
    @Test
    fun canCreate() {
        ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)
        ColorFilter.makeLighting(10, 10)
        ColorFilter.makeHighContrast(true, InversionMode.LIGHTNESS, 0.5f)
        ColorFilter.makeOverdraw(intArrayOf(127, 127, 127, 127, 127, 127))

        val colorMatrix = ColorMatrix(*FloatArray(20) { 0.5f })
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
}