package org.jetbrains.skiko.swing

import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.*
import org.jetbrains.skiko.redrawer.RedrawerManager
import java.awt.Graphics2D
import javax.accessibility.Accessible
import javax.swing.SwingUtilities.isEventDispatchThread

open class SkiaSwingLayer internal constructor(
    private val skikoView: SkikoView,
    private val properties: SkiaLayerProperties,
    private val analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    override val pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
) : SkiaSwingLayerComponent() {
    internal companion object {
        init {
            Library.load()
        }
    }

    private var isInited = false

    @Volatile
    private var isDisposed = false

    override val clipComponents: MutableList<ClipRectangle> get() = mutableListOf()

    private val redrawerManager = RedrawerManager<SwingRedrawer>(properties.renderApi) { renderApi, oldRedrawer ->
        oldRedrawer?.dispose()
        createDefaultSwingRedrawer(
            this@SkiaSwingLayer, skikoView,
            renderApi, analytics, properties, clipComponents
        )
    }

    private val redrawer: SwingRedrawer?
        get() = redrawerManager.redrawer

    override val renderApi: GraphicsApi
        get() = redrawerManager.renderApi

    @Suppress("unused") // used in Compose Multiplatform
    constructor(
        skikoView: SkikoView,
        isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
        renderApi: GraphicsApi = SkikoProperties.renderApi,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN
    ) : this(
        skikoView,
        SkiaLayerProperties(
            isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled,
            renderApi
        ),
        analytics,
        pixelGeometry
    )

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
        init(isInited)
    }

    private fun init(recreation: Boolean = false) {
        isDisposed = false
        redrawerManager.findNextWorkingRenderApi(recreation)
        isInited = true
    }

    override fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInited && !isDisposed) {
            // we should dispose redrawer first (to cancel `draw` in rendering thread)
            redrawer?.dispose()
            redrawerManager.dispose()
            isDisposed = true
        }
    }

    override fun paint(g: java.awt.Graphics) {
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
    override fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        // TODO: support accessibility
    }
}
