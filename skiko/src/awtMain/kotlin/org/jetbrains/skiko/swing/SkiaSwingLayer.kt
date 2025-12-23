package org.jetbrains.skiko.swing

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.*
import org.jetbrains.skiko.redrawer.RedrawerManager
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext
import javax.swing.JPanel
import javax.swing.SwingUtilities.isEventDispatchThread

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
    externalAccessibleFactory: ((Component) -> Accessible)? = null,
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

    val clipComponents: MutableList<ClipRectangle> get() = mutableListOf()

    private val renderDelegateWithClipping = object : SkikoRenderDelegate by renderDelegate {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            val scale = graphicsConfiguration.defaultTransform.scaleX.toFloat()
            // clipping
            for (index in clipComponents.indices) {
                val item = clipComponents[index]
                canvas.clipRectBy(item, scale)
            }
            renderDelegate.onRender(canvas, width, height, nanoTime)
        }
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
        isInitialized = true
    }

    fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInitialized && !isDisposed) {
            // we should dispose redrawer first (to cancel `draw` in rendering thread)
            redrawer?.dispose()
            redrawerManager.dispose()
            isDisposed = true
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

    @Suppress("LeakingThis")
    private val nativeAccessibleFocusHelper = NativeAccessibleFocusHelper(
        component = this,
        externalAccessible = externalAccessibleFactory?.invoke(this)
    )

    override fun getAccessibleContext(): AccessibleContext? {
        return nativeAccessibleFocusHelper.accessibleContext ?: super.getAccessibleContext()
    }

    fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        nativeAccessibleFocusHelper.requestNativeFocusOnAccessible(accessible)
    }
}
