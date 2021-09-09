package org.jetbrains.skiko

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorSpace
import org.junit.Test

class SkiaTest {
    @Test
    fun `color conversion`() {
        val cs = ColorSpace.sRGB
        val color = cs.convert(ColorSpace.sRGBLinear, Color4f(1f, 0f, 0f, 1f))
        assert(color.r != 0f)
    }

    @Test
    fun `color table`() {
        val array = ByteArray(256)
        val table = ColorFilter.makeTableARGB(array, array, array, array)
        assert(table._ptr != 0L)
    }
}