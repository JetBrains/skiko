package org.jetbrains.skiko

import android.opengl.GLES30
import org.jetbrains.skia.*
import java.nio.IntBuffer

/**
 * Create a [RenderContext] that rasterizes onto the **currently bound** GLES framebuffer (Android).
 *
 * The non-AWT entry point of the render-context API for Android: skiko owns no view — the consumer drives
 * a `GLSurfaceView` (or its own EGL surface) and calls into this from the GL thread with a current EGL
 * context (typically inside `Renderer.onDrawFrame`). Each frame:
 * [acquireSurface][RenderContext.acquireSurface], draw onto it, then [present][RenderContext.present]
 * (which flushes; the `GLSurfaceView` swaps buffers after the frame). The surface is reused while the size
 * is unchanged. Must be driven from the GL thread.
 */
fun RenderContext.Companion.createFromCurrentGLContext(): RenderContext = AndroidGLRenderContext()

/**
 * The single Android (GLES) render context: it owns the Skia [DirectContext] and the on-screen GPU
 * [Surface]/[BackendRenderTarget] for the current frame. It implements the public [RenderContext] (so the
 * same type backs both `RenderContext.createFromCurrentGLContext()` and skiko's internal
 * `SkikoSurfaceView` driver), and adds a
 * couple of Android-lifecycle-specific extras ([abandon], [isClosed]) that the public interface does not
 * carry.
 *
 * It rasterizes onto the **currently bound** GLES framebuffer, so every method must be called from the
 * `GLSurfaceView` GL thread with a current EGL context. The surface is reused while the size is unchanged.
 */
internal class AndroidGLRenderContext : RenderContext {
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var currentWidth = -1
    private var currentHeight = -1

    /**
     * Once closed (either via [close] on teardown or [abandon] on EGL-context loss) this context is dead:
     * [isClosed] flips to `true` so the internal driver can no-op a late frame instead of touching freed
     * native objects, and [acquireSurface] refuses to run.
     */
    private var closed = false

    /**
     * Whether this context has been [close]d or [abandon]ed. Not part of the public [RenderContext] contract
     * — used by skiko's internal Android driver to safely skip a frame that reaches the GL thread after
     * disposal.
     */
    val isClosed: Boolean get() = closed

    override val graphicsApi: GraphicsApi get() = GraphicsApi.OPENGL
    override val directContext: DirectContext? get() = context

    /** Lazily creates the surface for [width]x[height], recreating it when the size changes. */
    override fun acquireSurface(width: Int, height: Int): Surface {
        check(!closed) { "AndroidGLRenderContext is closed" }
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
        return surface!!
    }

    /** Flushes the recorded frame to GL. The `GLSurfaceView` swaps buffers after `onDrawFrame`. */
    override fun present() {
        if (closed) return
        surface?.flushAndSubmit()
        context?.flush()
    }

    /**
     * Clean teardown on the GL thread while the EGL context is still valid: the native GL objects are
     * released normally. Must run on the GL thread (e.g. via `GLSurfaceView.queueEvent`) so it never races
     * an in-flight [acquireSurface]/[present] on that thread.
     */
    override fun close() {
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
