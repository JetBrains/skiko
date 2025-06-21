package org.jetbrains.skiko.tests.org.jetbrains.skia.svg

import org.jetbrains.skia.Data
import org.jetbrains.skia.DynamicMemoryWStream
import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect
import org.jetbrains.skia.WStream
import org.jetbrains.skia.svg.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SVGCanvasTestWithDynamicMemoryWStream {
    @Test
    fun svgCanvasSmoke() {
        svgCanvasSmokeWithCustomWStream { doWithStream ->
            val outputStream = DynamicMemoryWStream()
            doWithStream(outputStream)
            val streamBytes = ByteArray(outputStream.bytesWritten())
            require(outputStream.read(streamBytes, 0, streamBytes.size))
            streamBytes
        }
    }
}

private val svgText = """
            <svg version="1.1"
                 width="300" height="200"
                 xmlns="http://www.w3.org/2000/svg">

              <rect width="100%" height="100%" fill="red" />

              <circle cx="150" cy="100" r="80" fill="green" />

              <text x="150" y="125" font-size="60" text-anchor="middle" fill="white">SVG</text>

            </svg>
        """.trimIndent()

fun svgCanvasSmokeWithCustomWStream(withStream: ((WStream) -> Unit) -> ByteArray) {
    val data = Data.makeFromBytes(svgText.encodeToByteArray())
    val inputDom = SVGDOM(data)

    val streamBytes = withStream { outputStream ->
        val svgCanvas = SVGCanvas.make(Rect.Companion.makeWH(300f, 200f), outputStream)
        inputDom.render(svgCanvas)
        svgCanvas.close()
    }

    val svgCanvasData = Data.makeFromBytes(streamBytes)
    val dom = SVGDOM(svgCanvasData)

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
    e.viewBox = Rect(0f, 1f, 100f, 200f)

    val aspectRatio =
        SVGPreserveAspectRatio(SVGPreserveAspectRatioAlign.XMIN_YMIN, SVGPreserveAspectRatioScale.MEET)
    e.preserveAspectRatio = aspectRatio
    assertEquals(aspectRatio, e.preserveAspectRatio)
    require(e.getIntrinsicSize(SVGLengthContext(100f, 100f)).x == 300f)
    e.viewBox = Rect.makeXYWH(0f, 1f, 2f, 3f)
    require(e.viewBox == Rect.makeXYWH(0f, 1f, 2f, 3f))
}