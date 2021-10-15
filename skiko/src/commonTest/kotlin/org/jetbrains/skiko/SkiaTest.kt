package org.jetbrains.skiko

import org.jetbrains.skia.Color4f
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.impl.Native
import kotlin.test.Test

class SkiaTest {
    @Test
    fun `color_conversion`() {
        val cs = ColorSpace.sRGB
        val color = cs.convert(ColorSpace.sRGBLinear, Color4f(1f, 0f, 0f, 1f))
        require(color.r != 0f)
    }

    @Test
    fun `color_table`() {
        val array = ByteArray(256)
        val table = ColorFilter.makeTableARGB(array, array, array, array)
        require(table._ptr != Native.NullPointer)
    }
}