package org.jetbrains.skiko.graphicapi

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.GpuPriority
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.alignedTextureWidth
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.chooseAdapter
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.createDirectXOffscreenDevice
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.disposeDevice
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.disposeDirectXTexture
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXContext
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXRenderTargetOffScreen
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.makeDirectXTexture
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.readPixels
import org.jetbrains.skiko.graphicapi.InternalDirectXApi.waitForCompletion

/**
 * Class that allows drawing into offscreen DirectX texture.
 *
 * Used in the `benchmarks` project:
 * https://github.com/JetBrains/compose-multiplatform/blob/cedd48f99e877f8b936ff574812d573baf231307/benchmarks/multiplatform/benchmarks/src/commonMain/kotlin/MeasureComposable.kt#L16
 */
@ExperimentalSkikoApi
class DirectXOffscreenContext : AutoCloseable {
    private val adapter = chooseAdapter(GpuPriority.Integrated)

    private val device = createDirectXOffscreenDevice(adapter).also {
        if (it == 0L) {
            throw RenderException("Failed to create DirectX12 device.")
        }
    }

    val directContext = DirectContext(makeDirectXContext(device))

    override fun close() {
        directContext.close()
        disposeDevice(device)
    }

    inner class Texture(desiredWidth: Int, desiredHeight: Int) : AutoCloseable {
        /**
         * Aligned width/height that is needed for performance optimization,
         * since DirectX uses aligned bytebuffer.
         */
        val actualWidth = alignedTextureWidth(desiredWidth)

        val actualHeight = desiredHeight

        private val texture = makeDirectXTexture(device, 0, actualWidth, actualHeight).also {
            if (it == 0L) {
                throw RenderException("Can't allocate DirectX resources")
            }
        }

        val backendRenderTarget = BackendRenderTarget(makeDirectXRenderTargetOffScreen(texture))

        override fun close() {
            backendRenderTarget.close()
            disposeDirectXTexture(texture)
        }

        fun waitForCompletion() {
            waitForCompletion(device, texture)
        }

        fun readPixels(byteArray: ByteArray) {
            if (!readPixels(texture, byteArray)) {
                throw RenderException("Couldn't read pixels")
            }
        }
    }
}
