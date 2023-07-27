package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal class LinuxOpenGLSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    private val skikoView: SkikoView,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, analytics, GraphicsApi.OPENGL) {
    init {
        onDeviceChosen("OpenGL") // TODO: properly choose device
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    private var holderPtr: Long? = null

    private val context get() = makeGLContext()

    private val storage = Bitmap()

    private var bytesToDraw = ByteArray(0)

    init {
        onContextInit()
    }

    override fun dispose() {
        bytesToDraw = ByteArray(0)
        storage.close()
        context.close()
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        autoCloseScope {
            holderPtr = createAndBindFrameBuffer(width, height)
            val fbId = getFboId(holderPtr!!)
            val renderTarget = makeGLRenderTarget(
                width,
                height,
                0,
                8,
                fbId,
                FramebufferFormat.GR_GL_RGBA8
            ).autoClose()
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
        finishRendering(holderPtr!!)
        disposeTexture(holderPtr!!)
        holderPtr = null
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


    private external fun createAndBindFrameBuffer(width: Int, height: Int): Long
    private external fun getFboId(holderPtr: Long): Int
    private external fun disposeTexture(holderPtr: Long)
    private external fun finishRendering(holderPtr: Long)
}