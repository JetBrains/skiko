package org.jetbrains.skiko.swing

import org.jetbrains.skia.Color
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayerAnalytics
import org.jetbrains.skiko.SkiaLayerAnalytics.DeviceAnalytics
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.Version
import org.jetbrains.skiko.autoreleasepool
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.AwtOffscreenSoftwareRenderContext
import org.jetbrains.skiko.swing.SharedTexturesAdapter.Companion.createSharedTexturesAdapter
import java.awt.Graphics2D
import java.util.concurrent.CancellationException
import javax.swing.SwingUtilities

/**
 * Drives a view-less offscreen [SwingRenderContext] for [SkiaSwingLayer]'s Swing-interop pull model, and blits
 * the rendered frame onto a `Graphics2D` with a [SwingPainter].
 *
 * The GPU/raster lifecycle lives in the reusable [SwingRenderContext] (which the public
 * `RenderContext.createOffscreen(...)` factory also uses), while this class keeps the AWT-only concerns the
 * public render context has nothing to do with — [SkiaLayerAnalytics], HiDPI scale/size, the first-frame
 * callbacks, and the Skia-surface → `Graphics2D` blit.
 *
 * Each [redraw] runs synchronously on the EDT: acquire the offscreen surface for the current scaled bounds,
 * clear it, let [renderDelegate] draw, [present][SwingRenderContext.present] it, then paint it onto the supplied
 * `Graphics2D`.
 */
@OptIn(ExperimentalSkikoApi::class)
internal class SwingRenderer(
    private val swingLayerProperties: SwingLayerProperties,
    private val renderDelegate: SkikoRenderDelegate,
    private val renderContext: SwingRenderContext,
    private val painter: SwingPainter,
    analytics: SkiaLayerAnalytics,
) {
    private val graphicsApi: GraphicsApi = renderContext.graphicsApi
    private val rendererAnalytics = analytics.renderer(Version.skiko, hostOs, graphicsApi)
    private val deviceAnalytics: DeviceAnalytics?
    private var isFirstFrameRendered = false
    private var isDisposed = false

    init {
        rendererAnalytics.init()
        rendererAnalytics.deviceChosen()
        deviceAnalytics = analytics.device(Version.skiko, hostOs, graphicsApi, renderContext.deviceName)
        deviceAnalytics?.init()
        deviceAnalytics?.contextInit()
    }

    fun dispose() {
        require(!isDisposed) { "$javaClass is disposed" }
        isDisposed = true
        renderContext.close()
        painter.dispose()
    }

    fun redraw(g: Graphics2D) {
        require(!isDisposed) { "$javaClass is disposed" }
        check(SwingUtilities.isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        requireNotNull(deviceAnalytics) { "deviceAnalytics is null" }

        val scale = swingLayerProperties.scale
        val width = (swingLayerProperties.width * scale).toInt().coerceAtLeast(0)
        val height = (swingLayerProperties.height * scale).toInt().coerceAtLeast(0)
        if (width < 1 || height < 1) {
            return
        }

        val isFirstFrame = !isFirstFrameRendered
        isFirstFrameRendered = true
        if (isFirstFrame) {
            deviceAnalytics?.beforeFirstFrameRender()
        }
        try {
            // Metal churns an offscreen MTLTexture per frame; drain it in an autorelease pool.
            // autoreleasepool is macOS-only native, so only enter it for Metal.
            if (graphicsApi == GraphicsApi.METAL) {
                autoreleasepool { renderFrame(g, width, height) }
            } else {
                renderFrame(g, width, height)
            }
        } catch (_: CancellationException) {
        }
        if (isFirstFrame && !isDisposed) {
            deviceAnalytics?.afterFirstFrameRender()
        }
    }

    private fun renderFrame(g: Graphics2D, width: Int, height: Int) {
        val surface = renderContext.acquireSurface(width, height)
        surface.canvas.clear(Color.TRANSPARENT)
        renderDelegate.onRender(surface.canvas, width, height, System.nanoTime())
        renderContext.present()
        painter.paint(g, surface, renderContext.texturePtr)
    }
}

/**
 * Creates a [SwingRenderer] backed by the offscreen [SwingRenderContext] for [renderApi], paired with the
 * matching [SwingPainter] (zero-copy [AcceleratedSwingPainter] for Metal where the JBR shared-texture path is
 * available, [SoftwareSwingPainter] otherwise). Throws [RenderException] if the backend cannot initialise on
 * this host, so [SkiaSwingLayer]'s fallback chain can move to the next API.
 */
@OptIn(ExperimentalSkikoApi::class)
internal fun createSwingRenderer(
    swingLayerProperties: SwingLayerProperties,
    renderDelegate: SkikoRenderDelegate,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
): SwingRenderer {
    if (renderApi == GraphicsApi.SOFTWARE_COMPAT || renderApi == GraphicsApi.SOFTWARE_FAST) {
        return softwareRenderer(swingLayerProperties, renderDelegate, renderApi, analytics)
    }
    return when (hostOs) {
        OS.MacOS -> {
            val context = MetalSwingRedrawer(
                adapterPriority = swingLayerProperties.adapterPriority,
                gpuResourceCacheLimit = swingLayerProperties.gpuResourceCacheLimit,
            )
            SwingRenderer(swingLayerProperties, renderDelegate, context, metalPainter(swingLayerProperties), analytics)
        }
        OS.Windows -> {
            val context = Direct3DSwingRenderContext(
                adapterPriority = swingLayerProperties.adapterPriority,
                gpuResourceCacheLimit = swingLayerProperties.gpuResourceCacheLimit,
            )
            SwingRenderer(swingLayerProperties, renderDelegate, context, SoftwareSwingPainter(swingLayerProperties), analytics)
        }
        OS.Linux -> {
            val context = LinuxOpenGLSwingRedrawer(
                gpuResourceCacheLimit = swingLayerProperties.gpuResourceCacheLimit,
            )
            SwingRenderer(swingLayerProperties, renderDelegate, context, SoftwareSwingPainter(swingLayerProperties), analytics)
        }
        else -> softwareRenderer(swingLayerProperties, renderDelegate, GraphicsApi.SOFTWARE_FAST, analytics)
    }
}

@OptIn(ExperimentalSkikoApi::class) // AwtOffscreenSoftwareRenderContext is @ExperimentalSkikoApi
private fun softwareRenderer(
    swingLayerProperties: SwingLayerProperties,
    renderDelegate: SkikoRenderDelegate,
    renderApi: GraphicsApi,
    analytics: SkiaLayerAnalytics,
): SwingRenderer {
    val context = AwtOffscreenSoftwareRenderContext(renderApi, 0, 0)
    return SwingRenderer(swingLayerProperties, renderDelegate, context, SoftwareSwingPainter(swingLayerProperties), analytics)
}

private fun metalPainter(swingLayerProperties: SwingLayerProperties): SwingPainter = try {
    AcceleratedSwingPainter(
        sharedTextures = createSharedTexturesAdapter()
    ) { SoftwareSwingPainter(swingLayerProperties) }
} catch (_: RenderException) {
    SoftwareSwingPainter(swingLayerProperties)
}
