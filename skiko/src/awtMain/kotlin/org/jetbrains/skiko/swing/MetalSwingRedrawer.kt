package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import org.jetbrains.skiko.redrawer.AWTRedrawer
import java.awt.Graphics2D

@OptIn(ExperimentalSkikoApi::class)
internal fun MetalSwingRedrawer(
    layer: SkiaSwingLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
): MetalSwingRedrawer =
    MetalSwingRedrawerImpl(layer, analytics, properties)

internal interface MetalSwingRedrawer {
    fun dispose()

    fun redraw(graphics: Graphics2D)
}

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
 * @see MetalSwingContextHandler
 * @see FrameDispatcher
 */
@ExperimentalSkikoApi
private class MetalSwingRedrawerImpl(
    private val skiaSwingLayer: SkiaSwingLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
    // TODO: what to do with SkiaLayer???
) : AWTRedrawer(analytics, GraphicsApi.METAL, skiaSwingLayer::update, skiaSwingLayer::inDrawScope),
    MetalSwingRedrawer {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter: MetalAdapter = chooseMetalAdapter(properties.adapterPriority).also {
        onDeviceChosen(it.name)
    }

    private val contextHandler = MetalSwingContextHandler(skiaSwingLayer, adapter).also {
        onContextInit()
    }

    override val renderInfo: String get() = contextHandler.rendererInfo()

    override fun dispose() {
        contextHandler.dispose()
        super.dispose()
    }

    override fun redraw(graphics: Graphics2D) {
        update(System.nanoTime())
        inDrawScope {
            contextHandler.draw(graphics)
        }
    }

    override fun needRedraw() {
        throw IllegalStateException("Shouldn't be called")
    }

    override fun redrawImmediately() {
        throw IllegalStateException("Shouldn't be called")
    }
}