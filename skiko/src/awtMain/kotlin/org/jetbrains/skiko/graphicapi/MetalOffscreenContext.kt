package org.jetbrains.skiko.graphicapi

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.RenderException

/**
 * Class that allows drawing into offscreen Metal textures.
 *
 * The context owns the Metal device and the Skia direct context created from it. Each [Texture]
 * owns one Metal texture and exposes it as a Skia [BackendRenderTarget].
 */
@ExperimentalSkikoApi
class MetalOffscreenContext : AutoCloseable {
    private val device = makeMetalDevice().also {
        if (it == 0L) {
            throw RenderException("Failed to create Metal device.")
        }
    }

    val directContext = DirectContext(makeMetalContext(device))

    override fun close() {
        directContext.close()
        disposeMetalDevice(device)
    }

    /**
     * Offscreen Metal texture that can be wrapped into a Skia Surface.
     */
    inner class Texture(width: Int, height: Int) : AutoCloseable {
        private val texture = makeMetalTexture(device, width, height).also {
            if (it == 0L) {
                throw RenderException("Can't allocate Metal texture")
            }
        }

        val backendRenderTarget = BackendRenderTarget(makeMetalRenderTargetOffScreen(texture))

        override fun close() {
            backendRenderTarget.close()
            disposeMetalTexture(texture)
        }
    }

    private external fun makeMetalDevice(): NativePointer
    private external fun disposeMetalDevice(devicePtr: NativePointer)
    private external fun makeMetalContext(devicePtr: NativePointer): NativePointer
    private external fun makeMetalTexture(devicePtr: NativePointer, width: Int, height: Int): NativePointer
    private external fun disposeMetalTexture(texturePtr: NativePointer)
    private external fun makeMetalRenderTargetOffScreen(texturePtr: NativePointer): NativePointer

    private companion object {
        init {
            Library.load()
        }
    }
}
