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

    private val commandQueue = getCommandQueue(device)

    val directContext = DirectContext.makeMetal(device, commandQueue)

    override fun close() {
        directContext.close()
        disposeCommandQueue(commandQueue)
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

        val backendRenderTarget = BackendRenderTarget.makeMetal(
            width = width,
            height = height,
            texturePtr = texture
        )

        override fun close() {
            backendRenderTarget.close()
            disposeMetalTexture(texture)
        }
    }

    private external fun makeMetalDevice(): NativePointer
    private external fun disposeMetalDevice(devicePtr: NativePointer)
    private external fun makeMetalTexture(devicePtr: NativePointer, width: Int, height: Int): NativePointer
    private external fun disposeMetalTexture(texturePtr: NativePointer)
    private external fun getCommandQueue(devicePtr: NativePointer): NativePointer
    private external fun disposeCommandQueue(queuePtr: NativePointer)

    private companion object {
        init {
            Library.load()
        }
    }
}
