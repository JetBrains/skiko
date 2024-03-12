package org.jetbrains.skiko

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.redrawer.RedrawerManager
import java.awt.Color
import java.awt.Component
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.im.InputMethodRequests
import java.util.concurrent.CancellationException
import javax.accessibility.Accessible
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.SwingUtilities.isEventDispatchThread
import javax.swing.UIManager

actual open class SkiaLayer internal constructor(
    externalAccessibleFactory: ((Component) -> Accessible)? = null,
    private val properties: SkiaLayerProperties,
    private val renderFactory: RenderFactory = RenderFactory.Default,
    private val analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
    actual val pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
) : JPanel() {

    internal companion object {
        init {
            Library.load()
        }
    }

    enum class PropertyKind {
        Renderer,
        ContentScale,
    }

    private var _transparency: Boolean = false
    actual var transparency: Boolean
        get() = _transparency
        set(value) {
            _transparency = value
            if (!value) {
                background = UIManager.getColor("Panel.background")
            } else {
                background = Color(0, 0, 0, 0)
            }
        }

    internal val backedLayer: HardwareLayer

    constructor(
        externalAccessibleFactory: ((Component) -> Accessible)? = null,
        isVsyncEnabled: Boolean = SkikoProperties.vsyncEnabled,
        isVsyncFramelimitFallbackEnabled: Boolean = SkikoProperties.vsyncFramelimitFallbackEnabled,
        renderApi: GraphicsApi = SkikoProperties.renderApi,
        analytics: SkiaLayerAnalytics = SkiaLayerAnalytics.Empty,
        pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN,
    ) : this(
        externalAccessibleFactory,
        SkiaLayerProperties(
            isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled,
            renderApi
        ),
        RenderFactory.Default,
        analytics,
        pixelGeometry
    )

    val canvas: java.awt.Canvas
        get() = backedLayer

    private var peerBufferSizeFixJob: Job? = null
    private var latestReceivedGraphicsContextScaleTransform: AffineTransform? = null

    init {
        isOpaque = false
        layout = null
        backedLayer = object : HardwareLayer(externalAccessibleFactory) {
            override fun paint(g: java.awt.Graphics) {
                Logger.debug { "Paint called on $this" }
                checkContentScale()

                // 1. JPanel.paint is not always called (in rare cases).
                //    For example if we call 'jframe.isResizable = false` on Ubuntu
                //
                // 2. HardwareLayer.paint is also not always called.
                //    For example, on macOs when we resize window or change DPI
                //
                // 3. to avoid double paint in one single frame, use needRedraw instead of redrawImmediately
                redrawer?.needRedraw()
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

        backedLayer.addHierarchyListener {
            if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                checkShowing()
            }
        }


        addPropertyChangeListener("graphicsContextScaleTransform") {
            Logger.debug { "graphicsContextScaleTransform changed for $this" }
            latestReceivedGraphicsContextScaleTransform = it.newValue as AffineTransform
            redrawer?.syncSize()
            notifyChange(PropertyKind.ContentScale)

            // Workaround for JBR-5259
            if (hostOs == OS.Windows) {
                peerBufferSizeFixJob?.cancel()
                peerBufferSizeFixJob = GlobalScope.launch(MainUIDispatcher) {
                    backedLayer.setLocation(1, 0)
                    backedLayer.setLocation(0, 0)
                }
            }
        }
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
        isShowingCached = super.isShowing()
        if (wasShowing != isShowing) {
            redrawer?.setVisible(isShowing)
        }
        if (isShowing) {
            redrawer?.syncSize()
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

    private var keyEvent: KeyEvent? = null

    val clipComponents = mutableListOf<ClipRectangle>()

    @Volatile
    private var isDisposed = false

    private val redrawerManager = RedrawerManager<Redrawer>(properties.renderApi) { renderApi, oldRedrawer ->
        oldRedrawer?.dispose()
        val newRedrawer = renderFactory.createRedrawer(this, renderApi, analytics, properties)
        newRedrawer.syncSize()
        newRedrawer
    }

    internal val redrawer: Redrawer?
        get() = redrawerManager.redrawer

    actual var renderApi: GraphicsApi
        get() = redrawerManager.renderApi
        set(value) {
            redrawerManager.forceRenderApi(value)
            notifyChange(PropertyKind.Renderer)
        }

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

    private val stateHandlers =
        mutableMapOf<PropertyKind, MutableList<(SkiaLayer) -> Unit>>()

    fun onStateChanged(kind: PropertyKind, handler: (SkiaLayer) -> Unit) {
        stateHandlers.getOrPut(kind, { mutableListOf() }) += handler
    }

    private fun notifyChange(kind: PropertyKind) {
        stateHandlers.get(kind)?.let { handlers ->
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

    override fun doLayout() {
        Logger.debug { "doLayout on $this" }
        backedLayer.setBounds(0, 0, roundSize(width), roundSize(height))
        backedLayer.validate()
        redrawer?.syncSize()
    }


    override fun paint(g: java.awt.Graphics) {
        Logger.debug { "Paint called on: $this" }
        checkContentScale()

        // `paint` can be called when we already inside `draw` method.
        //
        // For example if we call some AWT function inside renderer.onRender,
        // such as `jframe.isEnabled = false` on Linux
        //
        // To avoid recursive call of `draw` (we don't support recursive calls) we just schedule redrawing.
        if (isRendering) {
            redrawer?.needRedraw()
        } else {
            redrawer?.redrawImmediately()
        }
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
    actual fun needRedraw() {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }
        redrawer?.needRedraw()
    }

    @Suppress("LeakingThis")
    private val fpsCounter = defaultFPSCounter(this)

    internal fun update(nanoTime: Long) {
        check(isEventDispatchThread()) { "Method should be called from AWT event dispatch thread" }
        check(!isDisposed) { "SkiaLayer is disposed" }

        checkContentScale()

        FrameWatcher.nextFrame()
        fpsCounter?.tick()

        val pictureWidth = (width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (height * contentScale).toInt().coerceAtLeast(0)

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
                redrawerManager.findNextWorkingRenderApi()
                redrawer?.redrawImmediately()
            }
        }
    }

    actual internal fun draw(canvas: Canvas) {
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

    private fun roundSize(value: Int): Int {
        var rounded = value * contentScale
        val diff = rounded - rounded.toInt()
        // We check values close to 0.5 and edit the size to avoid white lines glitch
        if (diff > 0.4f && diff < 0.6f) {
            rounded = value + 1f
        } else {
            rounded = value.toFloat()
        }
        return rounded.toInt()
    }

    fun requestNativeFocusOnAccessible(accessible: Accessible?) {
        backedLayer.requestNativeFocusOnAccessible(accessible)
    }
}

/**
 * Disable showing window title bar.
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
    if (!SkikoProperties.fpsEnabled) return@with null

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

// InputEvent is abstract, so we wrap to match modality.
actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoPlatformKeyboardEvent = KeyEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent
