package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Graphics2D

internal class LinuxOpenGLSwingRedrawer(
    swingLayerProperties: SwingLayerProperties,
    private val renderDelegate: SkikoRenderDelegate,
    analytics: SkiaLayerAnalytics
) : SwingRedrawerBase(swingLayerProperties, analytics, GraphicsApi.OPENGL) {
    init {
        onDeviceChosen("OpenGL OffScreen") // TODO: properly choose device
    }

    private val swingOffscreenDrawer = SwingOffscreenDrawer(swingLayerProperties)

    private val offScreenContextPtr: Long = makeOffScreenContext().also {
        if (it == 0L) {
            throw RenderException("Cannot create OpenGL context")
        }
    }


    private var offScreenBufferPtr: Long = 0L

    private val storage = Bitmap()

    private var bytesToDraw = ByteArray(0)

    init {
        onContextInit()
    }

    override fun dispose() {
        bytesToDraw = ByteArray(0)
        storage.close()
        disposeOffScreenBuffer(offScreenBufferPtr)
        disposeOffScreenContext(offScreenContextPtr)
        super.dispose()
    }

    override fun onRender(g: Graphics2D, width: Int, height: Int, nanoTime: Long) {
        offScreenBufferPtr = makeOffScreenBuffer(offScreenContextPtr, offScreenBufferPtr, width, height)
        if (offScreenBufferPtr == 0L) {
            throw RenderException("Cannot create offScreen OpenGL buffer")
        }
        startRendering(offScreenContextPtr, offScreenBufferPtr)
        try {
            autoCloseScope {
                // TODO: reuse texture
                val texturePtr = createAndBindTexture(width, height)
                if (texturePtr == 0L) {
                    throw RenderException("Cannot create offScreen OpenGL texture")
                }
                val fbId = getFboId(texturePtr)
                val renderTarget = makeGLRenderTarget(
                    width,
                    height,
                    0,
                    8,
                    fbId,
                    FramebufferFormat.GR_GL_RGBA8
                ).autoClose()

                // TODO: may be it is possible to reuse [makeGLContext]
                val directContext = makeGLContext().autoClose()
                val surface = Surface.makeFromBackendRenderTarget(
                    directContext,
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
                unbindAndDisposeTexture(texturePtr)
            }
        } finally {
            finishRendering(offScreenContextPtr)
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

    /**
     * Creates new OpenGL context and opens X11 connection.
     * This context can be used together with buffer ([makeOffScreenBuffer]) for offscreen rendering.
     *
     * Should be manually disposed using [disposeOffScreenContext] when no longer needed.
     */
    private external fun makeOffScreenContext(): Long
    private external fun disposeOffScreenContext(contextPtr: Long): Long

    /**
     * Provides offscreen pixels GPU buffer.
     * If size of [oldBufferPtr] same as [width] and [height], it will be reused
     * or created new one otherwise ([oldBufferPtr] will be disposed in this case automatically).
     *
     * Should be manually disposed using [disposeOffScreenBuffer] when no longer needed.
     *
     * @see [makeOffScreenContext]
     */
    private external fun makeOffScreenBuffer(contextPtr: Long, oldBufferPtr: Long, width: Int, height: Int): Long
    private external fun disposeOffScreenBuffer(bufferPtr: Long)

    /**
     * Sets current OpenGL context to [contextPtr] and [bufferPtr],
     * so OpenGL will render into offscreen texture not on screen.
     *
     * Make sure to call [finishRendering] to reset context and wait for all Open GL commands to apply.
     */
    private external fun startRendering(contextPtr: Long, bufferPtr: Long)
    private external fun finishRendering(contextPtr: Long)

    private external fun createAndBindTexture(width: Int, height: Int): Long
    private external fun getFboId(texturePtr: Long): Int
    private external fun unbindAndDisposeTexture(texturePtr: Long)
}