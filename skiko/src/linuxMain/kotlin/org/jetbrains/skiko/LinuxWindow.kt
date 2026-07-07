package org.jetbrains.skiko

/**
 * Windowing seam between [SkiaLayer]/[org.jetbrains.skiko.redrawer.Redrawer] and the
 * concrete Linux display-server backend (X11/GLX today, Wayland/EGL landing alongside
 * it). Geometry is always physical pixels; [contentScale] is the separate factor that
 * feeds UI density, matching the model X11 already established.
 */
internal interface LinuxWindow {
    val width: Int
    val height: Int
    val contentScale: Float

    fun show()

    fun close()

    /**
     * Creates the GL context matching this window's native surface (GLX vs EGL). Public
     * on the interface's implementers (e.g. [X11Window]) since those constructors are
     * public API consumed outside this module; [LinuxGlContext] itself is a plain public
     * type with no members meant for outside use beyond this call chain.
     */
    fun createGlContext(): LinuxGlContext

    /**
     * Registers a one-shot callback for the next compositor-paced frame, letting the
     * redrawer replace timer-driven pacing with real vsync signalling where the backend
     * supports it (Wayland's `wl_surface.frame`). Default is a no-op: X11 has no
     * equivalent and keeps pacing itself via [glXSwapIntervalEXT]-driven vsync.
     */
    fun frameCallback(onFrame: () -> Unit) {}
}

/**
 * GL context bound to a [LinuxWindow]'s native surface (GLX context vs EGL
 * context/surface). Public only because implementers of the internal [LinuxWindow] seam
 * (e.g. [X11Window]) are themselves public classes whose `createGlContext()` return type
 * must not leak an internal type; there is no supported use of this type outside skiko.
 */
interface LinuxGlContext {
    fun makeCurrent()

    fun swapBuffers()

    fun setSwapInterval(interval: Int)

    fun close()
}
