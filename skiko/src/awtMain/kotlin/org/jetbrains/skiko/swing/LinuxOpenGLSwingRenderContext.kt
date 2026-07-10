package org.jetbrains.skiko.swing

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.RenderContext
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkikoProperties
import org.jetbrains.skiko.makeGLContext
import org.jetbrains.skiko.makeGLRenderTarget

/**
 * A genuinely view-less **offscreen** OpenGL [RenderContext] for Linux: it renders into a GLX **pbuffer**
 * (`GLX_PBUFFER_BIT` + `glXCreatePbuffer` on its own `XOpenDisplay` connection — see `swingRedrawer.cc`), with
 * no X window and no AWT peer, and hands back the backing Skia [Surface]. The AWT counterpart of the other
 * platforms' view-less GPU factories.
 *
 * It backs both the public `RenderContext.createOffscreen(w, h, GraphicsApi.OPENGL)` factory and, via
 * [SwingRenderer], [SkiaSwingLayer]'s Swing-interop pull model (which blits the surface onto a `Graphics2D`
 * with a [SoftwareSwingPainter]; there is no zero-copy shared-texture path, so [texturePtr] stays 0).
 *
 * ### Frame lifecycle
 * A frame is opened lazily in [acquireSurface] (make pbuffer, `startRendering`, bind a fresh texture+FBO, make
 * a per-frame GL [DirectContext] and surface) and stays **current** through [present] so the caller can read
 * pixels back off the surface immediately after presenting. The GL context is only reset (`finishRendering` +
 * texture/context teardown) when the next [acquireSurface] opens the following frame, or at [close].
 *
 * Not thread-safe — drive it from a single render thread, mirroring [RenderContext]'s contract.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class LinuxOpenGLSwingRenderContext(
    private val gpuResourceCacheLimit: Long = SkikoProperties.gpuResourceCacheLimit,
) : SwingRenderContext {

    private val offScreenContextPtr: Long = makeOffScreenContext().also {
        if (it == 0L) {
            throw RenderException("Cannot create OpenGL context")
        }
    }

    private var offScreenBufferPtr: Long = 0L

    // Per-frame GPU state, only valid while a frame is open (see class kdoc).
    private var frameOpen = false
    private var glTexturePtr: Long = 0L
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var context: DirectContext? = null

    private var closed = false

    override val graphicsApi: GraphicsApi get() = GraphicsApi.OPENGL
    override val deviceName: String get() = "OpenGL OffScreen" // TODO: properly choose device
    override val directContext: DirectContext? get() = context

    override fun acquireSurface(width: Int, height: Int): Surface {
        check(!closed) { "LinuxOpenGLSwingRenderContext is disposed" }
        require(width > 0 && height > 0) { "Surface size must be positive, was ${width}x$height" }

        // Finish the previous frame (its context was left current for pixel readback) before starting a new one.
        finishOpenFrame()

        offScreenBufferPtr = makeOffScreenBuffer(offScreenContextPtr, offScreenBufferPtr, width, height)
        if (offScreenBufferPtr == 0L) {
            throw RenderException("Cannot create offScreen OpenGL buffer")
        }
        startRendering(offScreenContextPtr, offScreenBufferPtr)
        frameOpen = true

        try {
            // TODO: reuse texture
            glTexturePtr = createAndBindTexture(width, height)
            if (glTexturePtr == 0L) {
                throw RenderException("Cannot create offScreen OpenGL texture")
            }
            val fbId = getFboId(glTexturePtr)
            renderTarget = makeGLRenderTarget(width, height, 0, 8, fbId, FramebufferFormat.GR_GL_RGBA8)
            // TODO: may be it is possible to reuse [makeGLContext]
            val directContext = makeGLContext().also {
                if (gpuResourceCacheLimit >= 0) {
                    it.resourceCacheLimit = gpuResourceCacheLimit
                }
            }
            context = directContext
            surface = Surface.makeFromBackendRenderTarget(
                directContext,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            ) ?: throw RenderException("Cannot create surface")
            return surface!!
        } catch (e: Throwable) {
            // Roll back the half-opened frame so the GL context is not left current on failure.
            finishOpenFrame()
            throw e
        }
    }

    override fun present() {
        if (closed) return
        // Leave the frame open (GL context current) so a caller can read pixels back off the surface.
        surface?.flushAndSubmit(syncCpu = true)
    }

    override fun close() {
        if (closed) return
        closed = true
        finishOpenFrame()
        disposeOffScreenBuffer(offScreenBufferPtr)
        offScreenBufferPtr = 0L
        disposeOffScreenContext(offScreenContextPtr)
    }

    private fun finishOpenFrame() {
        if (!frameOpen) return
        surface?.close()
        renderTarget?.close()
        context?.close()
        surface = null
        renderTarget = null
        context = null
        if (glTexturePtr != 0L) {
            unbindAndDisposeTexture(glTexturePtr)
            glTexturePtr = 0L
        }
        finishRendering(offScreenContextPtr)
        frameOpen = false
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
