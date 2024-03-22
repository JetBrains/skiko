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
    private val renderDelegate: SkikoRenderDelegate,
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

    private var texturePtr: Long = 0

    private val storage = Bitmap()

    private var bytesToDraw = ByteArray(0)

    init {
        onContextInit()
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    override fun dispose() {
        bytesToDraw = ByteArray(0)
        storage.close()
        disposeMetalTexture(texturePtr)
        context.close()
        adapter.dispose()
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        autoreleasepool {
            autoCloseScope {
                texturePtr = makeMetalTexture(adapter.ptr, texturePtr, width, height)
                val renderTarget = makeRenderTarget().autoClose()
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
                renderDelegate.onRender(canvas, width, height, nanoTime)
                flush(surface, g)
            }
        }
    }

    private fun flush(surface: Surface, g: Graphics2D) {
        surface.flushAndSubmit(syncCpu = true)

        val width = surface.width
        val height = surface.height

        val dstRowBytes = width * 4
        if (storage.width != width || storage.height != height) {
            storage.allocPixelsFlags(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL), false)
            bytesToDraw = ByteArray(storage.getReadPixelsArraySize(dstRowBytes = dstRowBytes))
        }
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        surface.readPixels(storage, 0, 0)

        val successfulRead = storage.readPixels(bytesToDraw, dstRowBytes = dstRowBytes)
        if (successfulRead) {
            swingOffscreenDrawer.draw(g, bytesToDraw, width, height)
        }
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Video card: ${adapter.name}\n" +
                "Total VRAM: ${adapter.memorySize / 1024 / 1024} MB\n"
    }

    private fun makeRenderTarget() = BackendRenderTarget(
        makeMetalRenderTargetOffScreen(texturePtr)
    )

    private fun makeMetalContext(): DirectContext = DirectContext(
        makeMetalContext(adapter.ptr)
    )

    private external fun makeMetalContext(adapter: Long): Long

    private external fun makeMetalRenderTargetOffScreen(texture: Long): Long

    /**
     * Provides Metal texture taking given [oldTexture] into account
     * since it can be reused if width and height are not changed,
     * or the new one will be created.
     */
    private external fun makeMetalTexture(adapter: Long, oldTexture: Long, width: Int, height: Int): Long
    private external fun disposeMetalTexture(texture: Long): Long
}