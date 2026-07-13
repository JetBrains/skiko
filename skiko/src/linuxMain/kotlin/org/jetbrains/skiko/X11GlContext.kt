package org.jetbrains.skiko

import kotlinx.cinterop.*
import x11gl.*

internal class X11GlContext(
    private val win: X11Window,
) : LinuxGlContext {
    private val context: GLXContext =
        glXCreateContext(win.display, win.visualInfo, null, 1)
            ?: throw RenderSetupException("glXCreateContext failed")

    override fun makeCurrent() {
        if (glXMakeCurrent(win.display, win.window, context) == 0) {
            throw RenderSetupException("glXMakeCurrent failed")
        }
    }

    override fun swapBuffers() {
        glXSwapBuffers(win.display, win.window)
    }

    override fun setSwapInterval(interval: Int) {
        val proc =
            memScoped {
                glXGetProcAddressARB("glXSwapIntervalEXT".cstr.ptr.reinterpret())
            } ?: return
        val swapIntervalExt = proc.reinterpret<CFunction<(CPointer<Display>?, GLXDrawable, Int) -> Unit>>()
        swapIntervalExt(win.display, win.window, interval)
    }

    override fun close() {
        glXMakeCurrent(win.display, 0uL, null)
        glXDestroyContext(win.display, context)
    }
}
