package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.MetalOffScreenContextHandler
import java.awt.Graphics2D
import javax.swing.JComponent

@OptIn(ExperimentalSkikoApi::class)
internal fun MetalOffScreenRedrawer(
    layer: SkiaSwingLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
): MetalOffScreenRedrawer =
    MetalOffScreenRedrawerImpl(layer, analytics, properties)

internal interface MetalOffScreenRedrawer {
    val clipComponents: MutableList<ClipRectangle>

    var skikoView: SkikoView?

    val renderInfo: String

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
 * @see MetalOffScreenContextHandler
 * @see FrameDispatcher
 */
@ExperimentalSkikoApi
private class MetalOffScreenRedrawerImpl(
    private val skiaSwingLayer: SkiaSwingLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
    // TODO: what to do with SkiaLayer???
) : AWTRedrawer(analytics, GraphicsApi.METAL, skiaSwingLayer::update, skiaSwingLayer::inDrawScope),
    MetalOffScreenRedrawer {
    companion object {
        init {
            Library.load()
        }
    }

    override val clipComponents: MutableList<ClipRectangle>
        get() = skiaSwingLayer.clipComponents

    override var skikoView: SkikoView?
        get() = skiaSwingLayer.skikoView
        set(value) {
            skiaSwingLayer.skikoView = value
        }

    private val adapter: MetalAdapter = chooseMetalAdapter(properties.adapterPriority).also {
        onDeviceChosen(it.name)
    }

    private val contextHandler = MetalOffScreenContextHandler(skiaSwingLayer, adapter).also {
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