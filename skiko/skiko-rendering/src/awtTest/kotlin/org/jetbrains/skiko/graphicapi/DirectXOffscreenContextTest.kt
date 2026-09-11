package org.jetbrains.skiko.graphicapi

import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.autoCloseScope
import org.jetbrains.skiko.hostOs
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSkikoApi::class)
class DirectXOffscreenContextTest {
    @Test
    fun readPixels() = autoCloseScope {
        assumeTrue(hostOs.isWindows)

        val context = try {
            DirectXOffscreenContext().autoClose()
        } catch (e: RenderException) {
            println("Cannot create DirectX12 context, skipping test. Stacktrace:")
            e.printStackTrace()
            return@autoCloseScope
        }

        val texture = context.Texture(desiredWidth = 200, desiredHeight = 100).autoClose()

        var textureBytes = ByteArray(texture.actualWidth * texture.actualHeight * 4)

        val surface = Surface.makeFromBackendRenderTarget(
            context.directContext,
            texture.backendRenderTarget,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        )?.autoClose() ?: throw RenderException("Cannot create surface")

        fun drawFrame(color: Int) {
            val canvas = surface.canvas
            canvas.drawRect(Rect(0f, 0f, texture.actualWidth.toFloat(), texture.actualHeight.toFloat()), Paint().apply {
                this.color = color
            })

            surface.flushAndSubmit(syncCpu = false)
            texture.waitForCompletion()
            texture.readPixels(textureBytes)
        }

        drawFrame(Color.RED)
        assertEquals(
            listOf(0, 0, -1, -1), // BGRA format, -1 color is the same as 255 color
            textureBytes.take(4).map { it.toInt() }
        )

        drawFrame(Color.BLUE)
        assertEquals(
            listOf(-1, 0, 0, -1),
            textureBytes.take(4).map { it.toInt() }
        )
    }
}
