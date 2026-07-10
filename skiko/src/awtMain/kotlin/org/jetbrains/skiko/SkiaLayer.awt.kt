package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skiko.internal.fastForEach
import org.jetbrains.skiko.redrawer.AWTRedrawer
import org.jetbrains.skiko.redrawer.OnScreenRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.RedrawerManager
import java.awt.Component
import java.awt.Dimension
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.swing.JComponent
import javax.swing.SwingUtilities.isEventDispatchThread

/**
 * The classic, self-driven ("pull") Skia layer.
 *
 * It is now a thin pull-model layer over the push-only [SkiaPanel] presenter: [SkiaPanel] owns the on-screen
 * heavyweight surface, the per-API render context, and the present path; [SkiaLayer] adds the deprecated
 * self-driven mode — a [renderDelegate] plus a coalescing frame loop ([OnScreenRedrawer]) that, on each
 * scheduled frame, records the delegate into a [Picture] and hands that stored picture to the very same
 * present path. New code should present a [Picture] through [SkiaPanel] and schedule frames with a
 * [DisplayFrameTicker]; see the class-level `@Deprecated`.
 */
@Deprecated(
    message = "SkiaLayer is superseded by SkiaPanel: record content into a Picture and present it with " +
        "SkiaPanel.present(picture), driving frames with DisplayFrameTicker(window). To render off-screen, " +
        "use RenderContext.createOffscreen(width, height, api). The replacement is not a drop-in " +
        "expression, so no automatic ReplaceWith is offered."
)
@OptIn(ExperimentalSkikoApi::class)
actual open class SkiaLayer internal constructor(
    accessibleContextProvider: ((Component) -> AccessibleContext)? = null,
    // Threaded to SkiaPanel, which declares the public `properties` val; SkiaLayer overrides it below
    // as a stable member so `skiaLayer.properties` resolves without an ExperimentalSkikoApi opt-in.
    properties: SkiaLayerProperties,
    private val renderFactory: RenderFactory = RenderFactory.Default,
    analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
    fillsWindow: Boolean = false,
) : SkiaPanel(
    accessibleContextProvider = accessibleContextProvider,
    renderMode = RenderMode.DirectSurface,
    properties = properties,
    analytics = analytics,
    openPixelGeometry = pixelGeometry,
    fillsWindow = fillsWindow,
), Accessible {

    enum class PropertyKind {
        Renderer,
        ContentScale,
    }

    constructor(
        accessibleContextProvider: ((Component) -> AccessibleContext)? = null,
        isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
        frameBuffering: FrameBuffering = SkikoProperties.frameBuffering,
        renderApi: GraphicsApi = SkikoProperties.renderApi,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
        fillsWindow: Boolean = false,
    ) : this(
        accessibleContextProvider = accessibleContextProvider,
        properties = SkiaLayerProperties(
            isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled,
            frameBuffering,
            renderApi
        ),
        renderFactory = RenderFactory.Default,
        analytics = analytics,
        pixelGeometry = pixelGeometry,
        fillsWindow = fillsWindow
    )

    constructor(
        accessibleContextProvider: ((Component) -> AccessibleContext)? = null,
        properties: SkiaLayerProperties,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
        fillsWindow: Boolean = false,
    ) : this(
        accessibleContextProvider = accessibleContextProvider,
        properties = properties,
        renderFactory = RenderFactory.Default,
        analytics = analytics,
        pixelGeometry = pixelGeometry,
        fillsWindow = fillsWindow
    )

    // SkiaLayer is always DirectSurface, so the backing heavyweight canvas always exists.
    override val canvas: java.awt.Canvas
        get() = backedLayer!!

    // These stable members are redeclared (rather than inherited from the @ExperimentalSkikoApi SkiaPanel) so
    // that using them via a SkiaLayer reference does NOT newly require an ExperimentalSkikoApi opt-in at
    // existing call sites. They forward to / match the SkiaPanel behavior.
    actual override val contentScale: Float
        get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()

    override val properties: SkiaLayerProperties
        get() = super.properties

    actual override val pixelGeometry: PixelGeometry = pixelGeometry

    actual override var fullscreen: Boolean
        get() = super.fullscreen
        set(value) {
            super.fullscreen = value
        }

    actual override val component: Any?
        get() = backedLayer

    override var transparency: Boolean
        get() = super.transparency
        set(value) {
            super.transparency = value
        }

    override val clipComponents: MutableList<ClipRectangle>
        get() = super.clipComponents

    // Redeclared for the same reason as the stable members above: `checkContentScale()` is public on
    // SkiaLayer, so calling it via a SkiaLayer reference must not require an ExperimentalSkikoApi opt-in.
    override fun checkContentScale() = super.checkContentScale()

    /**
     * The single generic pull-model frame loop selector. Builds an [OnScreenRedrawer] through the injectable
     * [renderFactory] (the seam skiko's UI tests override to force a backend), owning the fallback queue.
     */
    private val redrawerManager = RedrawerManager<Redrawer>(
        defaultRenderApi = properties.renderApi,
        redrawerFactory = { renderApi, oldRedrawer ->
            oldRedrawer?.dispose()
            renderFactory.createRedrawer(this, renderApi, analytics, properties).also {
                it.syncBoundsFromPlatformComponent()
            }
        },
        onRenderApiChanged = {
            notifyChange(PropertyKind.Renderer)
        }
    )

    /**
     * The current pull-model frame loop driving the render context, or `null` before init / after [dispose] /
     * after a total fallback failure.
     */
    internal val redrawer: Redrawer? by redrawerManager::redrawer

    // The push-model SkiaPanel present machinery (checkShowing/reshape/transparency) reads the live per-API
    // context and its API from here; for the pull model both come from the loop's wrapped context.
    internal override val currentRedrawer: AWTRedrawer?
        get() = (redrawer as? OnScreenRedrawer)?.renderer

    internal override val directSurfaceRenderApi: GraphicsApi
        get() = renderApi

    actual var renderApi: GraphicsApi by redrawerManager::renderApi

    actual var renderDelegate: SkikoRenderDelegate? = null

    val renderInfo: String
        get() = redrawer?.renderInfo ?: "SkiaLayer isn't initialized yet"

    /**
     * Returns the pointer to an OS specific handle (native resource) of the [SkiaLayer].
     */
    val contentHandle: Long
        get() = backedLayer!!.contentHandle

    /**
     * Returns the pointer to an OS specific window handle (native resource)
     * which the current [SkiaLayer] is attached.
     */
    val windowHandle: Long
        get() = backedLayer!!.windowHandle

    /**
     * Returns the physical DPI value (number of dots per inch) of the current monitor.
     */
    val currentDPI: Int
        get() = backedLayer!!.currentDPI

    actual fun attachTo(container: Any) {
        attachTo(container as JComponent)
    }

    fun attachTo(jComponent: JComponent) {
        jComponent.add(this)
    }

    actual fun detach() {
        dispose()
    }

    private var pictureRecorder: PictureRecorder? = null
    private var isRendering = false

    override fun initDirectSurfaceDriver(recreation: Boolean) {
        // The pull model drives its own loop, so SkiaPanel's push-only redrawerFactory stays dormant: build
        // the frame loop here instead. The manager disposes any previous loop and syncBounds the new one.
        pictureRecorder = PictureRecorder()
        redrawerManager.findNextWorkingRenderApi(recreation)
    }

    override fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        // Dispose the frame loop first (to cancel `draw` on the render thread), drop it so a disposed loop
        // can't stay reachable, then let SkiaPanel tear down the surface and stored picture.
        redrawer?.dispose()
        redrawerManager.dispose()
        pictureRecorder?.close()
        pictureRecorder = null
        super.dispose()
    }

    // The frame loop (OnScreenRedrawer.dispose) already disposed the render context; don't dispose it again.
    override fun disposeRedrawer() {}

    override fun recreateRedrawerAfterFailure() {
        if (isDisposed) return
        try {
            redrawerManager.findNextWorkingRenderApi(recreation = false)
            redrawer?.needRender(throttledToVsync = false)
        } catch (e: RenderException) {
            Logger.error(e) { "Cannot recreate render context after a render failure" }
        }
    }

    override fun presentImmediatelyForResize() {
        // Re-record via the render delegate at the new size first, then present synchronously.
        redrawer?.renderImmediately()
    }

    override fun onBackedLayerExpose() {
        redrawer?.needRender(throttledToVsync = false)
    }

    override fun onBackedLayerReshape() {
        redrawer?.onLayerComponentResized()
    }

    override fun onContentScaleChanged() {
        notifyChange(PropertyKind.ContentScale)
    }

    override fun requestPresent() {
        redrawer?.needRender(throttledToVsync = true)
    }

    private val stateChangeListeners =
        mutableMapOf<PropertyKind, MutableList<(SkiaLayer) -> Unit>>()

    fun onStateChanged(kind: PropertyKind, handler: (SkiaLayer) -> Unit) {
        stateChangeListeners.getOrPut(kind, ::mutableListOf) += handler
    }

    private fun notifyChange(kind: PropertyKind) {
        stateChangeListeners[kind]?.let { handlers ->
            handlers.fastForEach { handler ->
                handler.invoke(this)
            }
        }
    }

    /**
     * Redraw on the next animation frame (on vsync signal if vsync is enabled).
     */
    actual fun needRender(throttledToVsync: Boolean) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }
        redrawer?.needRender(throttledToVsync)
    }

    @Deprecated(
        message = "Use needRender() instead",
        replaceWith = ReplaceWith("needRender()")
    )
    actual fun needRedraw() = needRender()

    /**
     * Updates the layer and redraws synchronously.
     */
    fun renderImmediately() {
        redrawer?.renderImmediately()
    }

    /**
     * Records [renderDelegate] into the stored [Picture] (background and cutouts are NOT recorded — they are
     * applied on the present path by [SkiaPanel]). Called by [OnScreenRedrawer] once per scheduled frame.
     */
    internal fun update(nanoTime: Long, forcedSize: Dimension? = null) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        // Protects against re-entrant calls.
        // This can actually happen if, for example, renderDelegate.onRender shows a modal dialog during a live-resize.
        if (isRendering) return

        checkContentScale()
        FrameWatcher.nextFrame()

        val contentScale = this.contentScale
        // A forced size comes from the platform when it, not AWT, dictates the frame size (macOS Metal live
        // resize); it is already in pixels, so contentScale must not be applied again.
        val pictureWidth = (forcedSize?.width?.toFloat() ?: (backedLayer!!.width * contentScale)).coerceAtLeast(0f)
        val pictureHeight = (forcedSize?.height?.toFloat() ?: (backedLayer!!.height * contentScale)).coerceAtLeast(0f)
        val intWidth = pictureWidth.toInt()
        val intHeight = pictureHeight.toInt()

        val pictureRecorder = pictureRecorder!!
        val canvas = pictureRecorder.beginRecording(0f, 0f, pictureWidth, pictureHeight)

        try {
            isRendering = true
            renderDelegate?.onRender(canvas, intWidth, intHeight, nanoTime)
        } finally {
            isRendering = false
        }

        // we can dispose the layer during onRender (or even dispose it and pack it again)
        if (!isDisposed && !pictureRecorder.isClosed) {
            val picture = pictureRecorder.finishRecordingAsPicture()
            storePicture(picture, intWidth, intHeight)
        }
    }

    internal actual fun draw(canvas: Canvas) {
        check(!isDisposed) { "SkiaLayer is disposed" }
        drawStored(canvas)
    }

    // Captures the current layer as a bitmap.
    fun screenshot(): Bitmap? {
        check(!isDisposed) { "SkiaLayer is disposed" }
        return withStoredPicture { picture ->
            val store = Bitmap()
            val ci = ColorInfo(
                ColorType.BGRA_8888, ColorAlphaType.OPAQUE, ColorSpace.sRGB
            )
            store.setImageInfo(ImageInfo(ci, picture.width, picture.height))
            store.allocN32Pixels(picture.width, picture.height)
            val canvas = Canvas(store)
            canvas.drawPicture(picture.instance)
            store.setImmutable()
            store
        }
    }

    override fun getAccessibleContext(): AccessibleContext {
        if (accessibleContext == null) {
            accessibleContext = AccessibleSkiaLayer()
        }
        return accessibleContext
    }

    @Suppress("RedundantInnerClassModifier")
    protected inner class AccessibleSkiaLayer : AccessibleJComponent() {
        override fun getAccessibleRole(): AccessibleRole {
            return AccessibleRole.PANEL
        }
    }
}

/**
 * Disable showing the window title bar.
 */
@Suppress("DEPRECATION")
fun SkiaLayer.disableTitleBar(customHeaderHeight: Float) {
    backedLayer!!.disableTitleBar(customHeaderHeight)
}

/**
 * Request to show emoji and symbols popup.
 */
fun orderEmojiAndSymbolsPopup() {
    platformOperations.orderEmojiAndSymbolsPopup()
}
