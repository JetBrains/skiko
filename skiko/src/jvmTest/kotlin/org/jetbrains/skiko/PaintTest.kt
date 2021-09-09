package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.jetbrains.skiko.util.loadResourceImage
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class PaintTest {
    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun filterQuality() {
        // macOs has different results
        assumeTrue(hostOs.isWindows || hostOs.isLinux)

        val surface = Surface.makeRasterN32Premul(16, 16)

        surface.canvas.drawImageRect(
            image = loadResourceImage("test.png"),
            src = Rect.makeXYWH(0f, 2f, 2f, 4f),
            dst = Rect.makeXYWH(0f, 4f, 4f, 12f),
            samplingMode = FilterMipmap(FilterMode.NEAREST, MipmapMode.NONE),
            Paint(),
            true
        )
        surface.canvas.drawImageRect(
            image = loadResourceImage("test.png"),
            src = Rect.makeXYWH(0f, 2f, 2f, 4f),
            dst = Rect.makeXYWH(4f, 4f, 4f, 12f),
            samplingMode = FilterMipmap(FilterMode.LINEAR, MipmapMode.NONE),
            Paint(),
            true
        )
        surface.canvas.drawImageRect(
            image = loadResourceImage("test.png"),
            src = Rect.makeXYWH(0f, 2f, 2f, 4f),
            dst = Rect.makeXYWH(8f, 4f, 4f, 12f),
            samplingMode = CubicResampler(1 / 3.0f, 1 / 3.0f),
            Paint(),
            true
        )

        screenshots.assert(surface.makeImageSnapshot())
    }
}