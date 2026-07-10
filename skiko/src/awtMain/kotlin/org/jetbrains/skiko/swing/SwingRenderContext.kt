package org.jetbrains.skiko.swing

import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.RenderContext

/**
 * The view-less **offscreen** [RenderContext]s used by the Swing-interop path ([SkiaSwingLayer] via
 * [SwingRenderer]) and by the public `RenderContext.createOffscreen(...)` factory.
 *
 * It is a plain [RenderContext] (`acquireSurface` / `present` / `directContext` / `graphicsApi` / `close`) plus
 * two small internal extras the Swing blit path needs:
 *
 *  * [deviceName] — the adapter/device name for [org.jetbrains.skiko.SkiaLayerAnalytics], captured at
 *    construction (the on-screen contexts expose the same via `AwtRenderContext.deviceName`);
 *  * [texturePtr] — the current offscreen GPU texture handle a zero-copy [AcceleratedSwingPainter] samples;
 *    `0` for the raster/software and non-shared-texture backends, whose painters read the surface's pixels
 *    instead.
 */
@OptIn(ExperimentalSkikoApi::class)
internal interface SwingRenderContext : RenderContext {
    /** Adapter/device name for analytics; `null` when unknown. */
    val deviceName: String?

    /** Current offscreen GPU texture handle for a zero-copy blit; `0` when there is none to share. */
    val texturePtr: Long get() = 0L
}
