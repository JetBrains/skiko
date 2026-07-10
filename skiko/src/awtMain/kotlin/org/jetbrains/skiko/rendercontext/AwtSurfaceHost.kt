package org.jetbrains.skiko.rendercontext

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.HardwareLayer
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

/**
 * The on-screen AWT host an [AwtRenderContext] renders into and presents to.
 *
 * This is the seam that decouples the per-API AWT render contexts from the concrete
 * [org.jetbrains.skiko.SkiaLayer]. It captures **exactly** what those contexts read from their host — nothing
 * more — so a render context can be driven by any AWT surface owner, not just `SkiaLayer`:
 *
 *  * geometry ([width]/[height]/[contentScale]/[pixelGeometry]),
 *  * flags the backends branch on ([transparency]/[fullscreen]/[renderApi]),
 *  * the native handles a device/swap-chain is created from ([windowHandle]/[contentHandle]),
 *  * the AWT peer the backends lock a drawing surface on / blit to ([backedLayer]),
 *  * visibility for the frame limiter's gating ([isShowing]),
 *  * and the [draw] callback that renders the current frame's content onto a supplied [Canvas].
 *
 * The [draw] callback lets one render context serve either presentation model. `SkiaLayer` drives the
 * pull model — it records its render delegate into a `Picture`, then replays that picture onto the canvas —
 * while `SkiaPanel` drives the push model, replaying a picture the caller has already handed it
 * (`canvas.drawPicture(storedPicture)`). The same render context serves both, unchanged.
 */
internal interface AwtSurfaceHost {
    /** Host width in AWT points (the lightweight component bounds; scaled by [contentScale] for pixels). */
    val width: Int

    /** Host height in AWT points (the lightweight component bounds; scaled by [contentScale] for pixels). */
    val height: Int

    /** Device pixel scale (HiDPI factor) of the monitor the host is on. */
    val contentScale: Float

    /** Pixel geometry (subpixel layout) of the device rendering the host. */
    val pixelGeometry: PixelGeometry

    /** Whether the host wants a transparent background (affects device/surface creation and the software blit). */
    val transparency: Boolean

    /** Whether the host is currently fullscreen (non-macOS transparency is unsupported while fullscreen). */
    val fullscreen: Boolean

    /** The render API the host is configured for; used only in the human-readable `renderInfo` summary. */
    val renderApi: GraphicsApi

    /** Native OS window handle the host is attached to (Metal device / vsyncer creation). */
    val windowHandle: Long

    /** Native OS handle of the host's content peer (Direct3D / WGL / Windows-software device creation). */
    val contentHandle: Long

    /** The heavyweight AWT peer the backends lock a drawing surface on, blit to, and read bounds from. */
    val backedLayer: HardwareLayer

    /** Whether the host is currently showing; gates the Linux GL software frame limiter. */
    val isShowing: Boolean

    /**
     * Whether this host covers its entire window. Window-level platform optimizations are only correct when it
     * does — on macOS, driving the interactive live-resize redraw from the window. `false` when the host is
     * embedded as a Swing component somewhere in the window's hierarchy, which disables those paths.
     */
    val fillsWindow: Boolean

    /** Render the current frame's content onto [canvas] (the render callback described in the class kdoc). */
    fun draw(canvas: Canvas)
}

/**
 * The AWT counterpart of [defaultIsTransparentBackgroundSupported]`(SkiaLayer)`, parameterized on the
 * decoupled [AwtSurfaceHost] instead of the concrete layer. Reads only [AwtSurfaceHost.fullscreen].
 */
internal fun defaultIsTransparentBackgroundSupported(host: AwtSurfaceHost): Boolean {
    if (hostOs == OS.MacOS) {
        // macOS transparency is always supported
        return true
    }
    // for non-macOS in fullscreen transparency is not supported
    return !host.fullscreen
}
