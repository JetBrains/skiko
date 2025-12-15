@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.*
import org.jetbrains.skiko.internal.x11.*
import org.jetbrains.skiko.internal.x11.Cursor as X11Cursor
import platform.posix.usleep

private const val DEFAULT_WIDTH = 640
private const val DEFAULT_HEIGHT = 480
private const val XK_ESCAPE: ULong = 0xff1bUL
private const val FALLBACK_REFRESH_RATE_HZ = 60.0
private const val REFRESH_RATE_POLL_INTERVAL_NANOS = 1_000_000_000L

/**
 * Minimal X11 window host for Kotlin/Native Linux.
 *
 * Rendering is delegated to a platform [org.jetbrains.skiko.redrawer.Redrawer] created by [SkiaLayer.attachTo].
 */
class X11SkikoWindow(
    private val layer: SkiaLayer,
    initialWidth: Int = DEFAULT_WIDTH,
    initialHeight: Int = DEFAULT_HEIGHT,
    title: String = "Skiko Native (X11)",
    private val closeOnEscape: Boolean = true,
    private val onMouseMove: ((x: Int, y: Int) -> Unit)? = null,
    private val onButtonPress: ((button: Int, x: Int, y: Int, ctrl: Boolean) -> Unit)? = null,
    private val onButtonRelease: ((button: Int) -> Unit)? = null,
) : LinuxCursorHost, AutoCloseable {
    val contentScale: Float = 1f

    var width: Int = initialWidth
        private set
    var height: Int = initialHeight
        private set

    private var needsRender: Boolean = true
    private var running: Boolean = true
    private var throttledToVsync: Boolean = true
    private var displayRefreshRateHz: Double = FALLBACK_REFRESH_RATE_HZ
    private var lastRefreshRatePollTimeNanos: Long = 0L
    private var nextFrameTimeNanos: Long = 0L

    internal val display: CPointer<Display> =
        XOpenDisplay(null) ?: error("XOpenDisplay failed. Is DISPLAY set and X server running?")
    internal val screen: Int = XDefaultScreen(display)
    private val rootWindow: Window = XRootWindow(display, screen)

    internal val glxFbConfig: GLXFBConfig?
    internal val window: Window
    internal val gc: GC
    private val colormap: Colormap?
    private val wmDeleteWindow: Atom

    private val xCursors = mutableMapOf<Int, X11Cursor>()
    private var appliedXCursor: X11Cursor? = null

    internal fun requestRender(throttledToVsync: Boolean) {
        this.throttledToVsync = throttledToVsync
        needsRender = true
    }

    override var cursor: Cursor? = null
        set(value) {
            field = value
            applyCursor(value)
        }

    init {
        val (createdWindow, fbConfig, createdColormap) = try {
            // Create a GLX-capable window when possible, so we can fall back to OpenGL if Vulkan isn't available.
            createGlxWindow(display, screen, rootWindow, width, height)
        } catch (_: Throwable) {
            Triple(
                XCreateSimpleWindow(
                    display,
                    rootWindow,
                    0,
                    0,
                    width.convert(),
                    height.convert(),
                    0.convert(),
                    0.convert(),
                    0.convert(),
                ),
                null,
                null,
            )
        }
        window = createdWindow
        glxFbConfig = fbConfig
        colormap = createdColormap

        XStoreName(display, window, title)

        wmDeleteWindow = XInternAtom(display, "WM_DELETE_WINDOW", 0)
        memScoped {
            val protocols = allocArray<AtomVar>(1)
            protocols[0] = wmDeleteWindow
            XSetWMProtocols(display, window, protocols, 1)
        }

        val eventMask = (
            ExposureMask or
                KeyPressMask or
                StructureNotifyMask or
                ButtonPressMask or
                ButtonReleaseMask or
                PointerMotionMask
            ).convert<Long>()
        XSelectInput(display, window, eventMask)

        gc = XCreateGC(display, window, 0.convert(), null) ?: error("XCreateGC failed")
        XMapWindow(display, window)
        XFlush(display)

        layer.attachTo(this)

        val now = currentNanoTime()
        lastRefreshRatePollTimeNanos = now - REFRESH_RATE_POLL_INTERVAL_NANOS
        nextFrameTimeNanos = now
    }

    fun run() {
        while (running) {
            processEvents()

            val now = currentNanoTime()
            if (throttledToVsync && now - lastRefreshRatePollTimeNanos >= REFRESH_RATE_POLL_INTERVAL_NANOS) {
                lastRefreshRatePollTimeNanos = now
                queryDisplayRefreshRateHz()?.let { rate ->
                    displayRefreshRateHz = rate
                }
            }

            if (needsRender && (!throttledToVsync || now >= nextFrameTimeNanos)) {
                needsRender = false
                try {
                    layer.redrawer?.renderImmediately()
                } catch (t: Throwable) {
                    if (!layer.tryFallbackFromRenderFailure(t)) {
                        Logger.error(t) { "Render failed and no further fallback is available; closing window" }
                        running = false
                    } else {
                        needsRender = true
                    }
                }

                if (throttledToVsync) {
                    val periodNanos = framePeriodNanos(displayRefreshRateHz)
                    val afterRender = currentNanoTime()
                    val scheduled = nextFrameTimeNanos + periodNanos
                    nextFrameTimeNanos = maxOf(scheduled, afterRender)
                }
            }

            val sleepNanos = when {
                !needsRender -> 1_000_000L
                !throttledToVsync -> 0L
                else -> nextFrameTimeNanos - currentNanoTime()
            }
            sleepForNanos(sleepNanos)
        }
    }

    private fun processEvents() = memScoped {
        val event = alloc<XEvent>()
        while (XPending(display) > 0) {
            XNextEvent(display, event.ptr)
            when (event.type) {
                Expose -> {
                    needsRender = true
                }
                ConfigureNotify -> {
                    val newW = event.xconfigure.width
                    val newH = event.xconfigure.height
                    if (newW != width || newH != height) {
                        width = newW
                        height = newH
                        layer.redrawer?.syncBounds()
                    }
                    needsRender = true
                }
                MotionNotify -> {
                    onMouseMove?.invoke(event.xmotion.x, event.xmotion.y)
                    needsRender = true
                }
                ButtonPress -> {
                    val ctrl = (event.xbutton.state and ControlMask.convert()) != 0.convert<UInt>()
                    onButtonPress?.invoke(event.xbutton.button.toInt(), event.xbutton.x, event.xbutton.y, ctrl)
                    needsRender = true
                }
                ButtonRelease -> {
                    onButtonRelease?.invoke(event.xbutton.button.toInt())
                    needsRender = true
                }
                KeyPress -> {
                    val keySym = XLookupKeysym(event.xkey.ptr, 0)
                    if (closeOnEscape && keySym == XK_ESCAPE) {
                        running = false
                    } else {
                        needsRender = true
                    }
                }
                ClientMessage -> {
                    val isWmDelete = event.xclient.data.l[0] == wmDeleteWindow.toLong()
                    if (isWmDelete) {
                        running = false
                    }
                }
            }
        }
    }

    private fun applyCursor(cursor: Cursor?) {
        val id = cursor as? PredefinedCursorsId
        if (id == null) {
            XUndefineCursor(display, window)
            XFlush(display)
            return
        }

        val shape = when (id) {
            PredefinedCursorsId.DEFAULT -> XC_left_ptr
            PredefinedCursorsId.CROSSHAIR -> XC_crosshair
            PredefinedCursorsId.HAND -> XC_hand2
            PredefinedCursorsId.TEXT -> XC_xterm
        }

        val xCursor = xCursors.getOrPut(shape) {
            XCreateFontCursor(display, shape.convert())
        }

        if (appliedXCursor != xCursor) {
            appliedXCursor = xCursor
            XDefineCursor(display, window, xCursor)
            XFlush(display)
        }
    }

    override fun close() {
        running = false

        // Dispose rendering resources first (they may depend on the X11 window/display).
        layer.detach()

        appliedXCursor?.let {
            XUndefineCursor(display, window)
            appliedXCursor = null
        }
        xCursors.values.forEach { XFreeCursor(display, it) }
        xCursors.clear()

        XFreeGC(display, gc)
        colormap?.let { XFreeColormap(display, it) }
        XDestroyWindow(display, window)
        XCloseDisplay(display)
    }

    private fun queryDisplayRefreshRateHz(): Double? = memScoped {
        val screenResources = XRRGetScreenResourcesCurrent(display, window) ?: return@memScoped null
        try {
            val modes = screenResources.pointed.modes ?: return@memScoped null
            val nMode = screenResources.pointed.nmode

            fun rateForMode(modeId: RRMode): Double {
                if (modeId == 0.convert<RRMode>()) return 0.0
                for (i in 0 until nMode) {
                    val info = modes[i]
                    if (info.id == modeId) {
                        val hTotal = info.hTotal.toDouble()
                        val vTotal = info.vTotal.toDouble()
                        if (hTotal <= 0.0 || vTotal <= 0.0) return 0.0
                        return info.dotClock.toDouble() / (hTotal * vTotal)
                    }
                }
                return 0.0
            }

            val crtcs = screenResources.pointed.crtcs ?: return@memScoped null
            val nCrtc = screenResources.pointed.ncrtc
            var maxRate = 0.0
            for (i in 0 until nCrtc) {
                val info = XRRGetCrtcInfo(display, screenResources, crtcs[i]) ?: continue
                val rate = rateForMode(info.pointed.mode)
                if (rate > maxRate) maxRate = rate
                XRRFreeCrtcInfo(info)
            }

            maxRate.takeIf { it.isFinite() && it > 1.0 }
        } finally {
            XRRFreeScreenResources(screenResources)
        }
    }
}

