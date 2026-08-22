package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import org.jetbrains.skiko.redrawer.RedrawerManager
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import javax.accessibility.AccessibleContext
import javax.swing.JPanel
import javax.swing.SwingUtilities.isEventDispatchThread
import org.jetbrains.skiko.internal.fastForEach
/**
 * Swing component that draws content provided by [renderDelegate] with GPU acceleration using Skia engine.
 *
 * Drawn content can be clipped by providing [ClipRectangle] to [clipComponents].
 *
 * This component can be used for better interop with Swing,
 * so all Swing functionality like z-ordering, double-buffering etc. will be taken into account during rendering.
 *
 * But if no interop with Swing is needed, it is better to use [SkiaLayer] instead.
 */
@Suppress("unused") // used in Compose Multiplatform
@ExperimentalSkikoApi
open class SkiaSwingLayer(
    renderDelegate: SkikoRenderDelegate,
    analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    private val accessibleContextProvider: ((Component) -> AccessibleContext)? = null,
    private val properties: SkiaLayerProperties = SkiaLayerProperties()
) : JPanel() {
    internal companion object {
        init {
            Library.load()
        }
    }

    private var isInitialized = false

    @Volatile
    private var isDisposed = false

    val clipComponents: MutableList<ClipRectangle> = mutableListOf()

    private val renderDelegateWithClipping = SkikoRenderDelegate { canvas, width, height, nanoTime ->
        val scale = graphicsConfiguration.defaultTransform.scaleX.toFloat()
        // clipping
        clipComponents.fastForEach { component ->
            canvas.cutoutFromClip(component, scale)
        }
        renderDelegate.onRender(canvas, width, height, nanoTime)
    }

    private val swingLayerProperties = object : SwingLayerProperties {
        override val width: Int
            get() = this@SkiaSwingLayer.width
        override val height: Int
            get() = this@SkiaSwingLayer.height
        override val graphicsConfiguration: GraphicsConfiguration
            get() = this@SkiaSwingLayer.graphicsConfiguration
        override val adapterPriority: GpuPriority
            get() = this@SkiaSwingLayer.properties.adapterPriority
        override val gpuResourceCacheLimit: Long
            get() = this@SkiaSwingLayer.properties.gpuResourceCacheLimit
    }

    private val redrawerManager = RedrawerManager<SwingRedrawer>(
        properties.renderApi,
        redrawerFactory = { renderApi, oldRedrawer ->
            oldRedrawer?.dispose()
            createSwingRedrawer(swingLayerProperties, renderDelegateWithClipping, renderApi, analytics)
        }
    )

    private val redrawer: SwingRedrawer?
        get() = redrawerManager.redrawer

    private var repaintPacer: SwingRepaintPacer? = null

    val renderApi: GraphicsApi
        get() = redrawerManager.renderApi

    init {
        isOpaque = false
        layout = null
    }

    override fun removeNotify() {
        Logger.debug { "SkiaSwingLayer.awt#removeNotify $this" }
        dispose()
        super.removeNotify()
    }

    override fun addNotify() {
        Logger.debug { "SkiaSwingLayer.awt#addNotify $this" }
        super.addNotify()
        init(isInitialized)
    }

    private fun init(recreation: Boolean = false) {
        isDisposed = false
        redrawerManager.findNextWorkingRenderApi(recreation)
        repaintPacer?.dispose()
        repaintPacer = if (SkikoProperties.swingFramePacingEnabled) SwingRepaintPacer(this) else null
        isInitialized = true
    }

    fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInitialized && !isDisposed) {
            repaintPacer?.dispose()
            repaintPacer = null
            // we should dispose redrawer first (to cancel `draw` in rendering thread)
            redrawer?.dispose()
            redrawerManager.dispose()
            isDisposed = true
        }
    }

    /**
     * Requests a repaint of the layer content, like [repaint].
     *
     * When frame pacing is enabled ([SkikoProperties.swingFramePacingEnabled]) and the JetBrains
     * Runtime `FramePacing` service can pace the layer's display, the repaint is deferred to the
     * next display refresh tick, and multiple requests coalesce into at most one repaint per tick.
     * Otherwise the request falls back to a plain [repaint].
     *
     * Use this instead of [repaint] for invalidation-driven rendering (e.g. animations), so that a
     * continuously invalidating scene renders at most at the display refresh rate.
     *
     * Must be called on the AWT event dispatch thread.
     */
    fun needRender() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        val repaintPacer = repaintPacer
        if (repaintPacer != null && !isDisposed) {
            repaintPacer.requestRepaint()
        } else {
            repaint()
        }
    }

    override fun paint(g: Graphics) {
        try {
            redrawer?.redraw(g as Graphics2D)
        } catch (e: RenderException) {
            if (!isDisposed) {
                Logger.warn(e) { "Exception in draw scope" }
                redrawerManager.findNextWorkingRenderApi()
                repaint()
            }
        }
    }

    override fun getAccessibleContext(): AccessibleContext? {
        return accessibleContextProvider?.invoke(this) ?: super.getAccessibleContext()
    }
}
