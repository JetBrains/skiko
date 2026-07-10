package org.jetbrains.skiko

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.internal.fastForEach
import org.jetbrains.skiko.rendercontext.AwtRenderContext
import org.jetbrains.skiko.rendercontext.AwtSurfaceHost
import org.jetbrains.skiko.rendercontext.RenderApiFallbackManager
import org.jetbrains.skiko.rendercontext.createRenderContext
import org.jetbrains.skiko.swing.SwingLayerProperties
import org.jetbrains.skiko.swing.SwingRenderer
import org.jetbrains.skiko.swing.createSwingRenderer
import org.jetbrains.skiko.swing.scale
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.Point
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.HierarchyEvent
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import java.awt.event.KeyListener
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelListener
import java.awt.geom.AffineTransform
import java.awt.im.InputMethodRequests
import java.beans.PropertyChangeListener
import java.util.concurrent.CancellationException
import javax.accessibility.AccessibleContext
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.SwingUtilities.isEventDispatchThread
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener
import kotlin.math.floor

/**
 * A push-only Skia presenter for AWT/Swing.
 *
 * Unlike the deprecated pull-model [SkiaLayer] (which owns a [SkikoRenderDelegate] and an internal, coalescing
 * frame loop and asks the delegate to re-render on demand), [SkiaPanel] is driven entirely from the outside:
 * the consumer records a frame into a [Picture] and hands it over with [present]. There is **no**
 * `renderDelegate` and **no** internal frame loop here — frame scheduling belongs to the consumer, who owns a
 * [DisplayFrameTicker]. Damage/expose re-presents the last stored picture with no round trip to the consumer.
 *
 * Two render modes ship, both selected at construction:
 *
 *  * [RenderMode.DirectSurface] — a heavyweight on-screen GPU surface presented with vsync (the on-screen
 *    render contexts driven through the internal [AwtSurfaceHost] seam, exactly the classic [SkiaLayer]
 *    surface path). This owns the JAWT drawing-surface lock scope: [present] drives the per-API context's
 *    acquire→draw→present, and each context takes/releases the drawing-surface lock internally.
 *  * [RenderMode.SwingComposited] — lightweight Swing interop: the frame is rasterised offscreen and blitted
 *    onto the panel's `Graphics2D` in [paint] (the offscreen render contexts + a `SwingPainter`, the classic
 *    [org.jetbrains.skiko.swing.SkiaSwingLayer] path), so Swing z-order/double-buffering are respected.
 *
 * The transparency-aware background ([setBackground]) and the [clipComponents] interop cutouts are applied on
 * the **present** path (`clear(bg)` → cut out clip rectangles → `drawPicture`), never recorded into the
 * picture — so a push caller that hands over a bare picture still gets the configured background and cutouts.
 */
