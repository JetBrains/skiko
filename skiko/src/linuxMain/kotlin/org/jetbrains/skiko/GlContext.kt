package org.jetbrains.skiko

import kotlinx.cinterop.*
import x11gl.*

class GlContext(private val win: X11Window) {
    private val context: GLXContext = glXCreateContext(win.display, win.visualInfo, null, 1)
        ?: throw RenderSetupException("glXCreateContext failed")

    fun makeCurrent() {
        if (glXMakeCurrent(win.display, win.window, context) == 0) {
            throw RenderSetupException("glXMakeCurrent failed")
        }
    }

    fun swapBuffers() {
        glXSwapBuffers(win.display, win.window)
    }

    fun setSwapInterval(interval: Int) {
        val proc = memScoped {
            glXGetProcAddressARB("glXSwapIntervalEXT".cstr.ptr.reinterpret())
        } ?: return
        val swapIntervalExt = proc.reinterpret<CFunction<(CPointer<Display>?, GLXDrawable, Int) -> Unit>>()
        swapIntervalExt(win.display, win.window, interval)
    }

    fun close() {
        glXMakeCurrent(win.display, 0uL, null)
        glXDestroyContext(win.display, context)
    }
}
