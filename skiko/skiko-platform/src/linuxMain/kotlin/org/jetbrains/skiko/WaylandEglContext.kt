package org.jetbrains.skiko

import cnames.structs.wl_egl_window
import kotlinx.cinterop.*
import waylandegl.*

/**
 * [LinuxGlContext] backed by EGL on a `wl_egl_window`, the Wayland sibling of
 * [X11GlContext]. Creates a desktop OpenGL (not GLES) context, matching what
 * `DirectContext.makeGL()` expects, with the same RGBA8/depth 24/stencil 8 config the
 * GLX path requests.
 */
internal class WaylandEglContext(
    private val win: WaylandWindow,
) : LinuxGlContext {
    private val eglDisplay: EGLDisplay =
        eglGetPlatformDisplay(EGL_PLATFORM_WAYLAND_KHR.convert(), win.display, null)
            ?: throw RenderSetupException("eglGetPlatformDisplay failed for the Wayland display")

    private val eglConfig: EGLConfig
    private val eglContext: EGLContext
    private val eglWindow: CPointer<wl_egl_window>
    private val eglSurface: EGLSurface

    init {
        memScoped {
            val major = alloc<EGLintVar>()
            val minor = alloc<EGLintVar>()
            if (eglInitialize(eglDisplay, major.ptr, minor.ptr) == EGL_FALSE.toUInt()) {
                throw RenderSetupException("eglInitialize failed")
            }
        }
        if (eglBindAPI(EGL_OPENGL_API.convert()) == EGL_FALSE.toUInt()) {
            throw RenderSetupException("eglBindAPI(EGL_OPENGL_API) failed; desktop GL unsupported")
        }

        eglConfig = memScoped {
            val attribs = intArrayOf(
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_RENDERABLE_TYPE, EGL_OPENGL_BIT,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_DEPTH_SIZE, 24,
                EGL_STENCIL_SIZE, 8,
                EGL_NONE,
            )
            val config = alloc<EGLConfigVar>()
            val numConfigs = alloc<EGLintVar>()
            val chosen = eglChooseConfig(
                eglDisplay, attribs.toCValues().ptr, config.ptr, 1, numConfigs.ptr
            )
            if (chosen == EGL_FALSE.toUInt() || numConfigs.value < 1) {
                throw RenderSetupException("No EGL config with RGBA8 + depth 24 + stencil 8")
            }
            config.value
                ?: throw RenderSetupException("eglChooseConfig returned a null config")
        }

        eglContext = eglCreateContext(eglDisplay, eglConfig, null, null)
            ?: throw RenderSetupException("eglCreateContext failed")

        eglWindow = wl_egl_window_create(
            win.surface,
            win.width.coerceAtLeast(1),
            win.height.coerceAtLeast(1),
        ) ?: throw RenderSetupException("wl_egl_window_create failed")
        win.onPhysicalResize = { width, height ->
            wl_egl_window_resize(eglWindow, width.coerceAtLeast(1), height.coerceAtLeast(1), 0, 0)
        }

        eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, eglWindow, null)
            ?: throw RenderSetupException("eglCreateWindowSurface failed")
    }

    override fun makeCurrent() {
        if (eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext) == EGL_FALSE.toUInt()) {
            throw RenderSetupException("eglMakeCurrent failed")
        }
    }

    override fun swapBuffers() {
        if (eglSwapBuffers(eglDisplay, eglSurface) == EGL_FALSE.toUInt()) {
            val err = eglGetError()
            println("eglSwapBuffers failed: error 0x${err.toString(16)}")
        }
    }

    /**
     * Deliberately forces interval 0 regardless of what is asked: frame pacing comes from
     * `wl_surface.frame` (see [WaylandWindow.frameCallback]), and a non-zero EGL swap
     * interval would make [swapBuffers] block forever while the surface is occluded.
     */
    override fun setSwapInterval(interval: Int) {
        eglSwapInterval(eglDisplay, 0)
    }

    override fun close() {
        win.onPhysicalResize = null
        eglMakeCurrent(eglDisplay, null, null, null)
        eglDestroySurface(eglDisplay, eglSurface)
        wl_egl_window_destroy(eglWindow)
        eglDestroyContext(eglDisplay, eglContext)
        eglTerminate(eglDisplay)
    }
}
