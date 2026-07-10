package org.jetbrains.skiko

import android.opengl.GLES30
import org.jetbrains.skia.*
import java.nio.IntBuffer

/**
 * The single Android (GLES) render context: it owns the Skia [DirectContext] and the on-screen GPU
 * [Surface]/[BackendRenderTarget] for the current frame. This mirrors the collapsed per-API render-context
 * shape used on AWT (each backend owns its own GPU surface via `acquireSurface`/`present`), but Android has
 * exactly one backend so there is a single implementation.
 *
 * It rasterizes onto the **currently bound** GLES framebuffer, so every method must be called from the
 * `GLSurfaceView` GL thread with a current EGL context (typically from inside `Renderer.onDrawFrame`). The
 * surface is reused while the size is unchanged.
 */
internal class AndroidGLRenderContext {
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var currentWidth = -1
    private var currentHeight = -1

    /**
     * Once closed (either via [close] on teardown or [abandon] on EGL-context loss) this context is dead:
     * [acquireSurface] returns `null` and [present] is a no-op, so a late frame that reaches the GL thread
     * after disposal safely does nothing instead of touching freed native objects.
     */
    private var closed = false

    /** Lazily creates the surface for [width]x[height], recreating it when the size changes. */
    fun acquireSurface(width: Int, height: Int): Surface? {
        if (closed) return null
        val directContext = context ?: makeGLContext().also { context = it }
        if (surface == null || width != currentWidth || height != currentHeight) {
            disposeSurface()
            currentWidth = width
            currentHeight = height
            val boundFramebuffer = IntBuffer.allocate(1)
            GLES30.glGetIntegerv(GLES30.GL_DRAW_FRAMEBUFFER_BINDING, boundFramebuffer)
            renderTarget = makeGLRenderTarget(
                width,
                height,
                0,
                8,
                boundFramebuffer[0],
                FramebufferFormat.GR_GL_RGBA8
            )
            surface = Surface.makeFromBackendRenderTarget(
                directContext,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB
            ) ?: throw RenderException("Cannot create surface")
        }
        return surface
    }

    /** Flushes the recorded frame to GL. The `GLSurfaceView` swaps buffers after `onDrawFrame` returns. */
    fun present() {
        if (closed) return
        surface?.flushAndSubmit()
        context?.flush()
    }

    /**
     * Clean teardown on the GL thread while the EGL context is still valid: the native GL objects are
     * released normally. Must run on the GL thread (e.g. via `GLSurfaceView.queueEvent`) so it never races
     * an in-flight [acquireSurface]/[present] on that thread.
     */
    fun close() {
        if (closed) return
        closed = true
        disposeSurface()
        context?.close()
        context = null
    }

    /**
     * Drops all GPU resources after the backing EGL context was lost/recreated. [DirectContext.abandon]
     * marks the internal texture/buffer IDs invalid so the following handle closes make no backend GL calls
     * against the new (or dead) context. Must run on the GL thread.
     */
    fun abandon() {
        if (closed) return
        closed = true
        context?.abandon()
        disposeSurface()
        context?.close()
        context = null
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
    }
}
