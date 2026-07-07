package org.jetbrains.skiko

import cnames.structs.wl_callback
import cnames.structs.wl_compositor
import cnames.structs.wl_display
import cnames.structs.wl_output
import cnames.structs.wl_registry
import cnames.structs.wl_surface
import cnames.structs.wp_fractional_scale_manager_v1
import cnames.structs.wp_fractional_scale_v1
import cnames.structs.wp_viewport
import cnames.structs.wp_viewporter
import cnames.structs.xdg_surface
import cnames.structs.xdg_toplevel
import cnames.structs.xdg_wm_base
import cnames.structs.zxdg_decoration_manager_v1
import cnames.structs.zxdg_toplevel_decoration_v1
import kotlinx.cinterop.*
import platform.posix.POLLIN
import platform.posix.poll
import platform.posix.pollfd
import waylandegl.*

/**
 * The fractional-scale-v1 protocol reports the preferred scale as a numerator over 120
 * (e.g. 180 means 1.5).
 */
private const val FRACTIONAL_SCALE_DENOMINATOR = 120f

/**
 * [LinuxWindow] backed by a Wayland `xdg_toplevel` surface, the compositor-agnostic
 * sibling of [X11Window].
 *
 * Geometry model matches the seam contract: [width]/[height] are physical pixels,
 * [contentScale] feeds UI density. The compositor drives the logical size through
 * `xdg_toplevel.configure` and the scale through `wp_fractional_scale_v1.preferred_scale`
 * when the compositor supports it (falling back to the advertised `wl_output` integer
 * scale otherwise); physical size is derived from both.
 *
 * The owner of the window is responsible for pumping the event queue by calling
 * [dispatchPendingEvents] from its main loop, the Wayland equivalent of the
 * `XPending`/`XNextEvent` loop an [X11Window] owner runs.
 */
