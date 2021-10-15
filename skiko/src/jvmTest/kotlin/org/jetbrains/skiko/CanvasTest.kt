package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.SkikoByteBuffer
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.junit.Rule
import org.junit.Test
import java.nio.ByteBuffer

class CanvasTest {
    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun drawVertices() {
        val surface = Surface.makeRasterN32Premul(16, 16)

        val positions = Point.flattenArray(
            arrayOf(
                Point(0f, 0f),
                Point(16f, 0f),
                Point(8f, 8f),
                Point(16f, 8f),
                Point(0f, 16f),
                Point(16f, 16f),
            )
        )!!

        val colors = listOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW
        ).toIntArray()

        val indices = shortArrayOf(0, 1, 2, 3, 4, 5)

        surface.canvas.drawVertices(
            VertexMode.TRIANGLES,
            positions,
            colors,
            positions,
            indices,
            BlendMode.SRC_OVER,
            Paint()
        )

        screenshots.assert(surface.makeImageSnapshot())
    }
}
