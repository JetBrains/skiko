@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.X11SkikoWindow
import org.jetbrains.skiko.context.LinuxOpenGLContextHandler
import org.jetbrains.skiko.currentNanoTime
import org.jetbrains.skiko.internal.x11.*

internal class X11OpenGLRedrawer(
    private val layer: SkiaLayer,
) : Redrawer {
    private val window: X11SkikoWindow = requireNotNull(layer.x11Window) {
        "X11OpenGLRedrawer requires SkiaLayer to be attached to X11SkikoWindow"
    }
    private val display = window.display
    private val xWindow = window.window
    private val fbConfig = requireNotNull(window.glxFbConfig) { "GLX FBConfig is not initialized" }

    private val glxContext: GLXContext = glXCreateNewContext(
        display,
        fbConfig,
        GLX_RGBA_TYPE,
        null,
        1,
    ) ?: error("glXCreateNewContext failed")

    private val contextHandler = LinuxOpenGLContextHandler(layer)

    override val renderInfo: String
        get() = contextHandler.rendererInfo()

    init {
        makeCurrent()
    }

    override fun dispose() {
        makeCurrent()
        contextHandler.dispose()
        glXMakeCurrent(display, 0.convert(), null)
        glXDestroyContext(display, glxContext)
    }

    override fun needRender(throttledToVsync: Boolean) {
        window.requestRender(throttledToVsync)
    }

    override fun syncBounds() {
        // Context handler recreates surfaces lazily on size changes.
    }

    override fun update(nanoTime: Long) {
        layer.update(nanoTime)
    }

    override fun renderImmediately() {
        makeCurrent()
        layer.update(currentNanoTime())
        contextHandler.draw()
        glXSwapBuffers(display, xWindow)
    }

    private fun makeCurrent() {
        val ok = glXMakeCurrent(display, xWindow, glxContext)
        check(ok != 0) { "glXMakeCurrent failed" }
    }
}
