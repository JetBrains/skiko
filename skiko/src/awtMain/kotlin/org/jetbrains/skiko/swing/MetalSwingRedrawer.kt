package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

/**
 * Provides a way to draw on Skia canvas rendered off-screen with Metal GPU acceleration and then pass it to [java.awt.Graphics2D].
 * It provides better interoperability with Swing, but it is less efficient than on-screen rendering.
 *
 * For now, it uses drawing to [java.awt.image.BufferedImage] that cause VRAM <-> RAM memory transfer and so increased CPU usage.
 *
 * Content to draw is provided by [SkikoView].
 *
 * For on-screen rendering see [org.jetbrains.skiko.redrawer.MetalRedrawer].
 *
 * @see SwingRedrawerBase
 * @see SwingOffscreenDrawer
 */
internal class MetalSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    private val skikoView: SkikoView,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, analytics, GraphicsApi.METAL) {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter: MetalAdapter = chooseMetalAdapter(swingLayerProperties.adapterPriority).also {
        onDeviceChosen(it.name)
    }
    private val context: DirectContext = makeMetalContext()

    init {
        onContextInit()
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    override fun dispose() {
        adapter.dispose()
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) = autoCloseScope {
        val renderTarget = makeRenderTarget(width, height).autoClose()
        val surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        )?.autoClose() ?: throw RenderException("Cannot create surface")

        val canvas = surface.canvas
        canvas.clear(Color.TRANSPARENT)
        skikoView.onRender(canvas, width, height, nanoTime)
        flush(surface, g)
    }

    private fun flush(surface: Surface, g: Graphics2D) {
        surface.flushAndSubmit(syncCpu = true)

        val width = surface.width
        val height = surface.height

        val storage = Bitmap()
        storage.setImageInfo(ImageInfo.makeN32Premul(width, height))
        storage.allocPixels()
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        surface.readPixels(storage, 0, 0)

        val bytes = storage.readPixels(storage.imageInfo, (width * 4), 0, 0)
        if (bytes != null) {
            swingOffscreenDrawer.draw(g, bytes, width, height)
        }
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Video card: ${adapter.name}\n" +
                "Total VRAM: ${adapter.memorySize / 1024 / 1024} MB\n"
    }

    private fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeMetalRenderTargetOffScreen(adapter.ptr, width, height)
    )

    private fun makeMetalContext(): DirectContext = DirectContext(
        makeMetalContext(adapter.ptr)
    )

    private external fun makeMetalContext(adapter: Long): Long

    private external fun makeMetalRenderTargetOffScreen(adapter: Long, width: Int, height: Int): Long
}