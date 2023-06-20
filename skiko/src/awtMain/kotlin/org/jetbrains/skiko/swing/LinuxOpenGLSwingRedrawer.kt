package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal class LinuxOpenGLSwingRedrawer(
    private val swingLayerProperties: SwingLayerProperties,
    skikoView: SkikoView,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, skikoView, analytics, GraphicsApi.OPENGL) {
    init {
        onDeviceChosen("OpenGL") // TODO: properly choose device
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    private var holderPtr: Long? = null

    override fun createDirectContext(): DirectContext {
        return makeGLContext()
    }

    override fun initCanvas(context: DirectContext?): DrawingSurfaceData {
        context ?: error("DirectContext should be initialized")
        val scale = swingLayerProperties.scale
        val w = (swingLayerProperties.width * scale).toInt().coerceAtLeast(0)
        val h = (swingLayerProperties.height * scale).toInt().coerceAtLeast(0)

        holderPtr = createAndBindFrameBuffer(w, h)
        val fbId = getFboId(holderPtr!!)
        val renderTarget = makeGLRenderTarget(
            w,
            h,
            0,
            8,
            fbId,
            FramebufferFormat.GR_GL_RGBA8
        )
        val surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
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
        disposeTexture(holderPtr!!)
    }


    private external fun createAndBindFrameBuffer(width: Int, height: Int): Long
    private external fun getFboId(holderPtr: Long): Int
    private external fun disposeTexture(holderPtr: Long)
}