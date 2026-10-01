package org.jetbrains.skiko

import kotlinx.cinterop.*
import x11gl.*

class X11Window(
    title: String,
    width: Int,
    height: Int,
) : LinuxWindow {
    private val arena = Arena()

    val display: CPointer<Display> =
        XOpenDisplay(null)
            ?: throw RenderSetupException("Cannot open X11 display; is DISPLAY set?")
    val screen: Int = XDefaultScreen(display)

    val visualInfo: CPointer<XVisualInfo> =
        chooseVisual()
            ?: throw RenderSetupException("No GLX visual with RGBA + double buffer + stencil")

    val window: Window
    val wmDeleteWindow: Atom

    /**
     * Scale factor derived from `Xft.dpi` (96 dpi = 1.0). X11 has no per-window backing
     * scale: all geometry in this class is physical pixels. This factor converts the
     * logical size requested by the constructor and feeds the UI density upstream.
     */
    override val contentScale: Float = readXftDpi()?.let { it / 96f } ?: 1f

    /** Current width in physical pixels; the constructor argument is in logical units. */
    override var width: Int = (width * contentScale).toInt()

    /** Current height in physical pixels; the constructor argument is in logical units. */
    override var height: Int = (height * contentScale).toInt()

    init {
        val root = XRootWindow(display, screen)
        val visual = visualInfo.pointed.visual
        val colormap = XCreateColormap(display, root, visual, AllocNone)

        val attrs = arena.alloc<XSetWindowAttributes>().apply {
            this.colormap = colormap
            border_pixel = 0uL
            event_mask = ExposureMask or StructureNotifyMask or
                ButtonPressMask or ButtonReleaseMask or PointerMotionMask or
                KeyPressMask or KeyReleaseMask
        }
        window = XCreateWindow(
            display,
            root,
            0,
            0,
            this.width.toUInt(),
            this.height.toUInt(),
            0u,
            visualInfo.pointed.depth,
            InputOutput.toUInt(),
            visual,
            (CWColormap or CWBorderPixel or CWEventMask).toULong(),
            attrs.ptr,
        )
        XStoreName(display, window, title)

        wmDeleteWindow = XInternAtom(display, "WM_DELETE_WINDOW", 0)
        val protocols = arena.alloc<AtomVar>().apply { value = wmDeleteWindow }
        XSetWMProtocols(display, window, protocols.ptr, 1)
    }

    private fun chooseVisual(): CPointer<XVisualInfo>? = memScoped {
        val attribList = intArrayOf(
            GLX_RGBA,
            GLX_DOUBLEBUFFER,
            GLX_DEPTH_SIZE,
            24,
            GLX_STENCIL_SIZE,
            8,
            None.toInt(),
        )
        glXChooseVisual(display, screen, attribList.toCValues().ptr)
    }

    private fun readXftDpi(): Float? = XResourceManagerString(display)
        ?.toKString()
        ?.lineSequence()
        ?.firstOrNull { it.startsWith("Xft.dpi:") }
        ?.substringAfter(':')
        ?.trim()
        ?.toFloatOrNull()

    override fun show() {
        XMapWindow(display, window)
        XFlush(display)
    }

    override fun close() {
        XDestroyWindow(display, window)
        XCloseDisplay(display)
        arena.clear()
    }

    override fun createGlContext(): LinuxGlContext = X11GlContext(this)
}

class RenderSetupException(
    message: String,
) : RuntimeException(message)