@ExperimentalSkikoApi
open class SkiaPanel internal constructor(
    private val accessibleContextProvider: ((Component) -> AccessibleContext)?,
    val renderMode: RenderMode,
    /** The render configuration ([SkiaLayerProperties]) this panel was created with. */
    open val properties: SkiaLayerProperties,
    internal val analytics: SkiaLayerAnalytics,
    openPixelGeometry: PixelGeometry,
    /**
     * Whether this panel covers its entire window, enabling window-level platform optimizations — on macOS,
     * driving the interactive live-resize redraw from the window. Leave `false` when the panel is embedded as
     * a Swing component somewhere in the window's hierarchy rather than covering the whole window.
     */
    internal val fillsWindow: Boolean = false,
) : JPanel() {

    /** How a [SkiaPanel] puts its content on screen. See the class kdoc. */
    enum class RenderMode {
        /** Heavyweight on-screen GPU surface + vsync present (the classic [SkiaLayer] surface path). */
        DirectSurface,

        /** Lightweight offscreen rasterise + Java2D blit in [paint] (the classic `SkiaSwingLayer` path). */
        SwingComposited,
    }

    constructor(
        renderMode: RenderMode = RenderMode.DirectSurface,
        properties: SkiaLayerProperties = SkiaLayerProperties(),
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
        accessibleContextProvider: ((Component) -> AccessibleContext)? = null,
        fillsWindow: Boolean = false,
    ) : this(accessibleContextProvider, renderMode, properties, analytics, pixelGeometry, fillsWindow)

    internal companion object {
        init {
            Library.load()
        }
    }

    /** Pixel geometry of the device rendering this panel; overridden `actual` in [SkiaLayer]. */
    open val pixelGeometry: PixelGeometry = openPixelGeometry

    /** Device pixel scale (HiDPI factor) of the monitor this panel is on. */
    open val contentScale: Float
        get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()

    // --- Present-side state shared by both modes -------------------------------------------------------------

    // False while the JPanel superclass constructor runs (it calls setBackground via the LAF before our fields
    // are initialised); set true by a trailing init block once the panel is fully constructed.
    private var isFullyConstructed = false

    private val pictureLock = Any()

    @Volatile
    private var storedPicture: PictureHolder? = null

    /** A list of rectangles to cut out from the presented content; no content is drawn inside them. */
    open val clipComponents = mutableListOf<ClipRectangle>()

    private var _transparency: Boolean = false
    private var _background: Color? = null

    /** Whether transparency is enabled (the presented background is left transparent). */
    open var transparency: Boolean
        get() = _transparency
        set(value) = configureBackground(value, _background)

    override fun setBackground(bg: Color?) = configureBackground(_transparency, bg)

    private fun configureBackground(transparency: Boolean, bg: Color?) {
        _transparency = transparency
        _background = bg
        // See SkiaLayer: keep the property readable and preserve null (so getBackground() falls back to the
        // ancestor's). The panel itself doesn't opaque-fill; the present path clears to this background.
        super.setBackground(bg)
        // JPanel's superclass constructor installs LAF colours via setBackground before our fields (renderMode,
        // backedLayer, ...) are initialised. Skip the present machinery until construction has finished.
        if (!isFullyConstructed) return
        backedLayer?.background = if (transparency) Color(0, 0, 0, 0) else bg
        requestPresent()
    }

    /**
     * Store [picture] as the current frame and present it. Damage/expose later re-presents this same stored
     * picture with no round trip to the caller (see [drawStored]). The caller owns frame scheduling — call
     * this once per frame it produces (e.g. from a [DisplayFrameTicker] callback).
     */
    fun present(picture: Picture) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        val scale = contentScale
        val w = (width * scale).toInt().coerceAtLeast(0)
        val h = (height * scale).toInt().coerceAtLeast(0)
        storePicture(picture, w, h)
        requestPresent()
    }

    /**
     * The present-side draw: clear to the configured background, cut out the [clipComponents], then replay the
     * stored picture. This is the [AwtSurfaceHost.draw] body for [RenderMode.DirectSurface] and the render
     * delegate body for [RenderMode.SwingComposited]. Background and cutouts live here, never in the picture,
     * so a bare picture handed to [present] still gets them.
     */
    protected fun drawStored(canvas: Canvas) {
        // Scope the clip/cutout changes to this present so they don't accumulate across frames: the
        // cutouts are re-applied on every present, so the canvas must be saved and restored around them.
        val saveCount = canvas.save()
        try {
            val scale = contentScale
            clipComponents.fastForEach { component ->
                canvas.cutoutFromClip(component, scale)
            }
            val bg = background
            val isTransparentSupported = isTransparentBackgroundSupported()
            canvas.clear(
                if (_transparency && isTransparentSupported) bg.rgb else bg.rgb or 0xFF000000.toInt()
            )
            synchronized(pictureLock) {
                storedPicture?.let { canvas.drawPicture(it.instance) }
            }
        } finally {
            canvas.restoreToCount(saveCount)
        }
    }

    /** Swap in a new stored picture (already sized in device pixels), closing the previous one. */
    protected fun storePicture(picture: Picture, width: Int, height: Int) {
        synchronized(pictureLock) {
            storedPicture?.instance?.close()
            storedPicture = PictureHolder(picture, width, height)
        }
    }

    /** Run [action] with the current stored picture under the picture lock, or return `null` if there is none. */
    internal fun <T : Any> withStoredPicture(action: (PictureHolder) -> T): T? = synchronized(pictureLock) {
        storedPicture?.let(action)
    }

    private fun isTransparentBackgroundSupported(): Boolean =
        directSurfaceRenderer?.isTransparentBackgroundSupported()
            ?: org.jetbrains.skiko.rendercontext.defaultIsTransparentBackgroundSupported(surfaceHost)

    /**
     * Make the current stored picture visible. Base presents synchronously (DirectSurface) or repaints for a
     * Java2D blit (SwingComposited); [SkiaLayer] overrides it to schedule a frame through its pull loop.
     */
    protected open fun requestPresent() {
        when (renderMode) {
            RenderMode.DirectSurface -> if (isInited && !isDisposed && isShowing) presentNow(immediate = false)
            RenderMode.SwingComposited -> repaint()
        }
    }

    // --- Lifecycle -------------------------------------------------------------------------------------------

    private var isInited = false

    @Volatile
    protected var isDisposed = false
        private set

    private var latestReceivedGraphicsContextScaleTransform: AffineTransform? = null
    private var peerBufferSizeFixJob: Job? = null

    init {
        layout = null
        // JPanel defaults to opaque, whose PanelUI fills the whole component. Neither mode wants that: the
        // paint override never calls super.paint (so PanelUI.update is never reached), and SwingComposited
        // blits with alpha. Keep it non-opaque so Swing's repaint bookkeeping doesn't assume a full fill.
        isOpaque = false
    }

    // --- DirectSurface --------------------------------------------------------------------------------------

    /** The heavyweight on-screen peer; `null` in [RenderMode.SwingComposited]. */
    internal val backedLayer: HardwareLayer? = if (renderMode == RenderMode.DirectSurface) {
        object : HardwareLayer(accessibleContextProvider) {
            override fun paint(g: Graphics) {
                checkContentScale()
                onBackedLayerExpose()
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override fun reshape(x: Int, y: Int, width: Int, height: Int) {
                @Suppress("DEPRECATION")
                super.reshape(x, y, width, height)
                onBackedLayerReshape()
            }

            override fun getInputMethodRequests(): InputMethodRequests? = this@SkiaPanel.inputMethodRequests

            override fun requestFocus(cause: FocusEvent.Cause?) {
                if (canReceiveFocus(cause)) super.requestFocus(cause)
            }

            override fun requestFocusInWindow(cause: FocusEvent.Cause?): Boolean =
                canReceiveFocus(cause) && super.requestFocusInWindow(cause)

            private fun canReceiveFocus(cause: FocusEvent.Cause?) =
                cause != FocusEvent.Cause.MOUSE_EVENT || isRequestFocusEnabled
        }
    } else null

    /**
     * The decoupled on-screen host the per-API render contexts render into and present to. Every geometry/flag
     * member forwards to this panel; [AwtSurfaceHost.draw] is the push-model present body [drawStored].
     */
    internal val surfaceHost: AwtSurfaceHost = object : AwtSurfaceHost {
        override val width: Int get() = this@SkiaPanel.width
        override val height: Int get() = this@SkiaPanel.height
        override val contentScale: Float get() = this@SkiaPanel.contentScale
        override val pixelGeometry: PixelGeometry get() = this@SkiaPanel.pixelGeometry
        override val transparency: Boolean get() = this@SkiaPanel.transparency
        override val fullscreen: Boolean get() = this@SkiaPanel.fullscreen
        override val renderApi: GraphicsApi get() = this@SkiaPanel.directSurfaceRenderApi
        override val windowHandle: Long get() = backedLayer?.windowHandle ?: 0L
        override val contentHandle: Long get() = backedLayer?.contentHandle ?: 0L
        override val backedLayer: HardwareLayer get() = this@SkiaPanel.backedLayer!!
        override val isShowing: Boolean get() = this@SkiaPanel.isShowing
        override val fillsWindow: Boolean get() = this@SkiaPanel.fillsWindow
        override fun draw(canvas: Canvas) = drawStored(canvas)
    }

    /**
     * The per-API construction seam: build exactly [renderApi] directly from the decoupled [surfaceHost].
     * The pull-model [SkiaLayer] does not go through this — it builds its frame loop from its own injectable
     * `RenderFactory` instead (see [SkiaLayer]).
     */
    internal fun createRenderContext(renderApi: GraphicsApi): AwtRenderContext =
        createRenderContext(surfaceHost, renderApi, analytics, properties)

    /**
     * The push-only per-API context selector for [RenderMode.DirectSurface]; `null` in [RenderMode.SwingComposited]
     * and unused by the pull-model [SkiaLayer], which drives its own [org.jetbrains.skiko.rendercontext.OnScreenRenderer]
     * loop.
     */
    internal val renderContextFactory: RenderApiFallbackManager<AwtRenderContext>? =
        if (renderMode == RenderMode.DirectSurface) {
            RenderApiFallbackManager(
                defaultRenderApi = properties.renderApi,
                factory = { renderApi, _ -> createRenderContext(renderApi) },
                onRenderApiChanged = { onRenderApiChanged() },
            )
        } else null

    /** The live per-API render context for [RenderMode.DirectSurface], or `null` before init/after dispose. */
    internal open val currentRenderContext: AwtRenderContext?
        get() = renderContextFactory?.current

    /** The direct-surface render API currently selected (used by [surfaceHost] and `renderInfo`). */
    internal open val directSurfaceRenderApi: GraphicsApi
        get() = renderContextFactory?.renderApi ?: properties.renderApi

    private var _fullscreenAdapter: FullscreenAdapter? =
        backedLayer?.let { FullscreenAdapter(it) }

    /** Whether this on-screen panel is fullscreen (always `false` in [RenderMode.SwingComposited]). */
    open var fullscreen: Boolean
        get() = _fullscreenAdapter?.fullscreen ?: false
        set(value) {
            _fullscreenAdapter?.fullscreen = value
        }

    /** The heavyweight backing canvas: non-null in [RenderMode.DirectSurface], `null` otherwise. */
    open val canvas: java.awt.Canvas?
        get() = backedLayer

    /** Underlying platform component (the heavyweight peer for DirectSurface, else this panel). */
    open val component: Any?
        get() = backedLayer ?: this

    // --- SwingComposited -------------------------------------------------------------------------------------

    private val swingLayerProperties = object : SwingLayerProperties {
        override val width: Int get() = this@SkiaPanel.width
        override val height: Int get() = this@SkiaPanel.height
        override val graphicsConfiguration: GraphicsConfiguration get() = this@SkiaPanel.graphicsConfiguration
        override val adapterPriority: GpuPriority get() = properties.adapterPriority
        override val gpuResourceCacheLimit: Long get() = properties.gpuResourceCacheLimit
    }

    private val swingRenderDelegate =
        SkikoRenderDelegate { canvas, width, height, nanoTime -> onSwingRender(canvas, width, height, nanoTime) }

    /**
     * The body drawn each [RenderMode.SwingComposited] frame. Base presents the stored picture (push);
     * the deprecated pull-model [org.jetbrains.skiko.swing.SkiaSwingLayer] overrides it to invoke its
     * render delegate. Runs on the EDT onto the offscreen surface's canvas (already cleared transparent).
     */
    protected open fun onSwingRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        drawStored(canvas)
    }

    /** The SwingComposited render API (analogue of [directSurfaceRenderApi]). */
    internal val swingRenderApi: GraphicsApi
        get() = swingRenderApiManager?.renderApi ?: properties.renderApi

    private val swingRenderApiManager =
        if (renderMode == RenderMode.SwingComposited) {
            org.jetbrains.skiko.rendercontext.RenderApiFallbackManager<SwingRenderer>(
                properties.renderApi,
                factory = { renderApi, oldRenderer ->
                    oldRenderer?.dispose()
                    createSwingRenderer(swingLayerProperties, swingRenderDelegate, renderApi, analytics)
                }
            )
        } else null

    /** The live DirectSurface renderer for `isTransparentBackgroundSupported`; `null` until presented once. */
    private val directSurfaceRenderer: AwtRenderContext?
        get() = currentRenderContext

    // --- AWT integration -------------------------------------------------------------------------------------

    init {
        backedLayer?.let {
            @Suppress("LeakingThis")
            add(it)
            it.addHierarchyListener { e ->
                if (e.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) checkShowing()
            }
            addAncestorListener(object : AncestorListener {
                private var positionInWindow: Point? = null
                private val zeroPoint = Point(0, 0)

                private fun computePositionInWindow(): Point? {
                    val window = SwingUtilities.getWindowAncestor(this@SkiaPanel) ?: return null
                    return SwingUtilities.convertPoint(this@SkiaPanel, zeroPoint, window)
                }

                override fun ancestorAdded(event: AncestorEvent?) {
                    positionInWindow = computePositionInWindow()
                }

                override fun ancestorRemoved(event: AncestorEvent?) {
                    positionInWindow = null
                }

                override fun ancestorMoved(event: AncestorEvent?) {
                    val newPosition = computePositionInWindow()
                    if (positionInWindow != null && positionInWindow != newPosition) revalidate()
                    positionInWindow = newPosition
                }
            })
        }
        addPropertyChangeListener("graphicsContextScaleTransform") {
            latestReceivedGraphicsContextScaleTransform = it.newValue as AffineTransform
            revalidate()
            onContentScaleChanged()
            if (hostOs == OS.Windows && backedLayer != null) {
                peerBufferSizeFixJob?.cancel()
                @OptIn(DelicateCoroutinesApi::class)
                peerBufferSizeFixJob = GlobalScope.launch(MainUIDispatcher) {
                    backedLayer.setLocation(1, 0)
                    backedLayer.setLocation(0, 0)
                }
            }
        }
    }

    final override fun addPropertyChangeListener(propertyName: String?, listener: PropertyChangeListener?) {
        super.addPropertyChangeListener(propertyName, listener)
    }

    // Override to make final, because it's called in the init block.
    final override fun addAncestorListener(listener: AncestorListener?) {
        super.addAncestorListener(listener)
    }

    private var isShowingCached = false

    // The cached isShowing() is a DirectSurface-only heavyweight-flash workaround (kept fresh by checkShowing,
    // driven off the backedLayer's hierarchy listener). SwingComposited has no backedLayer, so it must fall
    // back to the real JComponent.isShowing() — otherwise Swing would treat the panel as never-showing and
    // skip repaint(). The `== DirectSurface` form is null-safe for calls during the JPanel super constructor.
    override fun isShowing(): Boolean =
        if (renderMode == RenderMode.DirectSurface) isShowingCached else super.isShowing()

    private fun checkShowing() {
        val wasShowing = isShowingCached
        val isShowingNow = super.isShowing().also { isShowingCached = it }
        if (wasShowing != isShowingNow) {
            val window = SwingUtilities.getWindowAncestor(this)
            if (window != null && window.isShowing) {
                currentRenderContext?.setVisible(isShowingNow)
            }
        }
        if (isShowingNow) {
            currentRenderContext?.syncBounds()
            repaint()
        }
    }

    override fun addNotify() {
        super.addNotify()
        if (renderMode == RenderMode.DirectSurface) {
            SwingUtilities.getWindowAncestor(this)?.addComponentListener(_fullscreenAdapter)
            checkShowing()
        }
        init(isInited)
    }

    override fun removeNotify() {
        if (renderMode == RenderMode.DirectSurface) {
            SwingUtilities.getWindowAncestor(this)?.removeComponentListener(_fullscreenAdapter)
        }
        dispose()
        super.removeNotify()
    }

    /** Initialise the render machinery; overridable so [SkiaLayer] can build its pull loop on top. */
    protected open fun init(recreation: Boolean) {
        isDisposed = false
        when (renderMode) {
            RenderMode.DirectSurface -> {
                backedLayer!!.init()
                initDirectSurfaceDriver(recreation)
            }
            RenderMode.SwingComposited -> swingRenderApiManager!!.findNextWorkingRenderApi(recreation)
        }
        isInited = true
    }

    /**
     * Build the [RenderMode.DirectSurface] render driver over the freshly initialised heavyweight peer. Base
     * selects a push-only per-API context through [renderContextFactory]; the pull-model [SkiaLayer] overrides this
     * to build its [org.jetbrains.skiko.rendercontext.OnScreenRenderer] frame loop instead (so [renderContextFactory]
     * stays dormant for it).
     */
    protected open fun initDirectSurfaceDriver(recreation: Boolean) {
        renderContextFactory!!.findNextWorkingRenderApi(recreation)
        currentRenderContext?.syncBounds()
    }

    /** Release native/GPU resources; overridable so [SkiaLayer] can tear down its pull loop first. */
    open fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInited && !isDisposed) {
            when (renderMode) {
                RenderMode.DirectSurface -> {
                    disposeRenderContext()
                    renderContextFactory!!.dispose()
                    backedLayer!!.dispose()
                    peerBufferSizeFixJob?.cancel()
                }
                RenderMode.SwingComposited -> {
                    swingRenderApiManager!!.current?.dispose()
                    swingRenderApiManager.dispose()
                }
            }
            synchronized(pictureLock) {
                storedPicture?.instance?.close()
                storedPicture = null
            }
            isDisposed = true
        }
    }

    // --- Open hooks the pull-model SkiaLayer overrides --------------------------------------------------------

    /** Called when the heavyweight peer must expose/repaint. Base re-presents the stored picture. */
    protected open fun onBackedLayerExpose() {
        if (isInited && !isDisposed && isShowing) presentNow(immediate = false)
    }

    /** Called when the heavyweight peer's bounds change. Base resizes the surface and re-presents. */
    protected open fun onBackedLayerReshape() {
        currentRenderContext?.syncBounds()
        if (isInited && !isDisposed && isShowing) presentNow(immediate = false)
    }

    /**
     * Close the current render context. Base closes it directly (push-only path); [SkiaLayer] overrides it
     * to a no-op because its frame loop ([org.jetbrains.skiko.rendercontext.OnScreenRenderer]) owns the
     * context's lifetime and closes it there.
     */
    protected open fun disposeRenderContext() {
        currentRenderContext?.close()
    }

    /** Called on a content-scale (HiDPI) change. Base does nothing extra. */
    protected open fun onContentScaleChanged() {}

    /** Called when the DirectSurface render API changes (fallback). Base does nothing extra. */
    protected open fun onRenderApiChanged() {}

    // --- DirectSurface present path ----------------------------------------------------------------------------

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    private fun createDrawScope(forcedSize: Dimension?) = if (forcedSize != null) {
        LayerDrawScope(
            pixelGeometry = pixelGeometry,
            scaledLayerWidth = forcedSize.width,
            scaledLayerHeight = forcedSize.height
        )
    } else {
        LayerDrawScope(
            pixelGeometry = pixelGeometry,
            layerWidth = width,
            layerHeight = height,
            scale = contentScale
        )
    }

    /**
     * [forcedSize] overrides the scope's pixel size when the platform, rather than this panel's own bounds,
     * decides the size a frame must be recorded at (macOS Metal live resize, where the AWT bounds lag the
     * window's).
     */
    internal inline fun inDrawScope(forcedSize: Dimension? = null, body: LayerDrawScope.() -> Unit) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isDisposed) return
        try {
            fpsCounter?.tick()
            with(createDrawScope(forcedSize)) { body() }
        } catch (_: CancellationException) {
        } catch (e: RenderException) {
            if (!isDisposed) {
                Logger.warn(e) { "Exception in draw scope" }
                recreateRenderContextAfterFailure()
            }
        }
    }

    /**
     * Recover after a runtime render failure. Base (push-only) rebuilds the next working context and
     * re-presents the stored picture; [SkiaLayer] overrides it to rebuild its pull-model frame loop instead.
     */
    protected open fun recreateRenderContextAfterFailure() {
        if (isDisposed) return
        try {
            renderContextFactory?.findNextWorkingRenderApi(recreation = false)
            currentRenderContext?.syncBounds()
            presentNow(immediate = false)
        } catch (e: RenderException) {
            Logger.error(e) { "Cannot recreate render context after a render failure" }
        }
    }

    /**
     * Present the stored picture synchronously through the current per-API context (acquire→draw→present).
     * The context owns the JAWT drawing-surface lock internally; a render failure falls back to the next API.
     */
    private fun presentNow(immediate: Boolean) {
        val ctx = currentRenderContext ?: return
        inDrawScope {
            runBlocking { ctx.renderFrame(this@inDrawScope, immediate) }
        }
    }

    // --- Layout / paint ----------------------------------------------------------------------------------------

    override fun doLayout() {
        backedLayer?.let {
            it.setBounds(
                0, 0,
                adjustSizeToContentScale(contentScale, width),
                adjustSizeToContentScale(contentScale, height)
            )
            it.validate()
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun reshape(x: Int, y: Int, w: Int, h: Int) {
        @Suppress("DEPRECATION")
        super.reshape(x, y, w, h)
        if (renderMode == RenderMode.DirectSurface) {
            if (isShowing && currentRenderContext?.presentsOnLayout == true) {
                presentImmediatelyForResize()
            }
            validate()
        }
    }

    /**
     * The Direct3D live-resize glitch workaround: present synchronously as early as possible. Base presents
     * the stored picture; [SkiaLayer] overrides it to re-record via its render delegate first.
     */
    protected open fun presentImmediatelyForResize() {
        currentRenderContext?.syncBounds()
        presentNow(immediate = true)
    }

    override fun paint(g: Graphics) {
        when (renderMode) {
            RenderMode.DirectSurface -> {
                checkContentScale()
                onBackedLayerExpose()
            }
            RenderMode.SwingComposited -> {
                try {
                    swingRenderApiManager?.current?.redraw(g as Graphics2D)
                } catch (e: RenderException) {
                    if (!isDisposed) {
                        Logger.warn(e) { "Exception in draw scope" }
                        swingRenderApiManager?.findNextWorkingRenderApi()
                        repaint()
                    }
                }
            }
        }
    }

    /** Workaround for JBR-5274 and JBR-5305. Open so [SkiaLayer] can re-expose it opt-in-free (see there). */
    open fun checkContentScale() {
        val current = graphicsConfiguration.defaultTransform
        if (current != latestReceivedGraphicsContextScaleTransform) {
            firePropertyChange(
                "graphicsContextScaleTransform",
                latestReceivedGraphicsContextScaleTransform,
                current
            )
        }
    }

    // --- Event delegation to the heavyweight peer (DirectSurface); Swing default otherwise --------------------

    override fun enableInputMethods(enable: Boolean) {
        backedLayer?.enableInputMethods(enable) ?: super.enableInputMethods(enable)
    }

    override fun getInputMethodListeners(): Array<InputMethodListener> =
        backedLayer?.getInputMethodListeners() ?: super.getInputMethodListeners()

    override fun processInputMethodEvent(e: InputMethodEvent?) {
        if (backedLayer != null) backedLayer.doProcessInputMethodEvent(e) else super.processInputMethodEvent(e)
    }

    override fun addFocusListener(l: FocusListener?) {
        if (backedLayer != null) backedLayer.addFocusListener(l) else super.addFocusListener(l)
    }

    override fun removeFocusListener(l: FocusListener?) {
        if (backedLayer != null) backedLayer.removeFocusListener(l) else super.removeFocusListener(l)
    }

    override fun setFocusable(focusable: Boolean) {
        if (backedLayer != null) backedLayer.isFocusable = focusable else super.setFocusable(focusable)
    }

    override fun isFocusable(): Boolean = backedLayer?.isFocusable ?: super.isFocusable()

    override fun hasFocus(): Boolean = backedLayer?.hasFocus() ?: super.hasFocus()

    override fun isFocusOwner(): Boolean = backedLayer?.isFocusOwner ?: super.isFocusOwner()

    override fun requestFocus() {
        backedLayer?.requestFocus() ?: super.requestFocus()
    }

    override fun requestFocus(cause: FocusEvent.Cause?) {
        backedLayer?.requestFocus(cause) ?: super.requestFocus(cause)
    }

    override fun requestFocusInWindow(): Boolean = backedLayer?.requestFocusInWindow() ?: super.requestFocusInWindow()

    override fun requestFocusInWindow(cause: FocusEvent.Cause?): Boolean =
        backedLayer?.requestFocusInWindow(cause) ?: super.requestFocusInWindow(cause)

    override fun setFocusTraversalKeysEnabled(focusTraversalKeysEnabled: Boolean) {
        if (backedLayer != null) backedLayer.focusTraversalKeysEnabled = focusTraversalKeysEnabled
        else super.setFocusTraversalKeysEnabled(focusTraversalKeysEnabled)
    }

    override fun getFocusTraversalKeysEnabled(): Boolean =
        backedLayer?.focusTraversalKeysEnabled ?: super.getFocusTraversalKeysEnabled()

    override fun addInputMethodListener(l: InputMethodListener) {
        super.addInputMethodListener(l)
        backedLayer?.addInputMethodListener(l)
    }

    override fun addMouseListener(l: MouseListener) {
        backedLayer?.addMouseListener(l) ?: super.addMouseListener(l)
    }

    override fun addMouseMotionListener(l: MouseMotionListener) {
        backedLayer?.addMouseMotionListener(l) ?: super.addMouseMotionListener(l)
    }

    override fun addMouseWheelListener(l: MouseWheelListener) {
        backedLayer?.addMouseWheelListener(l) ?: super.addMouseWheelListener(l)
    }

    override fun addKeyListener(l: KeyListener) {
        backedLayer?.addKeyListener(l) ?: super.addKeyListener(l)
    }

    override fun removeInputMethodListener(l: InputMethodListener) {
        super.removeInputMethodListener(l)
        backedLayer?.removeInputMethodListener(l)
    }

    override fun removeMouseListener(l: MouseListener) {
        backedLayer?.removeMouseListener(l) ?: super.removeMouseListener(l)
    }

    override fun removeMouseMotionListener(l: MouseMotionListener) {
        backedLayer?.removeMouseMotionListener(l) ?: super.removeMouseMotionListener(l)
    }

    override fun removeMouseWheelListener(l: MouseWheelListener) {
        backedLayer?.removeMouseWheelListener(l) ?: super.removeMouseWheelListener(l)
    }

    override fun removeKeyListener(l: KeyListener) {
        backedLayer?.removeKeyListener(l) ?: super.removeKeyListener(l)
    }

    override fun getAccessibleContext(): AccessibleContext =
        accessibleContextProvider?.invoke(this) ?: super.getAccessibleContext()

    init {
        // Runs last: from here on setBackground/present may touch the fully-initialised render machinery.
        isFullyConstructed = true
    }
}

internal fun defaultFPSCounter(
    component: Component
): FPSCounter? = with(SkikoProperties) {
    if (!fpsEnabled) return@with null

    // it is slow on Linux (100ms), so we cache it. Also refreshRate available only after window is visible
    val refreshRate by lazy { component.graphicsConfiguration.device.displayMode.refreshRate }
    FPSCounter(
        periodSeconds = fpsPeriodSeconds,
        showLongFrames = fpsLongFramesShow,
        getLongFrameMillis = { fpsLongFramesMillis ?: (1.5 * 1000 / refreshRate) },
        logOnTick = true
    )
}

// TODO Recheck this method validity in 2 cases - full Window content, and a Panel content
//  issue: https://youtrack.jetbrains.com/issue/CMP-5447/Window-white-line-on-the-bottom-before-resizing
/**
 * Increases the value of width/height by one if necessary, to avoid a 1px white line between the Canvas and
 * the bounding window when the window is resized (Px vs Points rounding mismatch under HiDPI scale).
 */
private fun adjustSizeToContentScale(contentScale: Float, value: Int): Int {
    val scaled = value * contentScale
    val diff = scaled - floor(scaled)
    return if (diff > 0.4f && diff < 0.6f) value + 1 else value
}
