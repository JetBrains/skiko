package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.svg.SVGDOM
import org.jetbrains.skia.svg.SVGLengthContext
import org.jetbrains.skia.svg.SVGLengthUnit
import org.jetbrains.skia.svg.SVGTag
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

    // @Test
    // TODO: disabled until all methods implemented in JS/Native.
    fun `svg_smoke`() {
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
        require(!dom.isClosed)
        dom.setContainerSize(Point(100f, 100f))
        dom.setContainerSize(101f, 101f)
        require(dom.root != null)
        val e = dom.root!!
        require(e.x.unit == SVGLengthUnit.NUMBER)
        require(e.y.unit == SVGLengthUnit.NUMBER)
        require(e.width.unit == SVGLengthUnit.NUMBER)
        require(e.height.unit == SVGLengthUnit.NUMBER)
        require(e.viewBox == null)
        require(e.tag == SVGTag.SVG)
        // e.viewBox = Rect(0f, 1f, 100f, 200f)
        // assert(e.viewBox!!.top == 1f)
        require(e.getIntrinsicSize(SVGLengthContext(100f, 100f)).x == 300f)
    }
}