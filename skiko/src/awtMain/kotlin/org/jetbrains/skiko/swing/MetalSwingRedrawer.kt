package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import java.awt.Graphics2D

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
internal class MetalSwingRedrawer(
    skiaSwingLayer: SkiaSwingLayer,
    skikoView: SkikoView,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties,
    clipComponents: MutableList<ClipRectangle>,
    renderExceptionHandler: (e: RenderException) -> Unit,
) : SwingRedrawerBase(skiaSwingLayer, skikoView, analytics, GraphicsApi.METAL, clipComponents, renderExceptionHandler) {
    companion object {
        init {
            Library.load()
        }
    }

    private val adapter: MetalAdapter = chooseMetalAdapter(properties.adapterPriority).also {
        onDeviceChosen(it.name)
    }

    private val contextHandler = MetalSwingContextHandler(skiaSwingLayer, adapter, this::draw).also {
        onContextInit()
    }

    override fun dispose() {
        contextHandler.dispose()
        super.dispose()
    }

    override fun redraw(g: Graphics2D) {
        update(System.nanoTime())
        inDrawScope {
            contextHandler.draw(g)
        }
    }
}