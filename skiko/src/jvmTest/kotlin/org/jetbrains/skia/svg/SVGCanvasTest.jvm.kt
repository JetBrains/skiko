package org.jetbrains.skiko.tests.org.jetbrains.skia.svg

import org.jetbrains.skia.OutputWStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test

class SVGCanvasTestWithOutputWStream {
    @Test
    fun svgCanvasSmoke() {
        svgCanvasSmokeWithCustomWStream { doWithStream ->
            ByteArrayOutputStream().use { baos ->
                doWithStream(OutputWStream(baos))
                baos.toByteArray()
            }
        }
    }
}
