package org.jetbrains.skiko

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.RedrawerManager
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Point
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.im.InputMethodRequests
import java.beans.PropertyChangeListener
import java.util.concurrent.CancellationException
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.SwingUtilities.isEventDispatchThread
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener
import kotlin.math.floor

actual open class SkiaLayer internal constructor(
    externalAccessibleFactory: ((Component) -> Accessible)? = null,
    val properties: SkiaLayerProperties,
    private val renderFactory: RenderFactory = RenderFactory.Default,
    private val analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    actual val pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
) : JComponent(), Accessible {

    internal companion object {
        init {
            Library.load()
        }
    }

    enum class PropertyKind {
        Renderer,
        ContentScale,
    }

    internal val backedLayer: HardwareLayer

    constructor(
        externalAccessibleFactory: ((Component) -> Accessible)? = null,
        isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
        frameBuffering: FrameBuffering = SkikoProperties.frameBuffering,
        renderApi: GraphicsApi = SkikoProperties.renderApi,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
    ) : this(
        externalAccessibleFactory,
        SkiaLayerProperties(
            isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled,
            frameBuffering,
            renderApi
        ),
        RenderFactory.Default,
        analytics,
        pixelGeometry
    )

    constructor(
        externalAccessibleFactory: ((Component) -> Accessible)? = null,
        properties: SkiaLayerProperties,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
    ) : this(
        externalAccessibleFactory,
        properties,
        RenderFactory.Default,
        analytics,
        pixelGeometry
    )

    val canvas: java.awt.Canvas
        get() = backedLayer

    private var peerBufferSizeFixJob: Job? = null
    private var latestReceivedGraphicsContextScaleTransform: AffineTransform? = null

    init {
        layout = null
        backedLayer = object : HardwareLayer(externalAccessibleFactory) {
            override fun paint(g: Graphics) {
                Logger.debug { "Paint called on HardwareLayer $this" }
                checkContentScale()

                // 1. JPanel.paint is not always called (in rare cases).
                //    For example if we call 'jframe.isResizable = false` on Ubuntu
                //
                // 2. HardwareLayer.paint is also not always called.
                //    For example, on macOs when we resize window or change DPI
                //
                // 3. to avoid double paint in one single frame, use needRender instead of renderImmediately
                redrawer?.needRender(throttledToVsync = false)
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override fun reshape(x: Int, y: Int, width: Int, height: Int) {
                Logger.debug { "reshape(x=$x, y=$y, w=$width, h=$height) called on $this" }
                @Suppress("DEPRECATION")
                super.reshape(x, y, width, height)

                redrawer?.syncBounds()
                redrawer?.needRender(throttledToVsync = false)
            }

            override fun getInputMethodRequests(): InputMethodRequests? {
                return this@SkiaLayer.inputMethodRequests
            }

            // check isRequestFocusEnabled manually, because it isn't implemented for Canvas.
            // But it should be implemented, how it is implemented for JComponent.
            // See Component.setRequestFocusEnabled description.
            override fun requestFocus(cause: FocusEvent.Cause?) {
                if (canReceiveFocus(cause)) {
                    super.requestFocus(cause)
                }
            }

            override fun requestFocusInWindow(cause: FocusEvent.Cause?): Boolean {
                return canReceiveFocus(cause) && super.requestFocusInWindow(cause)
            }

            private fun canReceiveFocus(cause: FocusEvent.Cause?) = cause != FocusEvent.Cause.MOUSE_EVENT ||
                    isRequestFocusEnabled
        }
        @Suppress("LeakingThis")
        add(backedLayer)

        addAncestorListener(object : AncestorListener {

            private var positionInWindow: Point? = null

            private val zeroPoint = Point(0, 0)

            private fun computePositionInWindow(): Point? {
                val window = SwingUtilities.getWindowAncestor(this@SkiaLayer)
                return if (window == null) {
                    null
                } else {
                    SwingUtilities.convertPoint(this@SkiaLayer, zeroPoint, window)
                }
            }

            override fun ancestorAdded(event: AncestorEvent?) {
                positionInWindow = computePositionInWindow()
            }

            override fun ancestorRemoved(event: AncestorEvent?) {
                positionInWindow = null
            }

            override fun ancestorMoved(event: AncestorEvent?) {
                val newPosition = computePositionInWindow()
                if ((positionInWindow != null) && (positionInWindow != newPosition)) {
                    revalidate()
                }
                positionInWindow = newPosition
            }
        })

        backedLayer.addHierarchyListener {
            if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                checkShowing()
            }
        }

        addPropertyChangeListener("graphicsContextScaleTransform") {
            Logger.debug { "graphicsContextScaleTransform changed for $this" }
            latestReceivedGraphicsContextScaleTransform = it.newValue as AffineTransform
            revalidate()
            notifyChange(PropertyKind.ContentScale)

            // Workaround for JBR-5259
            if (hostOs == OS.Windows) {
                peerBufferSizeFixJob?.cancel()
                @OptIn(DelicateCoroutinesApi::class)
                peerBufferSizeFixJob = GlobalScope.launch(MainUIDispatcher) {
                    backedLayer.setLocation(1, 0)
                    backedLayer.setLocation(0, 0)
                }
            }
        }
    }

    private var _transparency: Boolean = false
    actual var transparency: Boolean
        get() = _transparency
        set(value) {
            configureBackground(value, _background)
        }

    actual var backgroundColor: Int
        get() = background.rgb  // Will return an ancestor's non-null background after setBackground(null).
        set(value) {
            configureBackground(_transparency, Color(value, true))
        }

    // This is needed because after setBackground(null), getBackground() will not return null, but an ancestor's
    // non-null background. But we need to preserve the null value when modifying `transparency`.
    private var _background: Color? = null

    override fun setBackground(bg: Color?) {
        configureBackground(_transparency, bg)
    }

    private fun configureBackground(transparency: Boolean, bg: Color?) {
        _transparency = transparency
        _background = bg

        // Note that SkiaLayer itself doesn't draw its background; only backedLayer does, as it's heavyweight.
        // We set the property just so it can be read back correctly, and also for the case when bg==null, as that
        // indicates the parent's background should be used (getBackground() calls parent.getBackground() if own
        // background is null).
        super.setBackground(bg)

        // To enable transparency, the backedLayer's background must be transparent (also the window background).
        backedLayer.background = if (transparency) Color(0, 0, 0, 0) else bg

        needRender()
    }

    // Override to make final, because it's called it in the init block
    final override fun addAncestorListener(listener: AncestorListener?) {
        super.addAncestorListener(listener)
    }

    // Override to make final, because it's called it in the init block
    final override fun addPropertyChangeListener(propertyName: String?, listener: PropertyChangeListener?) {
        super.addPropertyChangeListener(propertyName, listener)
    }

    private var fullscreenAdapter = FullscreenAdapter(backedLayer)

    override fun removeNotify() {
        Logger.debug { "SkiaLayer.awt#removeNotify $this" }
        val window = SwingUtilities.getWindowAncestor(this)
        window.removeComponentListener(fullscreenAdapter)
        dispose()
        super.removeNotify()
    }

    override fun addNotify() {
        Logger.debug { "SkiaLayer.awt#addNotify $this" }
        super.addNotify()
        val window = SwingUtilities.getWindowAncestor(this)
        window.addComponentListener(fullscreenAdapter)
        checkShowing()
        init(isInited)
    }

    actual fun detach() {
        dispose()
    }

    private var isInited = false
    private var isRendering = false

    private fun checkShowing() {
        val wasShowing = isShowingCached
        val isShowingNow = super.isShowing().also {
            isShowingCached = it
        }
        if (wasShowing != isShowingNow) {
            // We don't want to call redrawer.setVisible(false) when the window becomes hidden, because that hides the
            // layer immediately and stops it from being painted (at the system level). But the window itself is still
            // actually visible for a few frames, and it draws its own background, causing a "flash".
            if (SwingUtilities.getWindowAncestor(this).isShowing) {
                redrawer?.setVisible(isShowingNow)
            }
        }
        if (isShowingNow) {
            redrawer?.syncBounds()
            repaint()
        }
    }

    private var isShowingCached = false

    override fun isShowing(): Boolean {
        return isShowingCached
    }

    actual val contentScale: Float
        get() = graphicsConfiguration.defaultTransform.scaleX.toFloat()

    /**
     * Returns the pointer to an OS specific handle (native resource) of the [SkiaLayer].
     */
    val contentHandle: Long
        get() = backedLayer.contentHandle

    /**
     * Returns the pointer to an OS specific window handle (native resource)
     * which the current [SkiaLayer] is attached.
     */
    val windowHandle: Long
        get() = backedLayer.windowHandle

    /**
     * Returns the physical DPI value (number of dots per inch)
     * of the current monitor.
     */
    val currentDPI: Int
        get() = backedLayer.currentDPI

    actual var fullscreen: Boolean
        get() = fullscreenAdapter.fullscreen
        set(value) {
            fullscreenAdapter.fullscreen = value
        }

    actual val component: Any?
        get() = backedLayer

    actual var renderDelegate: SkikoRenderDelegate? = null

    actual fun attachTo(container: Any) {
        attachTo(container as JComponent)
    }

    fun attachTo(jComponent: JComponent) {
        jComponent.add(this)
    }

    val clipComponents = mutableListOf<ClipRectangle>()

    @Volatile
    private var isDisposed = false

    private val redrawerManager = RedrawerManager<Redrawer>(
        defaultRenderApi = properties.renderApi,
        redrawerFactory = { renderApi, oldRedrawer ->
            oldRedrawer?.dispose()
            renderFactory.createRedrawer(this, renderApi, analytics, properties).also {
                it.syncBounds()
            }
        },
        onRenderApiChanged = {
            notifyChange(PropertyKind.Renderer)
        }
    )

    internal val redrawer: Redrawer? by redrawerManager::redrawer

    actual var renderApi: GraphicsApi by redrawerManager::renderApi

    val renderInfo: String
        get() = if (redrawer == null)
            "SkiaLayer isn't initialized yet"
        else
            redrawer!!.renderInfo

    @Volatile
    private var picture: PictureHolder? = null
    private var pictureRecorder: PictureRecorder? = null
    private val pictureLock = Any()

    private fun init(recreation: Boolean = false) {
        isDisposed = false
        backedLayer.init()
        pictureRecorder = PictureRecorder()
        redrawerManager.findNextWorkingRenderApi(recreation)
        isInited = true
    }

    private val stateChangeListeners =
        mutableMapOf<PropertyKind, MutableList<(SkiaLayer) -> Unit>>()

    fun onStateChanged(kind: PropertyKind, handler: (SkiaLayer) -> Unit) {
        stateChangeListeners.getOrPut(kind, ::mutableListOf) += handler
    }

    private fun notifyChange(kind: PropertyKind) {
        stateChangeListeners[kind]?.let { handlers ->
            handlers.forEach { it(this) }
        }
    }

    open fun dispose() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        if (isInited && !isDisposed) {
            // we should dispose redrawer first (to cancel `draw` in rendering thread)
            redrawer?.dispose()
            redrawerManager.dispose()
            picture?.instance?.close()
            picture = null
            pictureRecorder?.close()
            pictureRecorder = null
            backedLayer.dispose()
            peerBufferSizeFixJob?.cancel()
            isDisposed = true
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun reshape(x: Int, y: Int, w: Int, h: Int) {
        @Suppress("DEPRECATION")
        super.reshape(x, y, w, h)

        // Calling renderImmediately as early as possible improves the situation with
        // the visual glitch when the drawn content is scaled during window resize.
        // Note, however, that this actually causes the reverse glitch (content appears
        // scaled in the other direction from the window size), but this seems to
        // happen less often.
        //
        // Calling redraw during layout might break software renderers,
        // so apply this fix only for the Direct3D case.
        if (renderApi == GraphicsApi.DIRECT3D && isShowing) {
            redrawer?.syncBounds()
            redrawer?.renderImmediately()
        }

        // Setting the bounds of children should be done only in the layout pass,
        // but unfortunately, Compose expects the drawing area to be resized
        // immediately when `SkiaLayer` is resized.
        validate()
    }

    override fun doLayout() {
        Logger.debug { "doLayout on $this" }
        backedLayer.setBounds(
            0,
            0,
            adjustSizeToContentScale(contentScale, width),
            adjustSizeToContentScale(contentScale, height)
        )
        backedLayer.validate()
    }

    override fun paint(g: Graphics) {
        Logger.debug { "paint called on SkiaLayer $this" }
        checkContentScale()
        redrawer?.needRender(throttledToVsync = false)
    }

    // Workaround for JBR-5274 and JBR-5305
    fun checkContentScale() {
        val currentGraphicsContextScaleTransform = graphicsConfiguration.defaultTransform
        if (currentGraphicsContextScaleTransform != latestReceivedGraphicsContextScaleTransform) {
            firePropertyChange(
                "graphicsContextScaleTransform",
                latestReceivedGraphicsContextScaleTransform,
                currentGraphicsContextScaleTransform
            )
        }
    }

    // We need to delegate all event listeners to the Canvas (so and focus/input)
    // Canvas is heavyweight AWT component, JPanel is lightweight Swing component
    // Event handling doesn't properly work when we mix heavyweight and lightweight components.
    // For example, Canvas will eat all mouse events
    // (see "mouse events on a heavyweight component do not fall through to its parent",
    // https://www.comp.nus.edu.sg/~cs3283/ftp/Java/swingConnect/archive/tech_topics_arch/mixing/mixing.html)

    override fun enableInputMethods(enable: Boolean) {
        backedLayer.enableInputMethods(enable)
    }

    override fun getInputMethodListeners(): Array<InputMethodListener> {
        return backedLayer.getInputMethodListeners()
    }

    override fun processInputMethodEvent(e: InputMethodEvent?) {
        backedLayer.doProcessInputMethodEvent(e)
    }

    override fun addFocusListener(l: FocusListener?) {
        backedLayer.addFocusListener(l)
    }

    override fun removeFocusListener(l: FocusListener?) {
        backedLayer.removeFocusListener(l)
    }

    override fun setFocusable(focusable: Boolean) {
        backedLayer.isFocusable = focusable
    }

    override fun isFocusable(): Boolean {
        return backedLayer.isFocusable
    }

    override fun hasFocus(): Boolean {
        return backedLayer.hasFocus()
    }

    override fun isFocusOwner(): Boolean {
        return backedLayer.isFocusOwner
    }

    override fun requestFocus() {
        backedLayer.requestFocus()
    }

    override fun requestFocus(cause: FocusEvent.Cause?) {
        backedLayer.requestFocus(cause)
    }

    override fun requestFocusInWindow(): Boolean {
        return backedLayer.requestFocusInWindow()
    }

    override fun requestFocusInWindow(cause: FocusEvent.Cause?): Boolean {
        return backedLayer.requestFocusInWindow(cause)
    }

    override fun setFocusTraversalKeysEnabled(focusTraversalKeysEnabled: Boolean) {
        backedLayer.focusTraversalKeysEnabled = focusTraversalKeysEnabled
    }

    override fun getFocusTraversalKeysEnabled(): Boolean {
        return backedLayer.focusTraversalKeysEnabled
    }

    override fun addInputMethodListener(l: InputMethodListener) {
        super.addInputMethodListener(l)
        backedLayer.addInputMethodListener(l)
    }

    override fun addMouseListener(l: MouseListener) {
        backedLayer.addMouseListener(l)
    }

    override fun addMouseMotionListener(l: MouseMotionListener) {
        backedLayer.addMouseMotionListener(l)
    }

    override fun addMouseWheelListener(l: MouseWheelListener) {
        backedLayer.addMouseWheelListener(l)
    }

    override fun addKeyListener(l: KeyListener) {
        backedLayer.addKeyListener(l)
    }

    override fun removeInputMethodListener(l: InputMethodListener) {
        super.removeInputMethodListener(l)
        backedLayer.removeInputMethodListener(l)
    }

    override fun removeMouseListener(l: MouseListener) {
        backedLayer.removeMouseListener(l)
    }

    override fun removeMouseMotionListener(l: MouseMotionListener) {
        backedLayer.removeMouseMotionListener(l)
    }

    override fun removeMouseWheelListener(l: MouseWheelListener) {
        backedLayer.removeMouseWheelListener(l)
    }

    override fun removeKeyListener(l: KeyListener) {
        backedLayer.removeKeyListener(l)
    }

    /**
     * Redraw on the next animation Frame (on vsync signal if vsync is enabled).
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

    internal fun update(nanoTime: Long) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        checkContentScale()
        FrameWatcher.nextFrame()

        // The current approach is to render into a picture in the main thread, and render this picture in the render thread
        // If this approach will be changed, create an issue in https://youtrack.jetbrains.com/issues/CMP for changing it in
        // https://github.com/JetBrains/compose-multiplatform/blob/e4e2d329709cded91a09cc612d4defbce37aad96/benchmarks/multiplatform/benchmarks/src/commonMain/kotlin/MeasureComposable.kt#L151 as well

        val pictureWidth = (backedLayer.width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (backedLayer.height * contentScale).toInt().coerceAtLeast(0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val pictureRecorder = pictureRecorder!!
        val canvas = pictureRecorder.beginRecording(bounds)

        // clipping
        for (component in clipComponents) {
            canvas.clipRectBy(component, contentScale)
        }

        try {
            isRendering = true
            renderDelegate?.onRender(canvas, pictureWidth, pictureHeight, nanoTime)
        } finally {
            isRendering = false
        }

        // we can dispose layer during onRender
        // or even dispose it and pack it again
        if (!isDisposed && !pictureRecorder.isClosed) {
            synchronized(pictureLock) {
                picture?.instance?.close()
                val picture = pictureRecorder.finishRecordingAsPicture()
                this.picture = PictureHolder(picture, pictureWidth, pictureHeight)
            }
        }
    }

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    internal inline fun inDrawScope(body: () -> Unit) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }
        try {
            fpsCounter?.tick()
            body()
        } catch (e: CancellationException) {
            // ignore
        } catch (e: RenderException) {
            if (!isDisposed) {
                Logger.warn(e) { "Exception in draw scope" }
                redrawerManager.findNextWorkingRenderApi()
                redrawer?.renderImmediately()
            }
        }
    }

    internal actual fun draw(canvas: Canvas) {
        check(!isDisposed) { "SkiaLayer is disposed" }
        lockPicture {
            canvas.drawPicture(it.instance)
        }
    }

    private fun <T : Any> lockPicture(action: (PictureHolder) -> T): T? {
        return synchronized(pictureLock) {
            val picture = picture
            if (picture != null) {
                action(picture)
            } else {
                null
            }
        }
    }

    // Captures current layer as bitmap.
    fun screenshot(): Bitmap? {
        check(!isDisposed) { "SkiaLayer is disposed" }
        return lockPicture { picture ->
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

    fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        backedLayer.requestNativeFocusOnAccessible(accessible)
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
fun SkiaLayer.disableTitleBar(customHeaderHeight: Float) {
    backedLayer.disableTitleBar(customHeaderHeight)
}

/**
 * Request to show emoji and symbols popup.
 */
fun orderEmojiAndSymbolsPopup() {
    platformOperations.orderEmojiAndSymbolsPopup()
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

internal fun Canvas.clipRectBy(rectangle: ClipRectangle, scale: Float) {
    clipRect(
        Rect.makeLTRB(
            rectangle.x * scale,
            rectangle.y * scale,
            (rectangle.x + rectangle.width) * scale,
            (rectangle.y + rectangle.height) * scale
        ),
        ClipMode.DIFFERENCE,
        true
    )
}

// TODO Recheck this method validity in 2 cases - full Window content, and a Panel content
//  issue: https://youtrack.jetbrains.com/issue/CMP-5447/Window-white-line-on-the-bottom-before-resizing
//  suggestions: https://github.com/JetBrains/skiko/pull/988#discussion_r1763219300
//  possible issues:
//  - isn't obvious why 0.4/0.6 is used
//  - increasing it by one, we avoid 1px white line, but we cut the content by 1px
//  - it probably doesn't work correctly in a Panel content case - we don't need to adjust in this case
/**
 * Increases the value of width/height by one if necessary,
 * to avoid 1px white line between Canvas and the bounding window.
 * 1px white line appears when users resizes the window:
 * - it is resized in Px (125, 126, 127,...)
 * - the canvas is resized in Points (with 1.25 scale, it will be 100, 100, 101)
 * during converting Int AWT points to actual screen pixels.
 */
private fun adjustSizeToContentScale(contentScale: Float, value: Int): Int {
    val scaled = value * contentScale
    val diff = scaled - floor(scaled)
    return if (diff > 0.4f && diff < 0.6f) {
        value + 1
    } else {
        value
    }
}