class WaylandWindow(
    title: String,
    width: Int,
    height: Int,
) : LinuxWindow {
    private val arena = Arena()
    private val self = StableRef.create(this)

    val display: CPointer<wl_display> =
        wl_display_connect(null)
            ?: throw RenderSetupException("Cannot connect to Wayland display; is WAYLAND_DISPLAY set?")

    private var compositor: CPointer<wl_compositor>? = null
    private var wmBase: CPointer<xdg_wm_base>? = null
    private var viewporter: CPointer<wp_viewporter>? = null
    private var fractionalScaleManager: CPointer<wp_fractional_scale_manager_v1>? = null
    private var decorationManager: CPointer<zxdg_decoration_manager_v1>? = null
    private var outputScale = 1

    internal val surface: CPointer<wl_surface>
    private val xdgSurface: CPointer<xdg_surface>
    private val toplevel: CPointer<xdg_toplevel>
    private var viewport: CPointer<wp_viewport>? = null
    private var fractionalScale: CPointer<wp_fractional_scale_v1>? = null
    private var toplevelDecoration: CPointer<zxdg_toplevel_decoration_v1>? = null

    /** Logical (compositor-space) size; physical size is derived via [contentScale]. */
    private var logicalWidth = width
    private var logicalHeight = height

    private var configured = false

    /** Set when the compositor asks the toplevel to close; the event loop owner decides. */
    var closeRequested: Boolean = false
        private set

    override var contentScale: Float = 1f
        private set

    override val width: Int get() = (logicalWidth * contentScale).toInt()

    override val height: Int get() = (logicalHeight * contentScale).toInt()

    /** Lets the EGL context follow compositor-driven resizes; set by [WaylandEglContext]. */
    internal var onPhysicalResize: ((width: Int, height: Int) -> Unit)? = null

    private var pendingFrame: (() -> Unit)? = null

    private val registryListener = arena.alloc<wl_registry_listener>().apply {
        global = staticCFunction {
                data: COpaquePointer?,
                registry: CPointer<wl_registry>?,
                name: UInt,
                iface: CPointer<ByteVar>?,
                version: UInt,
            ->
            if (data != null && registry != null && iface != null) {
                data.asStableRef<WaylandWindow>().get().bindGlobal(registry, name, iface.toKString(), version)
            }
        }
        global_remove = staticCFunction { _: COpaquePointer?, _: CPointer<wl_registry>?, _: UInt -> }
    }

    private val wmBaseListener = arena.alloc<xdg_wm_base_listener>().apply {
        ping = staticCFunction { _: COpaquePointer?, wmBase: CPointer<xdg_wm_base>?, serial: UInt ->
            xdg_wm_base_pong(wmBase, serial)
        }
    }

    private val xdgSurfaceListener = arena.alloc<xdg_surface_listener>().apply {
        configure = staticCFunction { data: COpaquePointer?, xdgSurface: CPointer<xdg_surface>?, serial: UInt ->
            xdg_surface_ack_configure(xdgSurface, serial)
            data?.asStableRef<WaylandWindow>()?.get()?.configured = true
        }
    }

    private val toplevelListener = arena.alloc<xdg_toplevel_listener>().apply {
        configure = staticCFunction {
                data: COpaquePointer?,
                _: CPointer<xdg_toplevel>?,
                width: Int,
                height: Int,
                _: CPointer<wl_array>?,
            ->
            // 0x0 means the compositor defers to the client; keep the current size then.
            if (data != null && width > 0 && height > 0) {
                data.asStableRef<WaylandWindow>().get().onLogicalResize(width, height)
            }
        }
        close = staticCFunction { data: COpaquePointer?, _: CPointer<xdg_toplevel>? ->
            data?.asStableRef<WaylandWindow>()?.get()?.closeRequested = true
        }
    }

    private val outputListener = arena.alloc<wl_output_listener>().apply {
        geometry = staticCFunction {
                _: COpaquePointer?, _: CPointer<wl_output>?,
                _: Int, _: Int, _: Int, _: Int, _: Int,
                _: CPointer<ByteVar>?, _: CPointer<ByteVar>?, _: Int,
            ->
        }
        mode = staticCFunction {
                _: COpaquePointer?, _: CPointer<wl_output>?, _: UInt, _: Int, _: Int, _: Int,
            ->
        }
        done = staticCFunction { _: COpaquePointer?, _: CPointer<wl_output>? -> }
        scale = staticCFunction { data: COpaquePointer?, _: CPointer<wl_output>?, factor: Int ->
            if (data != null && factor > 0) {
                val window = data.asStableRef<WaylandWindow>().get()
                window.outputScale = maxOf(window.outputScale, factor)
            }
        }
    }

    private val fractionalScaleListener = arena.alloc<wp_fractional_scale_v1_listener>().apply {
        preferred_scale = staticCFunction {
                data: COpaquePointer?, _: CPointer<wp_fractional_scale_v1>?, scale: UInt,
            ->
            data?.asStableRef<WaylandWindow>()?.get()
                ?.onPreferredScale(scale.toInt() / FRACTIONAL_SCALE_DENOMINATOR)
        }
    }

    // The compositor may pick either mode; there is no client-side fallback yet
    // (libdecor is deferred), so a client-side answer just leaves the window borderless.
    private val toplevelDecorationListener = arena.alloc<zxdg_toplevel_decoration_v1_listener>().apply {
        configure = staticCFunction {
                _: COpaquePointer?, _: CPointer<zxdg_toplevel_decoration_v1>?, _: UInt ->
        }
    }

    private val frameListener = arena.alloc<wl_callback_listener>().apply {
        done = staticCFunction { data: COpaquePointer?, callback: CPointer<wl_callback>?, _: UInt ->
            callback?.let { wl_callback_destroy(it) }
            data?.asStableRef<WaylandWindow>()?.get()?.fireFrame()
        }
    }

    init {
        val registry = wl_display_get_registry(display)
            ?: throw RenderSetupException("wl_display_get_registry failed")
        wl_registry_add_listener(registry, registryListener.ptr, self.asCPointer())
        // First roundtrip delivers the globals, second the events (e.g. wl_output.scale)
        // sent in response to the binds the first one triggered.
        wl_display_roundtrip(display)
        wl_display_roundtrip(display)

        val compositor = compositor
            ?: throw RenderSetupException("Wayland compositor does not advertise wl_compositor")
        val wmBase = wmBase
            ?: throw RenderSetupException("Wayland compositor does not advertise xdg_wm_base")

        surface = wl_compositor_create_surface(compositor)
            ?: throw RenderSetupException("wl_compositor_create_surface failed")
        xdgSurface = xdg_wm_base_get_xdg_surface(wmBase, surface)
            ?: throw RenderSetupException("xdg_wm_base_get_xdg_surface failed")
        xdg_surface_add_listener(xdgSurface, xdgSurfaceListener.ptr, self.asCPointer())
        toplevel = xdg_surface_get_toplevel(xdgSurface)
            ?: throw RenderSetupException("xdg_surface_get_toplevel failed")
        xdg_toplevel_add_listener(toplevel, toplevelListener.ptr, self.asCPointer())
        xdg_toplevel_set_title(toplevel, title)

        // Ask for server-side decorations where the compositor offers the choice (KWin,
        // sway); GNOME does not advertise the manager and the window stays borderless.
        decorationManager?.let { manager ->
            toplevelDecoration =
                zxdg_decoration_manager_v1_get_toplevel_decoration(manager, toplevel)?.also {
                    zxdg_toplevel_decoration_v1_add_listener(it, toplevelDecorationListener.ptr, self.asCPointer())
                    zxdg_toplevel_decoration_v1_set_mode(it, ZXDG_TOPLEVEL_DECORATION_V1_MODE_SERVER_SIDE)
                }
        }

        val fractionalScaleManager = fractionalScaleManager
        if (fractionalScaleManager != null && viewporter != null) {
            fractionalScale =
                wp_fractional_scale_manager_v1_get_fractional_scale(fractionalScaleManager, surface)?.also {
                    wp_fractional_scale_v1_add_listener(it, fractionalScaleListener.ptr, self.asCPointer())
                }
        } else if (outputScale > 1) {
            contentScale = outputScale.toFloat()
            wl_surface_set_buffer_scale(surface, outputScale)
        }

        // Commit the (buffer-less) initial state and wait for the first configure;
        // xdg-shell forbids attaching a buffer before it is acked.
        wl_surface_commit(surface)
        while (!configured) {
            if (wl_display_dispatch(display) < 0) {
                throw RenderSetupException("Wayland connection lost while waiting for xdg_surface.configure")
            }
        }
    }

    private fun bindGlobal(registry: CPointer<wl_registry>, name: UInt, iface: String, version: UInt) {
        when (iface) {
            "wl_compositor" ->
                compositor = wl_registry_bind(registry, name, wl_compositor_interface.ptr, minOf(version, 4u))
                    ?.reinterpret()
            "xdg_wm_base" -> {
                wmBase = wl_registry_bind(registry, name, xdg_wm_base_interface.ptr, 1u)?.reinterpret()
                wmBase?.let { xdg_wm_base_add_listener(it, wmBaseListener.ptr, self.asCPointer()) }
            }
            "wp_viewporter" ->
                viewporter = wl_registry_bind(registry, name, wp_viewporter_interface.ptr, 1u)?.reinterpret()
            "wp_fractional_scale_manager_v1" ->
                fractionalScaleManager =
                    wl_registry_bind(registry, name, wp_fractional_scale_manager_v1_interface.ptr, 1u)
                        ?.reinterpret()
            "zxdg_decoration_manager_v1" ->
                decorationManager =
                    wl_registry_bind(registry, name, zxdg_decoration_manager_v1_interface.ptr, 1u)
                        ?.reinterpret()
            "wl_output" -> {
                val output = wl_registry_bind(registry, name, wl_output_interface.ptr, minOf(version, 2u))
                    ?.reinterpret<wl_output>()
                output?.let { wl_output_add_listener(it, outputListener.ptr, self.asCPointer()) }
            }
        }
    }

    private fun onLogicalResize(newWidth: Int, newHeight: Int) {
        if (newWidth == logicalWidth && newHeight == logicalHeight) return
        logicalWidth = newWidth
        logicalHeight = newHeight
        applyScale()
        onPhysicalResize?.invoke(width, height)
    }

    private fun onPreferredScale(newScale: Float) {
        if (newScale == contentScale || newScale <= 0f) return
        contentScale = newScale
        applyScale()
        onPhysicalResize?.invoke(width, height)
    }

    /**
     * With a fractional scale the buffer stays physical-sized and the viewport maps it to
     * the logical size; without one the surface stays at scale 1 (or the integer
     * buffer-scale chosen at init) and no viewport is needed.
     */
    private fun applyScale() {
        if (fractionalScale == null || contentScale == 1f) return
        val viewport = viewport
            ?: wp_viewporter_get_viewport(viewporter, surface).also { viewport = it }
            ?: return
        wp_viewport_set_destination(viewport, logicalWidth, logicalHeight)
    }

    /**
     * Non-blocking event pump for the owner's main loop: dispatches queued events, flushes
     * requests, and reads whatever the compositor has already sent, per libwayland's
     * `prepare_read`/`read_events` contract.
     */
    fun dispatchPendingEvents() {
        while (wl_display_prepare_read(display) != 0) {
            wl_display_dispatch_pending(display)
        }
        wl_display_flush(display)
        memScoped {
            val fds = alloc<pollfd>().apply {
                fd = wl_display_get_fd(display)
                events = POLLIN.toShort()
            }
            if (poll(fds.ptr, 1.convert(), 0) > 0) {
                wl_display_read_events(display)
            } else {
                wl_display_cancel_read(display)
            }
        }
        wl_display_dispatch_pending(display)
    }

    override fun frameCallback(onFrame: () -> Unit) {
        pendingFrame = onFrame
        val callback = wl_surface_frame(surface) ?: return
        wl_callback_add_listener(callback, frameListener.ptr, self.asCPointer())
        // The frame request rides the next commit (the eglSwapBuffers that follows).
    }

    private fun fireFrame() {
        pendingFrame?.also { pendingFrame = null }?.invoke()
    }

    override fun show() {
        wl_surface_commit(surface)
        wl_display_flush(display)
    }

    override fun close() {
        pendingFrame = null
        fractionalScale?.let { wp_fractional_scale_v1_destroy(it) }
        viewport?.let { wp_viewport_destroy(it) }
        toplevelDecoration?.let { zxdg_toplevel_decoration_v1_destroy(it) }
        xdg_toplevel_destroy(toplevel)
        xdg_surface_destroy(xdgSurface)
        wl_surface_destroy(surface)
        wl_display_disconnect(display)
        arena.clear()
        self.dispose()
    }

    override fun createGlContext(): LinuxGlContext = WaylandEglContext(this)
}
