package org.jetbrains.skiko.context

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.SkiaLayer
import platform.android.ANativeWindow_getHeight
import platform.android.ANativeWindow_getWidth
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
import platform.egl.EGL_TRUE
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
import platform.gles31.GL_DRAW_FRAMEBUFFER_BINDING
import platform.gles31.GL_RENDERER
import platform.gles31.GL_VENDOR
import platform.gles31.glGetIntegerv
import platform.gles31.glGetString

internal class AndroidNativeOpenGLContextHandler(layer: SkiaLayer) :
    ContextHandler(layer, layer::draw) {

    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null
    private var eglSurface: EGLSurface? = null
    private var eglConfig: EGLConfig? = null

    private var width = 0
    private var height = 0

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

    private fun createEGLSurface(window: CPointer<cnames.structs.ANativeWindow>): Boolean {
        if (eglDisplay == null || eglConfig == null) return false

        eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window, null)
        if (eglSurface == EGL_NO_SURFACE) {
            return false
        }

        return eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext) == EGL_TRUE.toUInt()
    }

    private fun destroyEGLSurface() {
        if (eglDisplay != null && eglSurface != null) {
            eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)
            eglDestroySurface(eglDisplay, eglSurface)
            eglSurface = null
        }
    }

    private fun destroyEGL() {
        destroyEGLSurface()

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

    override fun initContext(): Boolean {
        return eglDisplay != null || initEGL()
    }

    override fun initCanvas() {
        val window = layer.nativeWindow ?: return

        val newWidth = ANativeWindow_getWidth(window)
        val newHeight = ANativeWindow_getHeight(window)

        if (width != newWidth || height != newHeight || surface == null) {
            width = newWidth
            height = newHeight

            disposeCanvas()

            destroyEGLSurface()
            if (!createEGLSurface(window)) {
                return
            }

            context = DirectContext.makeGL()

            val fbId = memScoped {
                val outFbId = alloc<IntVar>()
                glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, outFbId.ptr)
                outFbId.value
            }

            renderTarget = BackendRenderTarget.makeGL(
                width,
                height,
                0,  // samples
                8,  // stencilBits
                fbId,
                FramebufferFormat.GR_GL_RGBA8
            )

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB
            )
        }

        canvas = surface?.canvas
    }

    override fun flush() {
        super.flush()
        if (eglDisplay != null && eglSurface != null) {
            eglSwapBuffers(eglDisplay, eglSurface)
        }
    }

    override fun dispose() {
        super.dispose()
        destroyEGL()
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Vendor: ${glGetString(GL_VENDOR)}\n" +
                "Model: ${glGetString(GL_RENDERER)}\n"
    }
}
