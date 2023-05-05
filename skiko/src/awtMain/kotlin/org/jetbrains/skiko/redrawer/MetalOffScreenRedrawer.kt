package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.MetalOffScreenContextHandler

/**
 * Experimental API that provides a way to draw on Skia canvas rendered off-screen with Metal GPU acceleration
 * and then passed to [layer] [java.awt.Graphics2D].
 * It provides better interoperability with Swing, but it is less efficient.
 *
 * For now, it uses drawing to [java.awt.image.BufferedImage] that cause VRAM <-> RAM memory transfer and so increased CPU usage.
 * Because of that frames are limited by [FrameDispatcher].
 *
 * For on-screen rendering see [MetalRedrawer].
 *
 * Content to draw is provided by [SkiaLayer.draw].
 *
 * @see MetalOffScreenContextHandler
 * @see FrameDispatcher
 */
@ExperimentalSkikoApi
internal class MetalOffScreenRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.METAL) {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter: MetalAdapter = chooseMetalAdapter(properties.adapterPriority).also {
        onDeviceChosen(it.name)
    }

    private val contextHandler = MetalOffScreenContextHandler(layer, adapter).also {
        onContextInit()
    }

    override val renderInfo: String get() = contextHandler.rendererInfo()

    private val frameJob = Job()
    private val frameLimiter = layerFrameLimiter(CoroutineScope(frameJob), layer.backedLayer)

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }

        if (layer.isShowing) {
            update(System.nanoTime())
            inDrawScope(contextHandler::draw)
        }
    }

    override fun dispose() {
        frameJob.cancel()
        frameDispatcher.cancel()
        contextHandler.dispose()
        super.dispose()
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        update(System.nanoTime())
        inDrawScope(contextHandler::draw)
    }
}