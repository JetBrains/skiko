package org.jetbrains.skiko.swing

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.ClipRectangle
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerAnalytics
import org.jetbrains.skiko.SkiaPanel
import org.jetbrains.skiko.SkiaLayerProperties
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.cutoutFromClip
import org.jetbrains.skiko.internal.fastForEach
import java.awt.Component
import javax.accessibility.AccessibleContext

/**
 * Swing component that draws content provided by [renderDelegate] with GPU acceleration using Skia engine.
 *
 * Drawn content can be clipped by providing [ClipRectangle] to [clipComponents].
 *
 * This component can be used for better interop with Swing,
 * so all Swing functionality like z-ordering, double-buffering etc. will be taken into account during rendering.
 *
 * But if no interop with Swing is needed, it is better to use [SkiaPanel] in
 * [SkiaPanel.RenderMode.DirectSurface] mode instead.
 *
 * It is now a thin pull-model layer over the push-only [SkiaPanel] presenter in [SkiaPanel.RenderMode.SwingComposited]
 * mode: [SkiaPanel] owns the offscreen render context, the `SwingPainter` blit and the Swing lifecycle, while this
 * class keeps only the deprecated render-delegate + clipping body. New code should present a `Picture` through
 * `SkiaPanel(RenderMode.SwingComposited)`; see the class-level `@Deprecated`.
 */
@Suppress("unused") // used in Compose Multiplatform
@ExperimentalSkikoApi
@Deprecated(
    message = "SkiaSwingLayer is superseded by SkiaPanel(RenderMode.SwingComposited): record content into a " +
        "Picture and present it with present(picture). The replacement is not a drop-in expression, so no " +
        "automatic ReplaceWith is offered."
)
open class SkiaSwingLayer(
    private val renderDelegate: SkikoRenderDelegate,
    analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    accessibleContextProvider: ((Component) -> AccessibleContext)? = null,
    properties: SkiaLayerProperties = SkiaLayerProperties(),
) : SkiaPanel(
    accessibleContextProvider = accessibleContextProvider,
    renderMode = RenderMode.SwingComposited,
    properties = properties,
    analytics = analytics,
    openPixelGeometry = org.jetbrains.skia.PixelGeometry.UNKNOWN,
) {
    val renderApi: GraphicsApi
        get() = swingRenderApi

    override fun onSwingRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        // Pull-model body: apply the interop clipping, then let the render delegate draw. Background is not
        // cleared here — the SwingComposited surface is transparent and blitted with alpha.
        val scale = graphicsConfiguration.defaultTransform.scaleX.toFloat()
        clipComponents.fastForEach { component ->
            canvas.cutoutFromClip(component, scale)
        }
        renderDelegate.onRender(canvas, width, height, nanoTime)
    }
}
