package org.jetbrains.skiko

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import org.jetbrains.skiko.swing.SwingRenderContext

/**
 * A genuinely view-less, CPU-backed [RenderContext] for AWT: it rasterizes into a
 * caller-owned raster [Surface] with **no** AWT component, native window, or JAWT drawing surface involved.
 *
 * This is what makes `RenderContext.createOffscreen(...)` a real view-less public entry point on AWT
 * (matching darwin/web/android): [acquireSurface] hands back a plain [Surface.makeRaster] surface the caller
 * draws onto, and [present] just flushes it — there is nothing to blit or swap because there is no on-screen
 * peer. Read the result back with [Surface.makeImageSnapshot]/`readPixels` like any raster surface.
 *
 * Not thread-safe — drive it from a single thread, mirroring [RenderContext]'s contract.
 */
internal class AwtOffscreenSoftwareRenderContext(
    override val graphicsApi: GraphicsApi,
    initialWidth: Int,
    initialHeight: Int,
) : SwingRenderContext {

    private var surface: Surface? = null
    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var closed = false

    init {
        if (initialWidth > 0 && initialHeight > 0) {
            allocateSurface(initialWidth, initialHeight)
        }
    }

    override val deviceName: String get() = "Software"

    // Software rasterizes on the CPU: there is no Ganesh DirectContext.
    override val directContext: DirectContext? get() = null

    override fun acquireSurface(width: Int, height: Int): Surface {
        check(!closed) { "RenderContext is closed" }
        require(width > 0 && height > 0) { "Surface size must be positive, was ${width}x$height" }
        if (surface == null || width != surfaceWidth || height != surfaceHeight) {
            allocateSurface(width, height)
        }
        return surface!!
    }

    override fun present() {
        if (closed) return
        // No on-screen peer to blit/swap to; just submit the recorded work so the raster surface is up to date.
        surface?.flushAndSubmit()
    }

    override fun close() {
        if (closed) return
        closed = true
        surface?.close()
        surface = null
    }

    private fun allocateSurface(width: Int, height: Int) {
        surface?.close()
        surface = Surface.makeRaster(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL))
        surfaceWidth = width
        surfaceHeight = height
    }
}
