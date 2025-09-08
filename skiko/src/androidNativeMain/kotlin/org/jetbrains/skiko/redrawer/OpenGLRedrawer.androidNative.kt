package org.jetbrains.skiko.redrawer

import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.newFixedThreadPoolContext
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.context.AndroidNativeOpenGLContextHandler
import platform.egl.EGLConfig
import platform.egl.EGLConfigVar
import platform.egl.EGLContext
import platform.egl.EGLDisplay
import platform.egl.EGLSurface
import platform.egl.EGL_ALPHA_SIZE
import platform.egl.EGL_BLUE_SIZE
import platform.egl.EGL_CONTEXT_CLIENT_VERSION
import platform.egl.EGL_DEFAULT_DISPLAY
import platform.egl.EGL_DEPTH_SIZE
import platform.egl.EGL_FALSE
import platform.egl.EGL_GREEN_SIZE
import platform.egl.EGL_NONE
import platform.egl.EGL_NO_CONTEXT
import platform.egl.EGL_NO_DISPLAY
import platform.egl.EGL_NO_SURFACE
import platform.egl.EGL_OPENGL_ES2_BIT
import platform.egl.EGL_RED_SIZE
import platform.egl.EGL_RENDERABLE_TYPE
import platform.egl.EGL_STENCIL_SIZE
import platform.egl.EGL_SURFACE_TYPE
import platform.egl.EGL_WINDOW_BIT
import platform.egl.EGLintVar
import platform.egl.eglChooseConfig
import platform.egl.eglCreateContext
import platform.egl.eglCreateWindowSurface
import platform.egl.eglDestroyContext
import platform.egl.eglDestroySurface
import platform.egl.eglGetDisplay
import platform.egl.eglInitialize
import platform.egl.eglMakeCurrent
import platform.egl.eglSwapBuffers
import platform.egl.eglTerminate

internal class AndroidNativeOpenGLRedrawer(
    private val skiaLayer: SkiaLayer,
) : Redrawer {

    private val contextHandler = AndroidNativeOpenGLContextHandler(skiaLayer)

    private val frameDispatcher = FrameDispatcher(newFixedThreadPoolContext(1, "OpenGLFrameDispatcher")) {
        redrawImmediately()
    }

    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null
    private var eglSurface: EGLSurface? = null
    private var eglConfig: EGLConfig? = null

    init {
        initEGL()
        createEGLSurface()
    }

    override fun dispose() {
        frameDispatcher.cancel()
        contextHandler.dispose()
        destroyEGLSurface()
        destroyEGL()
    }

    override fun needRedraw() {
        frameDispatcher.scheduleFrame()
    }

    override fun redrawImmediately() {
        val currentTime = kotlin.system.getTimeNanos()
        skiaLayer.update(currentTime)
        eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        contextHandler.draw()
        if (eglDisplay != null && eglSurface != null) {
            eglSwapBuffers(eglDisplay, eglSurface)
        }
    }

    override val renderInfo: String
        get() = contextHandler.rendererInfo()

    private fun initEGL(): Boolean {
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL_NO_DISPLAY) {
            return false
        }

        if (eglInitialize(eglDisplay, null, null) == EGL_FALSE.toUInt()) {
            return false
        }

        val configAttrs = cValuesOf(
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 16,
            EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_NONE
        )

        memScoped {
            val configs = alloc<EGLConfigVar>()
            val numConfigs = alloc<EGLintVar>()

            if (eglChooseConfig(
                    eglDisplay,
                    configAttrs,
                    configs.ptr,
                    1,
                    numConfigs.ptr
                ) == EGL_FALSE.toUInt() || numConfigs.value == 0
            ) {
                return false
            }

            eglConfig = configs.value
        }

        val contextAttrs = cValuesOf(
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
        )

        eglContext = eglCreateContext(
            eglDisplay,
            eglConfig,
            EGL_NO_CONTEXT,
            contextAttrs
        )

        return eglContext != EGL_NO_CONTEXT
    }

    private fun createEGLSurface(): Boolean {
        val window = skiaLayer.nativeWindow ?: return false
        if (eglDisplay == null || eglConfig == null) return false

        eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window, null)
        return eglSurface != EGL_NO_SURFACE
    }

    private fun destroyEGLSurface() {
        if (eglDisplay != null && eglSurface != null) {
            eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)
            eglDestroySurface(eglDisplay, eglSurface)
            eglSurface = null
        }
    }

    private fun destroyEGL() {
        if (eglDisplay != null) {
            if (eglContext != null) {
                eglDestroyContext(eglDisplay, eglContext)
                eglContext = null
            }
            eglTerminate(eglDisplay)
            eglDisplay = null
        }
        eglConfig = null
    }
}
