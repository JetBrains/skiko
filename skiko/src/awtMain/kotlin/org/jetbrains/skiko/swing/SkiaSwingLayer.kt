package org.jetbrains.skiko.swing

import org.jetbrains.skiko.*
import org.jetbrains.skiko.renderer.RenderApiFallbackManager
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import javax.accessibility.AccessibleContext
import javax.swing.JPanel
import javax.swing.SwingUtilities
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

        private val rendererInitializationExecutor = Executors.newCachedThreadPool { runnable ->
            Thread(runnable, "SkiaSwingLayer renderer initialization").apply { isDaemon = true }
        }
    }

    private var isInitialized = false

    private val initializationLock = Any()
    private val initializationGeneration = AtomicLong()

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

    private val rendererManager = RenderApiFallbackManager<SwingRenderer>(
        properties.renderApi,
        factory = { renderApi, oldRenderer ->
            oldRenderer?.dispose()
            createSwingRenderer(swingLayerProperties, renderDelegateWithClipping, renderApi, analytics)
        }
    )

    private val renderer: SwingRenderer?
        get() = rendererManager.current

    val renderApi: GraphicsApi
        get() = rendererManager.renderApi

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
        isInitialized = true
        val generation = initializationGeneration.incrementAndGet()

        // Direct3D device creation may block in a graphics driver, so do not run it
        // from addNotify on the Swing event dispatch thread.
        renderer?.dispose()
        rendererManager.dispose()
        rendererInitializationExecutor.execute {
            synchronized(initializationLock) {
                if (generation != initializationGeneration.get()) return@synchronized

                try {
                    rendererManager.findNextWorkingRenderApi(recreation)
                } catch (e: RenderException) {
                    Logger.warn(e) { "Cannot initialize a Swing renderer" }
                }

                // Complete on EDT before another initialization attempt can replace this renderer.
                SwingUtilities.invokeAndWait {
                    if (generation == initializationGeneration.get() && !isDisposed) {
                        repaint()
                    } else {
                        renderer?.dispose()
                        rendererManager.dispose()
                    }
                }
            }
        }
    }

    fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        initializationGeneration.incrementAndGet()
        if (isInitialized && !isDisposed) {
            // we should dispose renderer first (to cancel `draw` in rendering thread)
            renderer?.dispose()
            rendererManager.dispose()
            isDisposed = true
        }
    }

    override fun paint(g: Graphics) {
        try {
            renderer?.redraw(g as Graphics2D)
        } catch (e: RenderException) {
            if (!isDisposed) {
                Logger.warn(e) { "Exception in draw scope" }
                rendererManager.findNextWorkingRenderApi()
                repaint()
            }
        }
    }

    override fun getAccessibleContext(): AccessibleContext? {
        return accessibleContextProvider?.invoke(this) ?: super.getAccessibleContext()
    }
}
