package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.svg.SVGDOM
import org.jetbrains.skia.svg.SVGLengthContext
import org.jetbrains.skia.svg.SVGLengthUnit
import org.jetbrains.skia.svg.SVGTag
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
    fun `svg smoke`() {
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
        val e = dom.root!!
        assert(e.x.unit == SVGLengthUnit.NUMBER)
        assert(e.y.unit == SVGLengthUnit.NUMBER)
        assert(e.width.unit == SVGLengthUnit.NUMBER)
        assert(e.height.unit == SVGLengthUnit.NUMBER)
        assert(e.viewBox == null)
        assert(e.tag == SVGTag.SVG)
        // e.viewBox = Rect(0f, 1f, 100f, 200f)
        // assert(e.viewBox!!.top == 1f)
        assert(e.getIntrinsicSize(SVGLengthContext(100f, 100f)).x == 300f)
    }
}