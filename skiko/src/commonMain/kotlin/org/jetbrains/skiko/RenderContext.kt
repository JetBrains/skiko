package org.jetbrains.skiko

import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface

/**
 * A reusable, view-decoupled GPU render context.
 *
 * It abstracts only the two things that are genuinely platform/API specific — **creating** the drawable
 * surface and **presenting** it — and otherwise gets out of the way: [acquireSurface] hands you the real
 * Skia [Surface], and you draw onto it however you like (replay an `SkPicture`, issue draw calls directly,
 * blit, snapshot, read pixels — whatever [Surface] supports). skiko does not wrap, hide, or dictate the
 * content.
 *
 * Typical frame:
 * ```
 * val surface = ctx.acquireSurface(width, height)
 * surface.canvas.drawPicture(picture)   // or any direct drawing
 * ctx.present()
 * ```
 *
 * This is the primitive that a consumer uses to drive **its own** view: own your `CAMetalLayer` /
 * `<canvas>` / GL context, obtain a context from the matching per-platform factory on
 * [RenderContext.Companion], and run the loop above. There is no skiko view involved — frame scheduling (a render loop,
 * `requestAnimationFrame`, a `GLSurfaceView`, …) stays entirely your concern.
 *
 * Not thread-safe — drive it from a single render thread. [close] releases the GPU resources; after it, the
 * context must not be used again.
 */
interface RenderContext : AutoCloseable {
    /** The graphics API this context renders with (e.g. [GraphicsApi.METAL], [GraphicsApi.OPENGL]). */
    val graphicsApi: GraphicsApi

    /**
     * The live Skia Ganesh GPU context backing this render context, or `null`.
     *
     * `null` means there is **no Ganesh [DirectContext]** — either because rendering is on the CPU
     * (software / raster) **or** because the context is driven by a non-Ganesh engine (such as Skia's
     * Graphite backend), whose contexts are not [DirectContext]s. It may also be `null` before the first
     * [acquireSurface] of a lazily-initialised backend. Do **not** read `null` as "software" — treat it only
     * as "no Ganesh `DirectContext` is available here".
     *
     * When non-`null` it is owned by this context: use it, but do not [close][org.jetbrains.skia.RefCnt.close] it.
     */
    val directContext: DirectContext?

    /**
     * The [Surface] to draw the current frame into, sized [width] x [height] device pixels.
     *
     * The surface is **reused** across frames while its backing is stable (e.g. a GL framebuffer or an
     * offscreen texture whose size has not changed); swap-chain APIs (Metal) necessarily return the current
     * drawable's surface, which rotates per frame. The returned [Surface] is owned by the context — draw on
     * it and flush it, but do not [close][Surface.close] it.
     */
    fun acquireSurface(width: Int, height: Int): Surface

    /**
     * Finish the current frame: submit the GPU work and present it where the API presents — swap a Metal
     * drawable, blit a software buffer, or no-op for targets the host swaps (a browser canvas or a
     * `GLSurfaceView`, which swap after the frame).
     */
    fun present()

    /**
     * Anchor for the per-platform factories, which are declared as extensions on it:
     * `RenderContext.createFromCanvas(canvas)` on web, `RenderContext.createFromMetalLayer(layer)` on
     * darwin, `RenderContext.createFromCurrentGLContext()` on Android, and
     * `RenderContext.createOffscreen(width, height, api)` on AWT.
     */
    companion object
}
