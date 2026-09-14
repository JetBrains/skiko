package org.jetbrains.skiko.graphicapi

import kotlinx.browser.document
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GL
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.wasm.createWebGLContext
import org.w3c.dom.HTMLCanvasElement

/**
 * Class that allows drawing into an offscreen WebGL render target.
 *
 * The context owns a hidden canvas and the Skia direct context created from that canvas' WebGL
 * context. WebGL uses a current-context model, so [withTexture] makes this context current before
 * exposing the render target to callers.
 */
@ExperimentalSkikoApi
class WebGLOffscreenContext : AutoCloseable {
    private val canvas = document.createElement("canvas") as HTMLCanvasElement

    private val contextPtr: NativePointer = createWebGLContext(canvas).also {
        if (it == NullPointer) {
            throw RenderException("Cannot create WebGL context")
        }
    }

    val directContext: DirectContext = makeCurrent().let {
        DirectContext.makeGL()
    }

    /**
     * Resizes the hidden canvas and provides a temporary render target for the canvas framebuffer.
     */
    fun <T> withTexture(width: Int, height: Int, block: (Texture) -> T): T {
        canvas.width = width
        canvas.height = height
        makeCurrent()

        val texture = Texture(width, height)
        return try {
            block(texture)
        } finally {
            texture.close()
        }
    }

    override fun close() {
        makeCurrent()
        directContext.close()
    }

    /**
     * WebGL render target for the hidden canvas' default framebuffer.
     *
     * This does not allocate a separate WebGLTexture; framebuffer id 0 refers to the canvas backing
     * buffer for the current WebGL context.
     */
    class Texture(width: Int, height: Int) : AutoCloseable {
        val backendRenderTarget = BackendRenderTarget.makeGL(
            width,
            height,
            1,
            8,
            0,
            FramebufferFormat.GR_GL_RGBA8
        )

        override fun close() {
            backendRenderTarget.close()
        }
    }

    private fun makeCurrent() {
        if (!GL.makeContextCurrent(contextPtr)) {
            throw RenderException("Cannot make WebGL context current")
        }
    }
}
