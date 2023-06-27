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
    private val swingLayerProperties: SwingLayerProperties,
    skikoView: SkikoView,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, skikoView, analytics, GraphicsApi.METAL) {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter: MetalAdapter = chooseMetalAdapter(swingLayerProperties.adapterPriority).also {
        onDeviceChosen(it.name)
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    override fun dispose() {
        adapter.dispose()
        super.dispose()
    }

    override fun createDirectContext(): DirectContext {
        return makeMetalContext()
    }

    override fun initCanvas(context: DirectContext?): DrawingSurfaceData {
        context ?: error("Context should be initialized")

        val scale = swingLayerProperties.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val width = (swingLayerProperties.width * scale).toInt().coerceAtLeast(0)
        val height = (swingLayerProperties.height * scale).toInt().coerceAtLeast(0)

        if (width == 0 || height == 0) {
            return DrawingSurfaceData(renderTarget = null, surface = null, canvas = null)
        }

        val renderTarget = makeRenderTarget(width, height)

        val surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB,
            SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
        ) ?: throw RenderException("Cannot create surface")

        return DrawingSurfaceData(renderTarget, surface, surface.canvas)
    }

    override fun flush(drawingSurfaceData: DrawingSurfaceData, g: Graphics2D) {
        val surface = drawingSurfaceData.surface ?: error("Surface should be initialized")
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