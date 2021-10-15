package org.jetbrains.skia

import org.jetbrains.skia.svg.SVGDOM
import org.jetbrains.skia.svg.SVGLengthContext
import org.jetbrains.skia.svg.SVGLengthUnit
import org.jetbrains.skia.svg.SVGTag
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import kotlin.test.Test

class SvgTest {
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
        require(!dom.isClosed)
        dom.setContainerSize(Point(100f, 100f))
        dom.setContainerSize(101f, 101f)
        require(dom.root != null)
        val e = dom.root!!
        require(e.x.unit == SVGLengthUnit.NUMBER)
        require(e.y.unit == SVGLengthUnit.NUMBER)
        if (kotlinBackend == KotlinBackend.JVM) {
            // TODO: disabled for other platforms until all methods implemented in JS/Native.
            require(e.width.unit == SVGLengthUnit.NUMBER)
            require(e.height.unit == SVGLengthUnit.NUMBER)
            require(e.viewBox == null)
            require(e.tag == SVGTag.SVG)
            // e.viewBox = Rect(0f, 1f, 100f, 200f)
            // assert(e.viewBox!!.top == 1f)
            require(e.getIntrinsicSize(SVGLengthContext(100f, 100f)).x == 300f)
            e.viewBox = Rect.makeXYWH(0f, 1f, 2f, 3f)
            require(e.viewBox == Rect.makeXYWH(0f, 1f, 2f, 3f))
        }
    }
}