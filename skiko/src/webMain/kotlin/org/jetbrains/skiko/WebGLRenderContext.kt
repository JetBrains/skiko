package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.wasm.createWebGLContext
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsNumber
import kotlin.js.toInt
import kotlin.js.unsafeCast

/**
 * Create a [RenderContext] that rasterizes onto a **caller-owned** [HTMLCanvasElement] (web, WebGL).
 *
 * The non-AWT entry point of the render-context API for the browser: skiko owns no view — the consumer
 * provides its `<canvas>`, skiko binds a WebGL context to it. Each frame:
 * [acquireSurface][RenderContext.acquireSurface], draw onto it, then [present][RenderContext.present]
 * (which flushes; WebGL shows the default framebuffer implicitly). The surface is reused while the size is
 * unchanged. Scheduling (e.g. `requestAnimationFrame`) stays the consumer's concern.
 */
fun RenderContext.Companion.createFromCanvas(canvas: HTMLCanvasElement): RenderContext =
    WebGLRenderContext(createWebGLContext(canvas), canvas.width, canvas.height)

/**
 * The internal WebGL render context for the browser target.
 *
 * This is the web counterpart of the per-API render contexts on the other platforms (the AWT
 * and native-mac render contexts): a single type that owns the
 * WebGL-backed Skia [DirectContext] and the on-screen GPU [Surface]. It implements the public
 * [RenderContext] (so the same type backs both `RenderContext.createFromCanvas(canvas)` and skiko's internal
 * [SkiaLayer] driver):
 *
 *  * [acquireSurface] — returns a [Surface] of the requested size, reusing the previous one while the size is
 *    unchanged and recreating it (and its [BackendRenderTarget]) when it changes.
 *  * [resize] — records a new size and drops the surface, so the next [acquireSurface] rebuilds it.
 *  * [present] — flushes the recorded GPU work; WebGL shows the default framebuffer implicitly.
 *  * [close] — releases the surface, render target and [DirectContext].
 *
 * Frame scheduling (e.g. `requestAnimationFrame`) stays the consumer's concern — see [SkiaLayer].
 *
 * Every context-touching entry point ([acquireSurface], [resize], [present], [close]) makes [contextPointer]
 * current first via [GL.makeContextCurrent]. In a multi-canvas app several WebGL contexts coexist and the
 * "current" one is process-global, so teardown/present of one canvas must not run against another canvas's
 * context.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class WebGLRenderContext(
    private val contextPointer: NativePointer,
    width: Int,
    height: Int,
) : RenderContext {
    private val context: DirectContext
    private var surface: Surface? = null
    private var renderTarget: BackendRenderTarget? = null

    /** Size the current [surface] was allocated at; `-1` while there is none. */
    private var surfaceWidth: Int = -1
    private var surfaceHeight: Int = -1

    /** Size the next frame renders at, set by [resize] and by [acquireSurface]. */
    var width: Int = width
        private set
    var height: Int = height
        private set

    var isDisposed: Boolean = false
        private set

    /**
     * Sample count of the WebGL context backing this render context, as reported by the browser: it depends
     * on the attributes the context was requested with, and drives Skia's antialiasing strategy. Coerced to
     * at least 1, as Skia itself does.
     */
    val requestedSampleCount: Int

    init {
        GL.makeContextCurrent(contextPointer)
        requestedSampleCount = currentSampleCount()
        context = DirectContext.makeGL()
    }

    override val graphicsApi: GraphicsApi get() = GraphicsApi.WEBGL
    override val directContext: DirectContext get() = context

    /**
     * Returns a [Surface] of the requested size, backed by the WebGL default framebuffer. The surface is
     * reused across frames while [width]/[height] are unchanged, and recreated when they differ (which is how
     * a resized `<canvas>` takes effect on the next frame).
     */
    override fun acquireSurface(width: Int, height: Int): Surface {
        check(!isDisposed) { "WebGLRenderContext is disposed" }
        GL.makeContextCurrent(contextPointer)
        this.width = width
        this.height = height
        if (surface == null || width != surfaceWidth || height != surfaceHeight) {
            disposeSurface()
            surfaceWidth = width
            surfaceHeight = height
            renderTarget =
                BackendRenderTarget.makeGL(width, height, requestedSampleCount, 8, 0, 0x8058 /* GR_GL_RGBA8 */)
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
     * Records a new render size and drops the surface, so the next [acquireSurface] rebuilds it.
     *
     * Must be called after the canvas element's `width`/`height` attributes change: that resets the WebGL
     * drawing buffer, while the Skia surface keeps targeting the default framebuffer at the old size.
     * Resizing to the size already in effect does nothing.
     */
    fun resize(width: Int, height: Int) {
        check(!isDisposed) { "WebGLRenderContext is disposed" }
        if (width == this.width && height == this.height) return
        GL.makeContextCurrent(contextPointer)
        this.width = width
        this.height = height
        disposeSurface()
    }

    /**
     * Flushes the current frame's GPU work. WebGL presents the default framebuffer implicitly, so there is no
     * explicit swap here.
     */
    override fun present() {
        if (isDisposed) return
        GL.makeContextCurrent(contextPointer)
        surface?.flushAndSubmit()
        context.flush()
    }

    /**
     * Releases the surface, render target and [DirectContext]. Idempotent; after this call [acquireSurface]
     * fails and [present] is a no-op.
     */
    override fun close() {
        if (isDisposed) return
        isDisposed = true
        GL.makeContextCurrent(contextPointer)
        disposeSurface()
        context.close()
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        renderTarget?.close()
        renderTarget = null
        surfaceWidth = -1
        surfaceHeight = -1
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun currentSampleCount(): Int =
    (currentGLContext(GL)?.getParameter(WebGLRenderingContext.SAMPLES)?.unsafeCast<JsNumber>()?.toInt() ?: 0)
        .coerceAtLeast(1)
