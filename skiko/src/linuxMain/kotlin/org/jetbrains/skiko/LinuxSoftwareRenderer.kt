package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps

internal class LinuxSoftwareRenderer(
    private val component: LinuxSkiaLayerComponent,
    override val renderApi: GraphicsApi,
) : LinuxLayerRenderer {
    init {
        require(renderApi == GraphicsApi.SOFTWARE_FAST || renderApi == GraphicsApi.SOFTWARE_COMPAT)
        component.beginSoftwareRendering()
    }

    override val description: String =
        when (renderApi) {
            GraphicsApi.SOFTWARE_FAST -> "Skia Raster/Software"
            else -> "Skia Raster/Software (compatibility)"
        }

    override val deviceName: String = "Software"

    private var bitmap: Bitmap? = null
    private var pixmap: Pixmap? = null
    private var surface: Surface? = null
    private var width = 0
    private var height = 0
    private var closed = false

    override fun render(
        width: Int,
        height: Int,
        waitForVsync: Boolean,
        block: (Canvas) -> Unit,
    ) {
        check(!closed) { "Renderer is closed" }
        ensureSurface(width, height)
        val rasterSurface = checkNotNull(surface)
        rasterSurface.canvas.clear(if (component.transparency) Color.TRANSPARENT else Color.BLACK)
        block(rasterSurface.canvas)
        val pixels = checkNotNull(pixmap)
        component.presentSoftwareFrame(pixels.addr, width, height, pixels.rowBytes)
    }

    override fun snapshot(width: Int, height: Int): Bitmap {
        check(!closed) { "Renderer is closed" }
        ensureSurface(width, height)
        val result = Bitmap()
        check(result.allocPixels(imageInfo(width, height))) {
            "Could not allocate a Skia screenshot bitmap"
        }
        if (!checkNotNull(surface).readPixels(result, 0, 0)) {
            result.close()
            error("Could not read pixels from the Skia raster surface")
        }
        return result
    }

    private fun ensureSurface(newWidth: Int, newHeight: Int) {
        require(newWidth > 0 && newHeight > 0)
        if (surface != null && width == newWidth && height == newHeight) return

        closeSurface()
        width = newWidth
        height = newHeight
        bitmap =
            Bitmap().also {
                check(it.allocPixels(imageInfo(width, height))) {
                    "Could not allocate a Skia software framebuffer"
                }
            }
        pixmap = checkNotNull(bitmap?.peekPixels())
        surface =
            Surface.makeRasterDirect(
                pixmap = checkNotNull(pixmap),
                surfaceProps = SurfaceProps(pixelGeometry = component.pixelGeometry),
            )
    }

    override fun close(contextLost: Boolean) {
        if (closed) return
        closed = true
        closeSurface()
        component.endSoftwareRendering()
    }

    private fun closeSurface() {
        surface?.close()
        surface = null
        pixmap?.close()
        pixmap = null
        bitmap?.close()
        bitmap = null
    }

    private fun imageInfo(width: Int, height: Int): ImageInfo =
        ImageInfo.makeN32(width, height, ColorAlphaType.PREMUL, ColorSpace.sRGB)
}
