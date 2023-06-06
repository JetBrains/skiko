package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.redrawer.RedrawerProvider
import java.awt.Graphics2D
import java.util.concurrent.CancellationException
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
    private val contentScale: Float
        get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()

    @Volatile
    private var isDisposed = false

    private val redrawerProvider = RedrawerProvider<SwingRedrawer>(properties.renderApi) { renderApi, oldRedrawer ->
        oldRedrawer?.dispose()
        createDefaultSwingRedrawer(this@SkiaSwingLayer, renderApi, analytics, properties)
    }

    private val redrawer: SwingRedrawer?
        get() = redrawerProvider.redrawer

    override val renderApi: GraphicsApi
        get() = redrawerProvider.renderApi

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    private val skiaLayerRenderer = SkiaLayerRenderer(fpsCounter)

    override val clipComponents: MutableList<ClipRectangle> get() = skiaLayerRenderer.clipComponents

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
        skiaLayerRenderer.init()
        redrawerProvider.findNextWorkingRenderApi(recreation)
        isInited = true
    }

    override fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInited && !isDisposed) {
            // we should dispose redrawer first (to cancel `draw` in rendering thread)
            redrawer?.dispose()
            redrawerProvider.dispose()
            skiaLayerRenderer.dispose()
            isDisposed = true
        }
    }

    override fun paint(g: java.awt.Graphics) {
        redrawer?.redraw(g as Graphics2D)
    }

    internal fun update(nanoTime: Long) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        skiaLayerRenderer.update(nanoTime, width, height, contentScale, skikoView)
    }

    internal inline fun inDrawScope(body: () -> Unit) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }
        try {
            body()
        } catch (e: CancellationException) {
            // ignore
        } catch (e: RenderException) {
            if (!isDisposed) {
                Logger.warn(e) { "Exception in draw scope" }
                redrawerProvider.findNextWorkingRenderApi()
                repaint()
            }
        }
    }

    internal fun draw(canvas: Canvas) {
        skiaLayerRenderer.draw(canvas)
    }

    override fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        // TODO: support accessibility
    }
}
