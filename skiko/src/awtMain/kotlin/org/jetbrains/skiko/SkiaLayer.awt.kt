package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.event.*
import java.awt.im.InputMethodRequests
import java.awt.Window
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
    private val renderFactory: RenderFactory = RenderFactory.Default
) : JPanel() {

    companion object {
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
    ) : this(
        externalAccessibleFactory,
        SkiaLayerProperties(
            isVsyncEnabled,
            isVsyncFramelimitFallbackEnabled,
            renderApi
        ),
        RenderFactory.Default
    )

    val canvas: java.awt.Canvas
        get() = backedLayer

    private var vrrDisplayLocker: JPanel? = null

    init {
        isOpaque = false
        layout = null
        backedLayer = object : HardwareLayer(externalAccessibleFactory) {
            override fun paint(g: Graphics) {
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
        }
        @Suppress("LeakingThis")
        add(backedLayer)
        backedLayer.addHierarchyListener {
            if (it.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                if (
                    vrrDisplayLocker == null
                    && hostOs == OS.Windows
                    && (renderApi == GraphicsApi.DIRECT3D || renderApi == GraphicsApi.OPENGL)
                ) {
                    vrrDisplayLocker = JPanel().apply {
                        background = Color(0, 0, 0, 0)
                    }
                    add(vrrDisplayLocker, Integer.valueOf(0))
                }
                checkShowing()
            }
        }
    }

    private var fullscreenAdapter = FullscreenAdapter(backedLayer)

    override fun removeNotify() {
        val window = SwingUtilities.getRoot(this) as Window
        window.removeComponentListener(fullscreenAdapter)
        dispose()
        super.removeNotify()
    }

    override fun addNotify() {
        super.addNotify()
        val window = SwingUtilities.getRoot(this) as Window
        window.addComponentListener(fullscreenAdapter)
        backedLayer.defineContentScale()
        checkShowing()
        init(isInited)
    }


    actual fun detach() {
        dispose()
    }

    private var isInited = false
    private var isRendering = false

    private fun checkShowing() {
        isShowingCached = super.isShowing()
        if (isShowing) {
            repaint()
        }
    }

    private var isShowingCached = false

    override fun isShowing(): Boolean {
        return isShowingCached
    }

    actual val contentScale: Float
        get() = backedLayer.contentScale

    val contentHandle: Long
        get() = backedLayer.contentHandle

    val windowHandle: Long
        get() = backedLayer.windowHandle

    actual var fullscreen: Boolean
        get() = fullscreenAdapter.fullscreen
        set(value) {
            fullscreenAdapter.fullscreen = value
        }

    actual var skikoView: SkikoView? = null

    actual fun attachTo(container: Any) {
        attachTo(container as JComponent)
    }

    fun attachTo(jComponent: JComponent) {
        jComponent.add(this)
    }

    private var keyEvent: KeyEvent? = null

    fun addView(view: SkikoView) {
        skikoView = view
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
            override fun mouseReleased(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
            override fun mouseEntered(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
            override fun mouseExited(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
            override fun mouseMoved(e: MouseEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
        })

        addMouseWheelListener(object : MouseWheelListener {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(e))
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                keyEvent = e
                skikoView?.onKeyboardEvent(toSkikoEvent(e))
            }
            override fun keyReleased(e: KeyEvent) {
                keyEvent = e
                skikoView?.onKeyboardEvent(toSkikoEvent(e))
            }
            override fun keyTyped(e: KeyEvent) {
                skikoView?.onInputEvent(toSkikoTypeEvent(e, keyEvent))
            }
        })

        addInputMethodListener(object : InputMethodListener {
            override fun caretPositionChanged(e: InputMethodEvent) {
                skikoView?.onInputEvent(toSkikoTypeEvent(e, keyEvent))
            }
            override fun inputMethodTextChanged(e: InputMethodEvent) {
                skikoView?.onInputEvent(toSkikoTypeEvent(e, keyEvent))
            }
        })
    }

    val clipComponents = mutableListOf<ClipRectangle>()

    @Volatile
    private var isDisposed = false
    internal var redrawer: Redrawer? = null
    private val fallbackRenderApiQueue = SkikoProperties.fallbackRenderApiQueue(properties.renderApi).toMutableList()
    private var renderApi_ = fallbackRenderApiQueue[0]
    actual var renderApi: GraphicsApi
        get() = renderApi_
        private set(value) {
            this.renderApi_ = value
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

    private fun findNextWorkingRenderApi() {
        var thrown: Boolean
        do {
            thrown = false
            try {
                renderApi = fallbackRenderApiQueue.removeAt(0)
                redrawer?.dispose()
                redrawer = renderFactory.createRedrawer(this, renderApi, properties)
                redrawer?.syncSize()
            } catch (e: RenderException) {
                println(e.message)
                thrown = true
            }
        } while (thrown && fallbackRenderApiQueue.isNotEmpty())

        if (thrown && fallbackRenderApiQueue.isEmpty()) {
            throw RenderException("Cannot fallback to any render API")
        }
    }

    private fun init(recreation: Boolean = false) {
        isDisposed = false
        backedLayer.init()
        pictureRecorder = PictureRecorder()
        if (recreation) {
            fallbackRenderApiQueue.add(0, renderApi)
        }
        findNextWorkingRenderApi()
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
            redrawer = null
            picture?.instance?.close()
            picture = null
            pictureRecorder?.close()
            pictureRecorder = null
            backedLayer.dispose()
            isDisposed = true
        }
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        var roundedWidth = width
        var roundedHeight = height
        if (isInited) {
            roundedWidth = roundSize(width)
            roundedHeight = roundSize(height)
        }
        super.setBounds(x, y, roundedWidth, roundedHeight)
        backedLayer.setSize(roundedWidth, roundedHeight)
        vrrDisplayLocker?.setBounds(0, 0, 1, 1)
        redrawer?.syncSize()
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (backedLayer.checkContentScale()) {
            notifyChange(PropertyKind.ContentScale)
        }
        redrawer?.syncSize() // setBounds not always called (for example when we change density on Linux

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

    override fun requestFocus() {
        backedLayer.requestFocus()
    }

    override fun requestFocus(cause: FocusEvent.Cause?) {
        backedLayer.requestFocus(cause)
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

    override fun setFocusTraversalKeysEnabled(focusTraversalKeysEnabled: Boolean) {
        backedLayer.focusTraversalKeysEnabled = focusTraversalKeysEnabled
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

        FrameWatcher.nextFrame()
        fpsCounter?.tick()

        val pictureWidth = (width * contentScale).toInt().coerceAtLeast(0)
        val pictureHeight = (height * contentScale).toInt().coerceAtLeast(0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder!!.beginRecording(bounds)

        // clipping
        for (component in clipComponents) {
            canvas.clipRectBy(component)
        }

        try {
            isRendering = true
            skikoView?.onRender(canvas, pictureWidth, pictureHeight, nanoTime)
        } finally {
            isRendering = false
        }

        // we can dispose layer during onRender
        if (!isDisposed) {
            synchronized(pictureLock) {
                picture?.instance?.close()
                val picture = pictureRecorder!!.finishRecordingAsPicture()
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
                println(e.message)
                findNextWorkingRenderApi()
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
                ColorType.BGRA_8888, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
            store.setImageInfo(ImageInfo(ci, picture.width, picture.height))
            store.allocN32Pixels(picture.width, picture.height)
            val canvas = Canvas(store)
            canvas.drawPicture(picture.instance)
            store.setImmutable()
            store
        }
    }

    private fun Canvas.clipRectBy(rectangle: ClipRectangle) {
        val dpi = contentScale
        clipRect(
            Rect.makeLTRB(
                rectangle.x * dpi,
                rectangle.y * dpi,
                (rectangle.x + rectangle.width) * dpi,
                (rectangle.y + rectangle.height) * dpi
            ),
            ClipMode.DIFFERENCE,
            true
        )
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
}

fun SkiaLayer.disableTitleBar(customHeaderHeight: Float) {
    backedLayer.disableTitleBar(customHeaderHeight)
}

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
        getLongFrameMillis = { fpsLongFramesMillis ?: 1.5 * 1000 / refreshRate }
    )
}

// InputEvent is abstract, so we wrap to match modality.
actual typealias SkikoTouchPlatformEvent = Any
actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = Any
actual typealias SkikoPlatformKeyboardEvent = KeyEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent
