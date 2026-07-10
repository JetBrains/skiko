package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.NativePointer
import org.w3c.dom.HTMLCanvasElement

/**
 * The internal WebGL render context for the browser target.
 *
 * This is the web counterpart of the per-API render contexts on the other platforms (the AWT
 * and native-mac render contexts): a single internal type that owns the
 * WebGL-backed Skia [DirectContext] and the on-screen GPU [Surface], exposing a small surface-lifecycle API:
 *
 *  * [acquireSurface] — returns a [Surface] sized to the caller-owned `<canvas>`, reusing the previous one
 *    while the size is unchanged and recreating it (and its [BackendRenderTarget]) when it changes.
 *  * [present] — flushes the recorded GPU work; WebGL shows the default framebuffer implicitly.
 *  * [close] — releases the surface, render target and [DirectContext].
 *
 * Frame scheduling (e.g. `requestAnimationFrame`) stays the consumer's concern — see [SkiaLayer].
 *
 * Every context-touching entry point ([acquireSurface], [present], [close]) makes [contextPointer] current
 * first via [GL.makeContextCurrent]. In a multi-canvas app several WebGL contexts coexist and the "current"
 * one is process-global, so teardown/present of one canvas must not run against another canvas's context.
 */
internal class WebGLRenderContext(
    private val contextPointer: NativePointer,
) {
    private val context: DirectContext
    private var surface: Surface? = null
    private var renderTarget: BackendRenderTarget? = null
    private var currentWidth: Int = -1
    private var currentHeight: Int = -1
    private var closed = false

    init {
        GL.makeContextCurrent(contextPointer)
        context = DirectContext.makeGL()
    }

    /**
     * Returns a [Surface] of the requested size, backed by the WebGL default framebuffer. The surface is
     * reused across frames while [width]/[height] are unchanged, and recreated when they differ (which is how
     * a resized `<canvas>` takes effect on the next frame).
     */
    fun acquireSurface(width: Int, height: Int): Surface {
        check(!closed) { "WebGLRenderContext is closed" }
        GL.makeContextCurrent(contextPointer)
        if (surface == null || width != currentWidth || height != currentHeight) {
            disposeSurface()
            currentWidth = width
            currentHeight = height
            renderTarget = BackendRenderTarget.makeGL(width, height, 1, 8, 0, 0x8058 /* GR_GL_RGBA8 */)
            surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps()
            ) ?: throw RenderException("Cannot create surface")
        }
        return surface!!
    }

    /**
     * Flushes the current frame's GPU work. WebGL presents the default framebuffer implicitly, so there is no
     * explicit swap here.
     */
    fun present() {
        if (closed) return
        GL.makeContextCurrent(contextPointer)
        surface?.flushAndSubmit()
        context.flush()
    }

    /**
     * Releases the surface, render target and [DirectContext]. Idempotent; after this call [acquireSurface]
     * fails and [present] is a no-op.
     */
    fun close() {
        if (closed) return
        closed = true
        GL.makeContextCurrent(contextPointer)
        disposeSurface()
        context.close()
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
        currentWidth = -1
        currentHeight = -1
    }
}
