package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.MetalAdapter
import org.jetbrains.skiko.RenderException

internal class MetalSwingContextHandler(
    private val skiaSwingLayer: SkiaSwingLayer,
    private val adapter: MetalAdapter,
    drawContent: Canvas.() -> Unit
) : SwingContextHandler(drawContent) {
    private val swingOffscreenDrawer = SwingOffscreenDrawer(skiaSwingLayer)

    override val renderApi: GraphicsApi
        get() = skiaSwingLayer.renderApi

    override fun isTransparentBackground(): Boolean = true

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = makeMetalContext()
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    Logger.info { "Renderer info:\n ${rendererInfo()}" }
                }
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia Metal context!" }
            return false
        }
        return true
    }

    override fun initCanvas() {
        disposeCanvas()
        val g = graphics

        if (g == null) {
            renderTarget = null
            surface = null
            canvas = null
            return
        }

        val scale = skiaSwingLayer.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val width = (skiaSwingLayer.width * scale).toInt().coerceAtLeast(0)
        val height = (skiaSwingLayer.height * scale).toInt().coerceAtLeast(0)

        if (width > 0 && height > 0) {
            renderTarget = makeRenderTarget(width, height)

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = skiaSwingLayer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface!!.canvas
        } else {
            renderTarget = null
            surface = null
            canvas = null
        }
    }

    override fun flush() {
        super.flush()
        // TODO: later may be syncCPU is not needed
        surface!!.flushAndSubmit(syncCpu = true)

        val width = surface!!.width
        val height = surface!!.height

        val storage = Bitmap()
        storage.setImageInfo(ImageInfo.makeN32Premul(width, height))
        storage.allocPixels()
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        surface!!.readPixels(storage, 0, 0)

        val bytes = storage.readPixels(storage.imageInfo, (width * 4), 0, 0)
        val g = graphics
        if (bytes != null && g != null) {
            try {
                swingOffscreenDrawer.draw(g, bytes, width, height)
            } finally {
                g.dispose()
            }
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