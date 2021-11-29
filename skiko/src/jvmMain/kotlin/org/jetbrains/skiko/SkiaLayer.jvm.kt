package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skiko.context.ContextHandler
import org.jetbrains.skiko.redrawer.Redrawer
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.event.*
import java.awt.event.KeyEvent.*
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
                skikoView?.onKeyboardEvent(toSkikoEvent(e))
            }
            override fun keyReleased(e: KeyEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(e))
            }
        })

        addInputMethodListener(object : InputMethodListener {
            override fun caretPositionChanged(e: InputMethodEvent) {
                skikoView?.onInputEvent(toSkikoEvent(e))
            }
            override fun inputMethodTextChanged(e: InputMethodEvent) {
                skikoView?.onInputEvent(toSkikoEvent(e))
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

    internal fun draw(canvas: Canvas) {
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
actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = InputMethodEvent
actual typealias SkikoPlatformKeyboardEvent = KeyEvent
actual typealias SkikoPlatformPointerEvent = MouseEvent

actual enum class SkikoKey(val value: Int) {
    KEY_UNKNOWN(-1),
    KEY_A(VK_A),
    KEY_S(VK_B),
    KEY_D(VK_D),
    KEY_F(VK_F),
    KEY_H(VK_H),
    KEY_G(VK_G),
    KEY_Z(VK_Z),
    KEY_X(VK_X),
    KEY_C(VK_C),
    KEY_V(VK_V),
    KEY_B(VK_B),
    KEY_Q(VK_Q),
    KEY_W(VK_W),
    KEY_E(VK_E),
    KEY_R(VK_R),
    KEY_Y(VK_Y),
    KEY_T(VK_T),
    KEY_U(VK_U),
    KEY_I(VK_I),
    KEY_P(VK_P),
    KEY_L(VK_L),
    KEY_J(VK_J),
    KEY_K(VK_K),
    KEY_N(VK_N),
    KEY_M(VK_M),
    KEY_O(VK_O),
    KEY_1(VK_1),
    KEY_2(VK_2),
    KEY_3(VK_3),
    KEY_4(VK_4),
    KEY_5(VK_5),
    KEY_6(VK_6),
    KEY_7(VK_7),
    KEY_8(VK_8),
    KEY_9(VK_9),
    KEY_0(VK_0),
    KEY_CLOSE_BRACKET(VK_CLOSE_BRACKET),
    KEY_OPEN_BRACKET(VK_OPEN_BRACKET),
    KEY_QUOTE(VK_QUOTE),
    KEY_SEMICOLON(VK_SEMICOLON),
    KEY_SLASH(VK_SLASH),
    KEY_COMMA(VK_COMMA),
    KEY_BACKSLASH(VK_BACK_SLASH),
    KEY_PERIOD(VK_PERIOD),
    KEY_BACK_QUOTE(VK_BACK_QUOTE),
    KEY_EQUALS(VK_EQUALS),
    KEY_MINUS(VK_MINUS),
    KEY_ENTER(VK_ENTER),
    KEY_ESCAPE(VK_ESCAPE),
    KEY_TAB(VK_TAB),
    KEY_BACKSPACE(VK_BACK_SPACE),
    KEY_SPACE(VK_SPACE),
    KEY_CAPSLOCK(VK_CAPS_LOCK),
    KEY_LEFT_META(VK_META),
    KEY_LEFT_SHIFT(VK_SHIFT),
    KEY_LEFT_ALT(VK_ALT),
    KEY_LEFT_CONTROL(VK_CONTROL),
    KEY_RIGHT_META(0x80000000.toInt() or VK_META),
    KEY_RIGHT_SHIFT(0x80000000.toInt() or VK_SHIFT),
    KEY_RIGHT_ALT(VK_ALT_GRAPH),
    KEY_RIGHT_CONTROL(0x80000000.toInt() or VK_CONTROL),
    KEY_UP(VK_UP),
    KEY_DOWN(VK_DOWN),
    KEY_LEFT(VK_LEFT),
    KEY_RIGHT(VK_RIGHT),
    KEY_F1(VK_F1),
    KEY_F2(VK_F2),
	KEY_F3(VK_F3),
    KEY_F4(VK_F4),
    KEY_F5(VK_F5),
	KEY_F6(VK_F6),
	KEY_F7(VK_F7),
	KEY_F8(VK_F8),
	KEY_F9(VK_F9),
    KEY_F10(VK_F10),
	KEY_F11(VK_F11),
	KEY_F12(VK_F12),
	KEY_PRINTSCEEN(VK_PRINTSCREEN),
	KEY_SCROLL_LOCK(VK_SCROLL_LOCK),
	KEY_PAUSE(VK_PAUSE),
    KEY_INSERT(VK_INSERT),
    KEY_HOME(VK_HOME),
	KEY_PGUP(VK_PAGE_UP),
    KEY_DELETE(VK_DELETE),
    KEY_END(VK_END),
    KEY_PGDOWN(VK_PAGE_DOWN),
    KEY_NUM_LOCK(VK_NUM_LOCK),
    KEY_NUMPAD_0(VK_NUMPAD0),
    KEY_NUMPAD_1(VK_NUMPAD1),
    KEY_NUMPAD_2(VK_NUMPAD2),
    KEY_NUMPAD_3(VK_NUMPAD3),
    KEY_NUMPAD_4(VK_NUMPAD4),
    KEY_NUMPAD_5(VK_NUMPAD5),
    KEY_NUMPAD_6(VK_NUMPAD6),
    KEY_NUMPAD_7(VK_NUMPAD7),
    KEY_NUMPAD_8(VK_NUMPAD8),
    KEY_NUMPAD_9(VK_NUMPAD9),
    KEY_NUMPAD_ENTER(0x80000000.toInt() or VK_ENTER),
    KEY_NUMPAD_ADD(VK_ADD),
    KEY_NUMPAD_SUBTRACT(VK_SUBTRACT),
    KEY_NUMPAD_MULTIPLY(VK_MULTIPLY),
    KEY_NUMPAD_DIVIDE(VK_DIVIDE),
    KEY_NUMPAD_DECIMAC(VK_DECIMAL);

    companion object {
        fun valueOf(value: Int): SkikoKey {
            val key = SkikoKey.values().firstOrNull { it.value == value }
            return if (key == null) SkikoKey.KEY_UNKNOWN else key
        }
    }
}