private fun createGlxWindow(
    display: CPointer<Display>,
    screen: Int,
    rootWindow: Window,
    width: Int,
    height: Int,
): Triple<Window, GLXFBConfig, Colormap?> = memScoped {
    val attribs = intArrayOf(
        GLX_X_RENDERABLE,
        1,
        GLX_DRAWABLE_TYPE,
        GLX_WINDOW_BIT,
        GLX_RENDER_TYPE,
        GLX_RGBA_BIT,
        GLX_X_VISUAL_TYPE,
        GLX_TRUE_COLOR,
        GLX_RED_SIZE,
        8,
        GLX_GREEN_SIZE,
        8,
        GLX_BLUE_SIZE,
        8,
        GLX_ALPHA_SIZE,
        8,
        GLX_DEPTH_SIZE,
        24,
        GLX_STENCIL_SIZE,
        8,
        GLX_DOUBLEBUFFER,
        1,
        0,
    )

    val fbCount = alloc<IntVar>()
    val fbConfigs = attribs.usePinned { pinned ->
        glXChooseFBConfig(display, screen, pinned.addressOf(0), fbCount.ptr)
    } ?: error("glXChooseFBConfig returned null")
    try {
        val fbConfig = fbConfigs[0] ?: error("glXChooseFBConfig returned empty list")
        val visualInfo = glXGetVisualFromFBConfig(display, fbConfig) ?: error("glXGetVisualFromFBConfig failed")
        try {
            val colormap = XCreateColormap(display, rootWindow, visualInfo.pointed.visual, AllocNone)
            val swa = alloc<XSetWindowAttributes>()
            swa.colormap = colormap

            val win = XCreateWindow(
                display,
                rootWindow,
                0,
                0,
                width.convert(),
                height.convert(),
                0.convert(),
                visualInfo.pointed.depth,
                InputOutput,
                visualInfo.pointed.visual,
                CWColormap.convert(),
                swa.ptr,
            )

            Triple(win, fbConfig, colormap)
        } finally {
            XFree(visualInfo)
        }
    } finally {
        XFree(fbConfigs)
    }
}

private fun framePeriodNanos(refreshRateHz: Double): Long {
    val safeRate = refreshRateHz.takeIf { it.isFinite() && it > 1.0 } ?: FALLBACK_REFRESH_RATE_HZ
    return (1_000_000_000.0 / safeRate).toLong().coerceAtLeast(1L)
}

private fun sleepForNanos(nanos: Long) {
    if (nanos <= 0) return
    val micros = nanos / 1_000
    if (micros <= 0) return
    usleep(micros.coerceAtMost(UInt.MAX_VALUE.toLong()).toUInt())
}
