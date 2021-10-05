package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.svg.SVGDOM
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

    @Test
    fun `svg dom`() {
        val svgText = """
            <svg version="1.1"
                 width="300" height="200"
                 xmlns="http://www.w3.org/2000/svg">

              <rect width="100%" height="100%" fill="red" />

              <circle cx="150" cy="100" r="80" fill="green" />

              <text x="150" y="125" font-size="60" text-anchor="middle" fill="white">SVG</text>

            </svg>
        """.trimIndent()
        val data = Data.makeFromBytes(svgText.encodeToByteArray())
        val dom = SVGDOM(data)
        assert(!dom.isClosed)
        dom.setContainerSize(Point(100f, 100f))
        dom.setContainerSize(101f, 101f)
        assert(dom.root != null)
    }
}