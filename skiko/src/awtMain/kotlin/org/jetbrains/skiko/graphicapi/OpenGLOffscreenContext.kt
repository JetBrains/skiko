package org.jetbrains.skiko.graphicapi

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.makeGLContext
import org.jetbrains.skiko.makeGLRenderTarget

/**
 * Class that allows drawing into offscreen OpenGL textures on Linux.
 *
 * The context owns a GLX context and an offscreen pbuffer. [withTexture] makes that context current
 * while the texture exists so Skia GL objects are created against the expected backend context.
 */
@ExperimentalSkikoApi
class OpenGLOffscreenContext : AutoCloseable {
    private val contextPtr = makeOffScreenContext().also {
        if (it == 0L) {
            throw RenderException("Cannot create OpenGL context")
        }
    }

    private var bufferPtr: NativePointer = 0L
    private var _directContext: DirectContext? = null

    val directContext: DirectContext
        get() = _directContext ?: error("OpenGL context is not current")

    /**
     * Makes the offscreen OpenGL context current and provides a temporary texture-backed render target.
     */
    fun <T> withTexture(width: Int, height: Int, block: (Texture) -> T): T {
        bufferPtr = makeOffScreenBuffer(contextPtr, bufferPtr, width, height)
        if (bufferPtr == 0L) {
            throw RenderException("Cannot create offscreen OpenGL buffer")
        }

        startRendering(contextPtr, bufferPtr)
        try {
            if (_directContext == null) {
                _directContext = makeGLContext()
            }

            val texture = Texture(width, height)
            return try {
                block(texture)
            } finally {
                texture.close()
            }
        } finally {
            finishRendering(contextPtr)
        }
    }

    override fun close() {
        _directContext?.close()
        _directContext = null
        disposeOffScreenBuffer(bufferPtr)
        bufferPtr = 0L
        disposeOffScreenContext(contextPtr)
    }

    /**
     * OpenGL texture and framebuffer pair that can be wrapped into a Skia Surface.
     */
    inner class Texture(width: Int, height: Int) : AutoCloseable {
        private val texturePtr = createAndBindTexture(width, height).also {
            if (it == 0L) {
                throw RenderException("Cannot create offscreen OpenGL texture")
            }
        }

        val backendRenderTarget: BackendRenderTarget = makeGLRenderTarget(
            width,
            height,
            0,
            8,
            getFboId(texturePtr),
            FramebufferFormat.GR_GL_RGBA8
        )

        override fun close() {
            backendRenderTarget.close()
            unbindAndDisposeTexture(texturePtr)
        }
    }

    private external fun makeOffScreenContext(): NativePointer
    private external fun disposeOffScreenContext(contextPtr: NativePointer)
    private external fun makeOffScreenBuffer(
        contextPtr: NativePointer,
        oldBufferPtr: NativePointer,
        width: Int,
        height: Int
    ): NativePointer
    private external fun disposeOffScreenBuffer(bufferPtr: NativePointer)
    private external fun startRendering(contextPtr: NativePointer, bufferPtr: NativePointer)
    private external fun finishRendering(contextPtr: NativePointer)
    private external fun createAndBindTexture(width: Int, height: Int): NativePointer
    private external fun getFboId(texturePtr: NativePointer): Int
    private external fun unbindAndDisposeTexture(texturePtr: NativePointer)

    private companion object {
        init {
            Library.load()
        }
    }
}
