package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.LayerDrawScope
import java.awt.Dimension

/**
 * One per-window, per-API on-screen backend for AWT: it owns the native device/swap-chain lifecycle, the
 * Skia `DirectContext` and the frame's surface, the present/swap, and its own pacing. The frame loop is not
 * here — [OnScreenRedrawer] drives every backend through the hooks below.
 *
 * Every native touch point is guarded by the implementation's own `drawLock`; [dispose] re-checks a disposed
 * flag inside that lock.
 */
internal interface AWTRedrawer {
    /** The graphics API this context renders with (for analytics + [OnScreenRedrawer.renderInfo]). */
    val graphicsApi: GraphicsApi

    /** Adapter/device name for analytics; `null` when unknown. Captured during construction. */
    val deviceName: String?

    /** Human-readable backend/device summary. */
    val renderInfo: String

    fun isTransparentBackgroundSupported(): Boolean

    /**
     * Whether a non-vsync-throttled [OnScreenRedrawer.needRender] should also run [SkiaLayer.update] on a
     * separate coalescing ticker (an input-latency optimisation used only by Metal). Default: `false`, so the
     * loop runs a single combined update+draw ticker.
     */
    val separatesUpdateAndDraw: Boolean get() = false

    /**
     * Pacing that must happen *before* the frame body (software / Linux GL frame limiters). Runs on the EDT,
     * every scheduled frame, before the `isShowing` gate — so an implementation that only wants to pace while
     * visible must check `layer.isShowing` itself. Default: nothing.
     */
    suspend fun paceBeforeFrame() {}

    /**
     * Pacing that must happen *after* the frame body (Metal vsync wait + occlusion back-off, Windows GL
     * off-EDT dwmFlush). Runs on the EDT, every scheduled frame. Default: nothing.
     */
    suspend fun paceAfterFrame() {}

    /**
     * Render exactly one frame into the layer's on-screen surface: acquire/resize the surface for the
     * current [scope] dimensions, draw the layer's recorded picture onto it, then present/swap. Owns the
     * backend's threading (off-EDT hand-off where applicable), drawing-surface lock, autorelease pool and
     * size guard. [immediate] selects the synchronous-redraw variant (its vsync handling differs per
     * backend). Suspends because some backends move the GPU work off the EDT.
     *
     * Throwing [org.jetbrains.skiko.RenderException] here signals the frame failed; the loop's caller
     * ([SkiaLayer.inDrawScope]) will fall back to the next render API.
     */
    suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean)

    /** AWT peer became visible/hidden (Metal pauses its drawable). Default: nothing. */
    fun setVisible(isVisible: Boolean) {}

    /** AWT bounds changed; reposition the on-screen surface if the backend needs it (Metal). Default: nothing. */
    fun syncBounds() {}

    /**
     * Whether the platform is currently the frame source instead of skiko's loop — macOS live resize, where
     * AppKit presents synchronously inside a `CATransaction`. While this returns `true`,
     * [OnScreenRedrawer.needRender] routes each redraw to [onFrameSchedulingIntercepted] instead of scheduling
     * through its dispatcher, so the two don't race. Default `false`: the loop drives every frame.
     */
    fun interceptFrameScheduling(): Boolean = false

    /**
     * Services a redraw request while [interceptFrameScheduling] returns `true`: present through the context's
     * own platform-driven path. [throttledToVsync] mirrors [OnScreenRedrawer.needRender]. Default: nothing.
     */
    fun onFrameSchedulingIntercepted(throttledToVsync: Boolean) {}

    /**
     * Hands the backend the loop's frame entry points, once, during [OnScreenRedrawer] construction. Only a
     * backend that can become the platform's frame source ([interceptFrameScheduling]) needs to keep it.
     * Default: ignored.
     */
    fun attachFrameHost(host: FrameHost) {}

    /** Release all native/GPU resources. Called once by [OnScreenRedrawer.dispose]. */
    fun dispose()
}

/**
 * The frame loop's entry points, as seen by a backend that is temporarily the platform's own frame source
 * (macOS live resize). Implemented by [OnScreenRedrawer].
 */
internal interface FrameHost {
    /** Schedules a regular loop-driven frame, as [OnScreenRedrawer.needRender] would. */
    fun requestFrame(throttledToVsync: Boolean)

    /**
     * Advances the layer to [size] and runs [body] in a draw scope forced to the same size, wrapped in the
     * loop's frame analytics. Used when the platform, not the loop, decides the size a frame must be
     * recorded at, so the content matches what the platform is about to present. Must be called on the EDT.
     */
    fun inForcedSizeFrame(size: Dimension, body: (LayerDrawScope) -> Unit)
}
